/*
 * Copyright (C) 2017 The Acidforge Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.acidforge.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/***
 * An enclosing HttpRequest to send and receive data through Http protocol.
 *
 */
public abstract class HttpRequest extends AsyncTask<Void, Integer, Integer> {

    /***
     * User agent defined in the http header request.
     */
    public static final String USER_AGENT = "AcidForge.Http/1.0";
    /***
     * The underlying connection.
     */
    protected HttpURLConnection urlConnection;
    /***
     * Context that called this request for component interaction.
     */
    protected Context context;
    /***
     * The SSL context, defined if a secure socket layer is enabled for the
     * request.
     */
    protected SSLContext sslContext;
    protected HttpCookieStore cookieStore;
    private String address;
    private MediaType requestMediaType = MediaType.JSON_UTF_8;
    private MediaType responseMediaType = MediaType.JSON_UTF_8;

    private int connectionTimeOut = 15000;
    private int readTimeOut = 60 * 3 * 1000;

    private String method = "get";
    private HttpMethod httpMethod;
    private URL url;

    /***
     * Creates a request for Json IO with the default {@link HttpMethod#GET} method.
     * @param context The Android @see{@link Context}, may be the application, a service or an activity context.
     * @param url An arbitrary @see{@link URL}
     */
    public HttpRequest(Context context, URL url) {
        this.context = context;
        this.url = url;
        this.httpMethod = HttpMethod.GET;
        this.cookieStore = new HttpCookieStore(context);
    }

    /***
     * Creates a request for Json IO with the specified {@link HttpMethod}
     * @param context The Android @see{@link Context}, may be the application, a service or an activity context.
     * @param url An arbitrary @see{@link URL}
     * @param httpMethod An @see{@link HttpMethod} for the underlying request. If the method requests an entity and it's
     *                   not passed away it'll raise an exception on the {@link HttpRequest#doInBackground(Void...)} method
     *                   and the request won't go any further.
     */
    public HttpRequest(Context context, URL url, HttpMethod httpMethod) {
        this(context, url);
        this.httpMethod = httpMethod;
    }

