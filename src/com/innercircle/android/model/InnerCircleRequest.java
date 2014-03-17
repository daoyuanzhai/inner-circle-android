package com.innercircle.android.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class InnerCircleRequest {
    private String mAPI;
    private List<NameValuePair> mNameValuePairs;

    public String getAPI() {
        return this.mAPI;
    }

    public List<NameValuePair> getNameValuePairs() {
        return this.mNameValuePairs;
    }

    private InnerCircleRequest(final Builder builder) {
        this.mAPI = builder.bAPI;
        this.mNameValuePairs = builder.bNameValuePairs;
    }

    public static class Builder {
        private String bAPI;
        private final List<NameValuePair> bNameValuePairs = new ArrayList<NameValuePair>(2);

        public InnerCircleRequest build() {
            return new InnerCircleRequest(this);
        }

        public Builder setAPI(final String api) {
            this.bAPI = api;
            return this;
        }

        public Builder setNameValuePair(final String name, final String value) {
            this.bNameValuePairs.add(new BasicNameValuePair(name, value));
            return this;
        }
    }
}
