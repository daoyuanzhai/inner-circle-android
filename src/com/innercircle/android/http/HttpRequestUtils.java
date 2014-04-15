package com.innercircle.android.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.model.InnerCircleUser;
import com.innercircle.android.model.InnerCircleUserList;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.SharedPreferencesUtils;

public class HttpRequestUtils {
    private static final String TAG = HttpRequestUtils.class.getSimpleName();

    private HttpRequestUtils () {
    }

    private static String urlBuilder(final String apiName) {
        return Constants.SERVICES_CONSOLE + apiName;
    }

    private static JSONObject fireRequest(final Context context, final InnerCircleRequest request) {
        return fireRequest(context, request, null);
    }

    private static JSONObject fireRequest(final Context context, final InnerCircleRequest request, final Uri uri) {
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            final String url = urlBuilder(request.getAPI());
            Log.v(TAG, "url: " + url);
            final HttpPost httpPost = new HttpPost(url);

            /*
            final List<NameValuePair> params = new LinkedList<NameValuePair>();
            for (Map.Entry<String,String> entry : request.getNameValuePairs().entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params));*/

            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            for (Map.Entry<String,String> entry : request.getNameValuePairs().entrySet()) {
                builder.addPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.MULTIPART_FORM_DATA));
            }
            if (null != uri) {
                builder.addPart(Constants.FILE, new FileBody(new File(uri.getPath())));
            }
            httpPost.setEntity(builder.build());

            final HttpResponse response = httpClient.execute(httpPost);

            final StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();

                final String responseString = out.toString();
                final JSONObject responseJSON = new JSONObject(responseString);
                if (InnerCircleResponse.Status.TOKEN_EXPIRE_ERROR ==
                        InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS))) {
                    Log.v(TAG, "accessToken has expired, refresh token now...");

                    final InnerCircleToken token = SharedPreferencesUtils.getTokenFromPreferences(context);
                    final InnerCircleRequest refreshRequest = (new InnerCircleRequest.Builder())
                            .setAPI(Constants.REFRESH_ACCESS_TOKEN_API)
                            .setNameValuePair(Constants.UID, token.getUid())
                            .setNameValuePair(Constants.REFRESH_TOKEN, token.getRefreshToken())
                            .build();
                    final InnerCircleResponse refreshResponse = refreshAccessTokenRequest(context, refreshRequest);

                    if (InnerCircleResponse.Status.SUCCESS == refreshResponse.getStatus()) {
                        Log.v(TAG, "accessToken has been successfully refreshed, making a second call for " + request.getAPI());

                        // the refreshed token has already been saved to SharedPreferences in the private refreshAccessTokenRequest call
                        final InnerCircleToken refreshedToken = (InnerCircleToken) refreshResponse.getData();
                        request.setNameValuePair(Constants.ACCESS_TOKEN, refreshedToken.getAccessToken());
                        return fireRequest(context, request, uri);
                    }
                    // if refresh token call fails, return empty json which will lead to a failed response
                    return null;
                }
                return responseJSON;
            } else {
                Log.e(TAG, request.getAPI() + " service request didn't receive SC_OK status, closing connection...");
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static InnerCircleResponse registerRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);
        return parseJSONToToken(context, responseJSON);
    }

    public static InnerCircleResponse loginRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);
        return parseJSONToToken(context, responseJSON);
    }

    public static InnerCircleResponse setGenderRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);
        return parseJSONToUser(context, responseJSON);
    }

    public static InnerCircleResponse setUsernameRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);
        return parseJSONToUser(context, responseJSON);
    }

    public static InnerCircleResponse getUserAccountsRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);
        return parseJSONToUsers(context, responseJSON);
    }

    public static InnerCircleResponse getCounterRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);

        final InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }

        InnerCircleResponse.Status status;
        try {
            status = InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS));

            if (status == InnerCircleResponse.Status.SUCCESS) {
                final JSONObject dataJSON = responseJSON.getJSONObject(Constants.DATA);
                final InnerCircleToken token = (new InnerCircleToken.Builder())
                        .setUid(dataJSON.getString(Constants.UID))
                        .setAccessToken(dataJSON.getString(Constants.ACCESS_TOKEN))
                        .setRefreshToken(dataJSON.getString(Constants.REFRESH_TOKEN))
                        .setTimestamp(dataJSON.getLong(Constants.TIMESTAMP))
                        .build();
                response.setData(token);
            }
            response.setStatus(status);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            status = InnerCircleResponse.Status.FAILED;
            response.setStatus(status);
            return response;
        }
    }

    public static InnerCircleResponse fileUploadRequest(final Context context, final InnerCircleRequest request, final Uri uri) {
        final JSONObject responseJSON = fireRequest(context, request, uri);
        final InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }
        try {
            response.setStatus(InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS)));
            return response;
        } catch (JSONException e) {
            Log.v(TAG, e.toString());
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }
    }

    private static InnerCircleResponse refreshAccessTokenRequest(final Context context, final InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(context, request);

        final InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }

        InnerCircleResponse.Status status;
        try {
            status = InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS));
            if (status == InnerCircleResponse.Status.SUCCESS) {
                final JSONObject dataJSON = responseJSON.getJSONObject(Constants.DATA);
                final String accessToken = dataJSON.getString(Constants.ACCESS_TOKEN);

                final InnerCircleToken token = SharedPreferencesUtils.getTokenFromPreferences(context);
                token.setAccessToken(accessToken);
                SharedPreferencesUtils.saveTokenToPreferences(context, token);

                response.setData(token);
            }
            response.setStatus(status);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            status = InnerCircleResponse.Status.FAILED;
            response.setStatus(status);
            return response;
        }
    }

    private static InnerCircleResponse parseJSONToToken(final Context context, final JSONObject responseJSON) {
        final InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }

        InnerCircleResponse.Status status;
        try {
            status = InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS));

            if (status == InnerCircleResponse.Status.SUCCESS) {
                final JSONObject dataJSON = responseJSON.getJSONObject(Constants.DATA);
                final InnerCircleToken token = (new InnerCircleToken.Builder())
                        .setUid(dataJSON.getString(Constants.UID))
                        .setAccessToken(dataJSON.getString(Constants.ACCESS_TOKEN))
                        .setRefreshToken(dataJSON.getString(Constants.REFRESH_TOKEN))
                        .setTimestamp(dataJSON.getLong(Constants.TIMESTAMP))
                        .build();
                response.setData(token);
            }
            response.setStatus(status);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            status = InnerCircleResponse.Status.FAILED;
            response.setStatus(status);
            return response;
        }
    }

    private static InnerCircleResponse parseJSONToUsers(final Context context, final JSONObject responseJSON) {
        final InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }

        InnerCircleResponse.Status status;
        try {
            status = InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS));

            if (status == InnerCircleResponse.Status.SUCCESS) {
                final JSONObject dataJSON = responseJSON.getJSONObject(Constants.DATA);
                final String uid = dataJSON.getString(Constants.UID);

                final JSONArray userArray = dataJSON.getJSONArray(Constants.USER_LIST);
                final List<InnerCircleUser> userList = new LinkedList<InnerCircleUser>();
                for (int i = 0; i < userArray.length(); i++) {
                    JSONObject userObject = userArray.getJSONObject(i);
                    InnerCircleUser user = (new InnerCircleUser.Builder())
                            .setId(userObject.getString(Constants.UID))
                            .setEmail(userObject.getString(Constants.EMAIL))
                            .setPassword(userObject.getString(Constants.PASSWORD))
                            .setGender(userObject.getString(Constants.GENDER).charAt(0))
                            .setVIPCode(userObject.getString(Constants.VIP_CODE))
                            .build();
                    if (!JSONObject.NULL.equals(userObject.get(Constants.USERNAME))) {
                        user.setUsername(userObject.getString(Constants.USERNAME));
                    }
                    userList.add(user);
                }
                final InnerCircleUserList users = new InnerCircleUserList();
                users.setUid(uid);
                users.setUserList(userList);
                response.setData(users);
            }
            response.setStatus(status);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            status = InnerCircleResponse.Status.FAILED;
            response.setStatus(status);
            return response;
        }
    }

    private static InnerCircleResponse parseJSONToUser(final Context context, final JSONObject responseJSON) {
        final InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }

        InnerCircleResponse.Status status;
        try {
            status = InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS));

            if (status == InnerCircleResponse.Status.SUCCESS) {
                final JSONObject dataJSON = responseJSON.getJSONObject(Constants.DATA);
                final InnerCircleUser user = (new InnerCircleUser.Builder())
                        .setId(dataJSON.getString(Constants.UID))
                        .setEmail(dataJSON.getString(Constants.EMAIL))
                        .setPassword(dataJSON.getString(Constants.PASSWORD))
                        .setGender(dataJSON.getString(Constants.GENDER).charAt(0))
                        .setVIPCode(dataJSON.getString(Constants.VIP_CODE))
                        .build();
                if (!JSONObject.NULL.equals(dataJSON.get(Constants.USERNAME))) {
                    user.setUsername(dataJSON.getString(Constants.USERNAME));
                }
                response.setData(user);
            }
            response.setStatus(status);
            return response;
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            status = InnerCircleResponse.Status.FAILED;
            response.setStatus(status);
            return response;
        }
    }
}
