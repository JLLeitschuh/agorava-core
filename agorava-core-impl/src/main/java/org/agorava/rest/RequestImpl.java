/*
 * Copyright 2013 Agorava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.agorava.rest;

import org.agorava.api.exception.AgoravaException;
import org.agorava.api.exception.ConnectionException;
import org.agorava.api.rest.Request;
import org.agorava.api.rest.RequestTuner;
import org.agorava.api.rest.Verb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents an HTTP Request object
 *
 * @author Pablo Fernandez
 */
public class RequestImpl implements Request {
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String CONTENT_TYPE = "Content-Type";

    private static RequestTuner NOOP = new RequestTuner() {
        @Override
        public void tune(Request _) {
        }
    };

    private String url;

    private Verb verb;

    private HttpParametersImpl querystringParams;

    private HttpParametersImpl bodyParams;

    private Map<String, String> headers;

    private String payload = null;

    protected HttpURLConnection getConnection() {
        return connection;
    }

    private HttpURLConnection connection;

    private String charset;

    private byte[] bytePayload = null;

    private boolean connectionKeepAlive = false;

    private Long connectTimeout = null;

    private Long readTimeout = null;

    /**
     * Creates a new Http Request
     *
     * @param verb Http Verb (GET, POST, etc)
     * @param url  url with optional querystring parameters.
     */
    public RequestImpl(Verb verb, String url) {
        this.verb = verb;
        this.url = url;
        this.querystringParams = new HttpParametersImpl();
        this.bodyParams = new HttpParametersImpl();
        this.headers = new HashMap<String, String>();
    }

    @Override
    public ResponseImpl send(RequestTuner tuner) {
        try {
            createConnection();
            return doSend(tuner);
        } catch (Exception e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public ResponseImpl send() {
        return send(NOOP);
    }

    private void createConnection() throws IOException {
        String completeUrl = getCompleteUrl();
        if (connection == null) {
            System.setProperty("http.keepAlive", connectionKeepAlive ? "true" : "false");
            connection = (HttpURLConnection) new URL(completeUrl).openConnection();
        }
    }

    @Override
    public String getCompleteUrl() {
        return querystringParams.asUrl(url);
    }

    ResponseImpl doSend(RequestTuner tuner) throws IOException {
        connection.setRequestMethod(this.verb.name());
        if (connectTimeout != null) {
            connection.setConnectTimeout(connectTimeout.intValue());
        }
        if (readTimeout != null) {
            connection.setReadTimeout(readTimeout.intValue());
        }
        addHeaders(connection);
        if (verb.equals(Verb.PUT) || verb.equals(Verb.POST)) {
            addBody(connection, getByteBodyContents());
        }
        tuner.tune(this);
        return new ResponseImpl(this);
    }

    void addHeaders(HttpURLConnection conn) {
        for (String key : headers.keySet())
            conn.setRequestProperty(key, headers.get(key));
    }

    void addBody(HttpURLConnection conn, byte[] content) throws IOException {
        conn.setRequestProperty(CONTENT_LENGTH, String.valueOf(content.length));

        // Set default content type if none is set.
        if (conn.getRequestProperty(CONTENT_TYPE) == null) {
            conn.setRequestProperty(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        }
        conn.setDoOutput(true);
        conn.getOutputStream().write(content);
    }

    @Override
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    @Override
    public void addBodyParameter(String key, String value) {
        this.bodyParams.add(key, value);
    }

    @Override
    public void addQuerystringParameter(String key, String value) {
        this.querystringParams.add(key, value);
    }

    @Override
    public void addPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public void addPayload(byte[] payload) {
        this.bytePayload = payload;
    }

    @Override
    public HttpParametersImpl getQueryStringParams() {
        try {
            HttpParametersImpl result = new HttpParametersImpl();
            String queryString = new URL(url).getQuery();
            result.addQuerystring(queryString);
            result.addAll(querystringParams);
            return result;
        } catch (MalformedURLException mue) {
            throw new AgoravaException("Malformed URL : " + url, mue);
        }
    }

    @Override
    public HttpParametersImpl getBodyParams() {
        return bodyParams;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getSanitizedUrl() {
        return url.replaceAll("\\?.*", "").replace("\\:\\d{4}", "");
    }

    @Override
    public String getBodyContents() {
        try {
            return new String(getByteBodyContents(), getCharset());
        } catch (UnsupportedEncodingException uee) {
            throw new AgoravaException("Unsupported Charset: " + charset, uee);
        }
    }

    byte[] getByteBodyContents() {
        if (bytePayload != null) return bytePayload;
        String body = (payload != null) ? payload : bodyParams.asFormUrlEncodedString();
        try {
            return body.getBytes(getCharset());
        } catch (UnsupportedEncodingException uee) {
            throw new AgoravaException("Unsupported Charset: " + getCharset(), uee);
        }
    }

    @Override
    public Verb getVerb() {
        return verb;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getCharset() {
        return charset == null ? Charset.defaultCharset().name() : charset;
    }

    @Override
    public void setConnectTimeout(int duration, TimeUnit unit) {
        this.connectTimeout = unit.toMillis(duration);
    }

    @Override
    public void setReadTimeout(int duration, TimeUnit unit) {
        this.readTimeout = unit.toMillis(duration);
    }

    @Override
    public void setCharset(String charsetName) {
        this.charset = charsetName;
    }

    @Override
    public void setConnectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
    }

    /*
     * We need this in order to stub the connection object for test cases
     */
    void setConnection(HttpURLConnection connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return String.format("@Request(%s %s)", getVerb(), getUrl());
    }

    @Override
    public void addBodyParameters(Map<String, ?> toAdd) {
        for (String key : toAdd.keySet()) {
            addBodyParameter(key, toAdd.get(key).toString());

        }
    }
}
