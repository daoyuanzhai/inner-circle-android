package com.innercircle.android.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.innercircle.android.model.InnerCircleRequest;
import com.innercircle.android.model.InnerCircleResponse;
import com.innercircle.android.model.InnerCircleToken;
import com.innercircle.android.utils.Constants;

public class HttpRequestUtils {
    private static final String TAG = HttpRequestUtils.class.getSimpleName();
    private static final String SERVICES_CONSOLE = "http://192.168.0.5:8080/ServicesConsole/";

    private HttpRequestUtils () {
    }

    private static String urlBuilder(final String apiName) {
        return SERVICES_CONSOLE + apiName;
    }

    private static JSONObject fireRequest(final InnerCircleRequest request) {
        try {
            final HttpClient httpClient = new DefaultHttpClient();
            final String url = urlBuilder(request.getAPI());
            Log.v(TAG, "url: " + url);
            final HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new UrlEncodedFormEntity(request.getNameValuePairs()));

            final HttpResponse response = httpClient.execute(httpPost);

            final StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();

                final String responseString = out.toString();
                return new JSONObject(responseString);
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

    public static InnerCircleResponse registerRequest(InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(request);
        return parseJSONToToken(responseJSON);
    }

    public static InnerCircleResponse loginRequest(InnerCircleRequest request) {
        final JSONObject responseJSON = fireRequest(request);
        return parseJSONToToken(responseJSON);
    }

    private static InnerCircleResponse parseJSONToToken(JSONObject responseJSON) {
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
            status = InnerCircleResponse.Status.JSON_PARSE_ERROR;
            response.setStatus(status);
            return response;
        }
    }
}
