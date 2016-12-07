package net.acidforge.http;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/***
 * Represents an Async HttpRequest by providing an @see {@link HttpResponse}.
 * You may also implement your own @see {@link HttpResponse} for custom operations.
 */
public abstract class HttpRequest<T extends HttpResponse> extends AsyncTask<String, Void, T> {
    private static final String USER_AGENT = "AcidForge.Http/1.0";
    private Class<T> tClass;
    private HttpClientCookieStore cookieStore;
    private String address;
    private HttpMethod method = HttpMethod.GET;
    private int connectionTimeout = HttpClient.getDefaultConnectionTimeout();
    private int readTimeout = HttpClient.getDefaultReadTimeout();
    private SSLContext sslContext;
    private String contentType = "text/plain; charset=us-ascii";
    private byte[] requestContent;

    /***
     * Constructs an HttpRequest for the desired @see {@link HttpResponse} implementation.
     * @param tClass The default @see {@link HttpResponse} or a subclass.
     * @param address If is set @see {@link HttpClient#setBaseUrl(URL)}, the address will be started
     *                with the informed base address. Otherwise a valid @see{@link URL} must be informed
     */
    public HttpRequest(Class<T> tClass, String address) {
        this.tClass = tClass;
        this.address = address;
        cookieStore = new HttpClientCookieStore(HttpClient.getContext());
    }

    /***
     * Constructs an HttpRequest for the desired @see {@link HttpResponse} implementation
     * on the provided @see{@link URL} by providing a @see{@link HttpMethod} and results
     * in a plan text response.
     * @param tClass The default @see {@link HttpResponse} or a subclass.
     * @param address If is set @see {@link HttpClient#setBaseUrl(URL)}, the address will be started
     *                with the informed base address. Otherwise a valid @see{@link URL} must be informed
     * @param method
     */
    public HttpRequest(Class<T> tClass, String address, HttpMethod method) {
        this(tClass, address);
        this.method = method;
    }
    /***
     * Constructs an HttpRequest for the desired @see {@link HttpResponse} implementation
     * on the provided address by providing a @see{@link HttpMethod} and a contentType
     * @param tClass The default @see {@link HttpResponse} or a subclass.
     * @param address If is set @see {@link HttpClient#setBaseUrl(URL)}, the address will be started
     *                with the informed base address. Otherwise a valid @see{@link URL} must be informed
     * @param method
     * @param contentType
     */
    public HttpRequest(Class<T> tClass, String address, HttpMethod method, String contentType) {
        this(tClass, address);
        this.method = method;
        this.contentType = contentType;
    }

    /***
     * Constructs a default @see {@link HttpMethod#POST} request for a desired content.
     * A contentType must be provided in order to receive the correct @see {@link HttpResponse}.
     * @param tClass The default @see {@link HttpResponse} or a subclass.
     * @param address If is set @see {@link HttpClient#setBaseUrl(URL)}, the address will be started
     *                with the informed base address. Otherwise a valid @see{@link URL} must be informed
     * @param contentType
     * @param requestContent
     */
    public HttpRequest(Class<T> tClass, String address, String contentType, byte[] requestContent) {
        this(tClass, address);
        this.method = HttpMethod.POST;
        this.contentType = contentType;
        this.requestContent = requestContent;
    }

    @Override
    protected T doInBackground(String... params) {
        String protocol, host;
        URL url;
        int port;
        T response = null;
        HttpURLConnection connection;
        String wrappedAddress;
        InputStream inputStream;
        ByteArrayOutputStream readStream;
        //<editor-fold desc="Instantiate the Response">
        try {
            response = tClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return response;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return response;
        }
        //</editor-fold>

        //<editor-fold desc="Checks if a request URL was defined">
        if (params == null || params.length == 0) {
            Exception exception = new ArrayIndexOutOfBoundsException(HttpClient.getContext().getString(R.string.exception_undefined_address));
            response.setException(exception);
            return response;
        }
        //</editor-fold>

        //<editor-fold desc="Defines the url to the request">
        if(HttpClient.hasBaseURL()){
            try{
                URL base = HttpClient.getBaseUrl();
                protocol = base.getProtocol();
                host = base.getHost();
                port = base.getPort();
                if (port == -1)
                    port = 80;
                wrappedAddress = String.format("%s://%s:%d", protocol, host, port, params[0]);
                url = new URL(wrappedAddress);
            }catch (MalformedURLException e){
                response.setException(e);
                return response;
            }
        }
        else{
            try{
                url = new URL(address);
            }catch (MalformedURLException e){
                response.setException(e);
                return response;
            }
        }
        //</editor-fold>

        //<editor-fold desc="Opens the connection and, if needed, defines the SSL factory>
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            response.setException(e);
            return response;
        }
        if (sslContext != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }
        //</editor-fold>

        //<editor-fold desc="Defines the User-Agent, the Content-Type, timeout operations and method
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Content-Type", contentType);
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);

        try {
            connection.setRequestMethod(method.toString());
        } catch (ProtocolException e) {
            response.setException(e);
            return response;
        }
        //</editor-fold>

        //<editor-fold desc="Gets the response headers and, if is set, it's cookies"
        try {
            response.setResponseHeaders(connection.getHeaderFields());
            attachCookies(connection);
            readCookies(connection);
        } catch (IOException e) {
            response.setException(e);
            return response;
        }
        //</editor-fold>

        connection.setDoOutput(requestContent != null && requestContent.length > 0);

        try {
            connection.connect();
        } catch (IOException e) {
            response.setException(e);
            return response;
        }

        try {
            response.setResponseCode(connection.getResponseCode());
        } catch (IOException e) {
            response.setException(e);
            return response;
        }

        if (connection.getDoOutput()) {
            try {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestContent);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        readConnectionStream(response, connection);
        connection.disconnect();
        return response;
    }

    private void readConnectionStream(T response, HttpURLConnection connection) {
        InputStream inputStream;
        ByteArrayOutputStream readStream;
        if (response.getResponseCode() == 200) {
            try {
                inputStream = connection.getInputStream();
                readStream = new ByteArrayOutputStream();
                int read = inputStream.read();
                while (read != -1) {
                    readStream.write(read);
                    read = inputStream.read();
                }
                inputStream.close();
                readStream.flush();
                response.setContent(readStream.toByteArray());
                readStream.close();
            } catch (IOException e) {
                response.setException(e);
            }
        } else if (response.getResponseCode() >= 400 && response.getResponseCode() <= 599) {
            try {
                inputStream = connection.getErrorStream();
                readStream = new ByteArrayOutputStream();
                int read = inputStream.read();
                while (read != -1) {
                    readStream.write(read);
                    read = inputStream.read();
                }
                inputStream.close();
                readStream.flush();
                response.setContent(readStream.toByteArray());
                readStream.close();
            } catch (IOException e) {
                response.setException(e);
            }
        }
    }

    private void attachCookies(HttpURLConnection httpURLConnection) throws IOException {
        List<HttpCookie> cookieList = cookieStore.getCookies();
        if (cookieList != null && cookieList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (HttpCookie cookie : cookieList) {
                sb.append(cookie.toString());
                sb.append(";");
            }
            httpURLConnection.setRequestProperty("Cookie", sb.toString());
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
                            cookieStore.add(null, cookie);
                        }
                    }
                } catch (NullPointerException e) {
                    continue;
                }
            }
        }
    }

    /***
     * To be called on the main thread, it brings the desired @see{@link HttpResponse} or
     * a subclass that extends it.
     * @param response A subclass of @see{@link HttpResponse}
     */
    @Override
    protected abstract void onPostExecute(T response);
}