    /***
     * Creates a request for Json IO with the default {@link HttpMethod#GET} method.
     * @param context The Android @see{@link Context}, may be the application, a service or an activity context.
     * @param uriPart The sub-part of uri address, usually a subdirectory or domain. To use
     *                this constructor you must define a BASE_URL and a BASE_PORT
     *                for the context in use.
     */
    public HttpRequest(Context context, String uriPart) {
        this.context = context;
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String baseAddress = sharedPreferences.getString("BASE_URL", null);
            Integer port = sharedPreferences.getInt("BASE_PORT", 80);
            if (baseAddress == null)
                throw new UndefinedBaseUrlException();
            this.url = new URL(String.format(Locale.ENGLISH, "%s://%s:%d%s", "https", baseAddress, port, uriPart));
        } catch (Exception ex) {
            Log.d("HttpRequest", ex.getLocalizedMessage());
        }
        this.httpMethod = HttpMethod.GET;
        this.cookieStore = new HttpCookieStore(context);
    }

    /***
     * Creates a request for Json IO with the default {@link HttpMethod#GET} method.
     * @param context The Android @see{@link Context}, may be the application, a service or an activity context.
     * @param uriPart The sub-part of uri address, usually a subdirectory or domain. To use
     *                this constructor you must define a BASE_URL and a BASE_PORT
     *                for the context in use.
     */
    public HttpRequest(Context context, String uriPart, HttpMethod httpMethod) {
        this.context = context;
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String baseAddress = sharedPreferences.getString("BASE_URL", null);
            Integer port = sharedPreferences.getInt("BASE_PORT", 80);
            if (baseAddress == null)
                throw new UndefinedBaseUrlException();
            this.url = new URL(String.format(Locale.ENGLISH, "%s://%s:%d%s", "https", baseAddress, port, uriPart));
        } catch (Exception ex) {
            Log.d("HttpRequest", ex.getLocalizedMessage());
        }
        this.httpMethod = HttpMethod.GET;
        this.cookieStore = new HttpCookieStore(context);
    }

    /***
     * Overrides the default request media type.
     * @param requestType MimeType to be used upon the request
     */
    public void setRequestMediaType(MediaType requestType) {
        this.requestMediaType = requestType;

    }

    /***
     * Overrides the default response media type.
     * @param responseType MimeType to be used upon the response
     */
    public void setResponseMediaType(MediaType responseType) {
        this.responseMediaType = responseType;
    }

    /***
     * Overrides the default request time out that is 15seconds.
     * @param connectionTimeOut Integer representing time in millis.
     */
    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    /***
     * Overrides the default read time out that is 3m.
     * @param readTimeOut Integer representing time in millis.
     */
    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    /***
     * Defines if the connection will be encrypted by a SSL layer.
     * @see{@link SSLContext}
     * @param sslContext
     */
    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        //<editor-fold desc="Opens the underlying connection to start trading">
        try {
            if (url == null)
                throw new UndefinedUrlException();
            if (sslContext != null) {
                urlConnection = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }
        } catch (Exception e) {
            Log.e("HttpRequest", e.getLocalizedMessage(), e);
            return 0;
        }
        //</editor-fold>

        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
        urlConnection.setRequestProperty("Content-Type", requestMediaType.toString());
        urlConnection.setConnectTimeout(connectionTimeOut);
        urlConnection.setReadTimeout(readTimeOut);
        try {
            urlConnection.setRequestMethod(httpMethod.toString());
        } catch (ProtocolException e) {
            Log.e("HttpRequest", e.getLocalizedMessage(), e);
            return 0;
        } catch (Exception e) {
            Log.e("HttpRequest", e.getLocalizedMessage(), e);
            return 0;
        }
        int responseCode = 0;
        try {
            attachCookies(urlConnection);
            urlConnection.setDoOutput(httpMethod.equals(HttpMethod.POST));
            if (urlConnection.getDoOutput())
                requestStream(urlConnection.getOutputStream());
            responseCode = urlConnection.getResponseCode();
            readCookies(urlConnection);
            if (responseCode == 200)
                responseStream(urlConnection.getInputStream());
            else
                errorStream(urlConnection.getErrorStream());
        } catch (IOException e) {
            Log.e("HttpRequest", e.getLocalizedMessage(), e);
        } catch (Exception e) {
            Log.e("HttpRequest", e.getLocalizedMessage(), e);
        } finally {
            urlConnection.disconnect();
            return responseCode;
        }
    }

    private void attachCookies(HttpURLConnection httpURLConnection) {
        try {
            List<HttpCookie> cookieList = cookieStore.getCookies();
            if (cookieList != null && cookieList.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (HttpCookie cookie : cookieList) {
                    sb.append(cookie.toString());
                    sb.append(";");
                }
                httpURLConnection.setRequestProperty("Cookie", sb.toString());
            }
        } catch (Exception e) {
            Log.e("HttpRequest", e.getLocalizedMessage());
        }
    }

    private void readCookies(HttpURLConnection httpURLConnection) {
        List<HttpCookie> cookieList;
        List<String> cookies = httpURLConnection.getHeaderFields().get("Set-Cookie");
        if (cookies != null) {
            for (String cookieHeader : cookies) {
                try {
                    cookieList = HttpCookie.parse(cookieHeader);
                    if (cookieList != null) {
                        for (HttpCookie cookie : cookieList) {
                            cookieStore.add(httpURLConnection.getURL().toURI(), cookie);
                        }
                    }
                } catch (NullPointerException e) {
                    Log.e("HttpRequest", e.getLocalizedMessage(), e);
                    continue;
                } catch (URISyntaxException e) {
                    Log.e("HttpRequest", e.getLocalizedMessage(), e);
                    continue;
                } catch (Exception e) {
                    Log.e("HttpRequest", e.getLocalizedMessage(), e);
                    continue;
                }
            }
        }
    }

    /***
     * The request output stream. Must be overriden for
     * @param stream
     * @throws IOException
     */
    public void requestStream(OutputStream stream) throws Exception {
        if (httpMethod.equals(HttpMethod.POST))
            throw new NotOverridenException();
    }

    /***
     * Reads the content from the connection asynchronously if there are no errors.
     * @param stream The connection response stream if the connection is successfull.
     */
    public abstract void responseStream(InputStream stream) throws Exception;

    /***
     * Reads an error stream from the connection asynchronously if there are errors.
     * Override this if you intend to capture the @see{@link HttpURLConnection#getErrorStream()}.
     * @param stream The error stream from the response.
     * @throws IOException
     */
    public void errorStream(InputStream stream) throws Exception {

    }

    /***
     * Disposes the resources and set back to ui interation.
     * @param result The http status code. Usually 200 is ok, all others must throw exceptions.
     */
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Log.d("HttpRequest", String.format("Reponse code %d", result));
    }
}
