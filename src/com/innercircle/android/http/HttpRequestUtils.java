package com.innercircle.android.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.utils.Constants;
import com.innercircle.android.utils.Utils;

public class HttpRequestUtils {
    private static final String TAG = HttpRequestUtils.class.getSimpleName();

    private HttpRequestUtils () {
    }

    private static String urlBuilder(final String apiName) {
        return Constants.SERVICES_CONSOLE + apiName;
    }

    private static JSONObject fireRequest(final Context context, final InnerCircleRequest request) {
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            final String url = urlBuilder(request.getAPI());
            Log.v(TAG, "url: " + url);
            final HttpPost httpPost = new HttpPost(url);

            final List<NameValuePair> params = new LinkedList<NameValuePair>();
            for (Map.Entry<String,String> entry : request.getNameValuePairs().entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params));

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

                    final InnerCircleToken token = Utils.getTokenFromPreferences(context);
                    final InnerCircleRequest refreshRequest = (new InnerCircleRequest.Builder())
                            .setAPI(Constants.REFRESH_ACCESS_TOKEN_API)
                            .setNameValuePair(Constants.UID, token.getUid())
                            .setNameValuePair(Constants.REFRESH_TOKEN, token.getRefreshToken())
                            .build();
                    final InnerCircleResponse refreshResponse = refreshAccessTokenRequest(context, refreshRequest);

                    if (InnerCircleResponse.Status.SUCCESS == refreshResponse.getStatus()) {
                        Log.v(TAG, "accessToken has been successfully refreshed, making a second call for " + request.getAPI());

                        // saving the new accessToken
                        final InnerCircleToken refreshedToken = (InnerCircleToken) refreshResponse.getData();
                        token.setAccessToken(refreshedToken.getAccessToken());
                        Utils.saveTokenToPreferences(context, token);

                        request.setNameValuePair(Constants.ACCESS_TOKEN, token.getAccessToken());
                        return fireRequest(context, request);
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

        InnerCircleResponse response = new InnerCircleResponse();
        if (null == responseJSON) {
            response.setStatus(InnerCircleResponse.Status.FAILED);
            return response;
        }
        InnerCircleResponse.Status status;
        try {
            final InnerCircleToken token = Utils.getTokenFromPreferences(context);

            status = InnerCircleResponse.Status.valueOf(responseJSON.getString(Constants.STATUS));
            if (status == InnerCircleResponse.Status.SUCCESS) {
                final JSONObject dataJSON = responseJSON.getJSONObject(Constants.DATA);
                final char gender = dataJSON.getString(Constants.GENDER).charAt(0);

                token.setGender(gender);
                Utils.saveTokenToPreferences(context, token);
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

                final InnerCircleToken token = Utils.getTokenFromPreferences(context);
                token.setAccessToken(accessToken);

                Utils.saveTokenToPreferences(context, token);
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
                        .setGender(dataJSON.getString(Constants.GENDER).charAt(0))
                        .setTimestamp(dataJSON.getLong(Constants.TIMESTAMP))
                        .build();
                Utils.saveTokenToPreferences(context, token);
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
}
