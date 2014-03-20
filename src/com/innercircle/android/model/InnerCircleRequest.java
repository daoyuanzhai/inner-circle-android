package com.innercircle.android.model;

import java.util.HashMap;
import java.util.Map;

public class InnerCircleRequest {
    private String mAPI;
    private Map<String, String> mNameValuePairs;

    public void setAPI(final String API) {
        this.mAPI = API;
    }

    public String getAPI() {
        return this.mAPI;
    }

    public void setNameValuePair(final String name, final String value) {
        this.mNameValuePairs.put(name, value);
    }

    public Map<String, String> getNameValuePairs() {
        return this.mNameValuePairs;
    }

    private InnerCircleRequest(final Builder builder) {
        this.mAPI = builder.bAPI;
        this.mNameValuePairs = builder.bNameValuePairs;
    }

    public static class Builder {
        private String bAPI;
        private final Map<String, String> bNameValuePairs = new HashMap<String, String>();

        public InnerCircleRequest build() {
            return new InnerCircleRequest(this);
        }

        public Builder setAPI(final String api) {
            this.bAPI = api;
            return this;
        }

        public Builder setNameValuePair(final String name, final String value) {
            this.bNameValuePairs.put(name, value);
            return this;
        }
    }
}
