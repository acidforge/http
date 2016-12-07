package net.acidforge.http;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.net.MalformedURLException;
import java.net.URL;

/***
 * The default HttpClient for the application.
 */
public class HttpClient extends Application {

    private static Context CONTEXT;
    private static SharedPreferences SHARED_PREFERENCES;




    @Override
    public void onCreate(){
        super.onCreate();
        HttpClient.CONTEXT = getApplicationContext();
        SHARED_PREFERENCES = CONTEXT.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
    }
    /***
     * Returns the context of the @see {@link HttpClient}
     * @return
     */
    public static Context getContext(){
        return CONTEXT;
    }

    public static boolean hasBaseURL(){
        return SHARED_PREFERENCES.getString("base_url", null) != null;
    }

    public static int getDefaultConnectionTimeout() {
        return SHARED_PREFERENCES.getInt("default_connection_timeout", 15000);
    }

    public static void setDefaultConnectionTimeout(int timeout){
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        editor.putInt("default_connection_timeout", timeout);
        editor.commit();
    }

    public static int getDefaultReadTimeout() {
        return SHARED_PREFERENCES.getInt("default_read_timeout", 30000);
    }

    public static void setDefaultReadTimeout(int timeout){
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        editor.putInt("default_read_timeout", timeout);
        editor.commit();
    }

    /***
     * Gets the base URL for the @see{@link HttpClient}
     * @return
     * @throws MalformedURLException
     */
    public static URL getBaseUrl() throws MalformedURLException{
        try{
            URL url = new URL(SHARED_PREFERENCES.getString("base_url", null));
            return url;
        }catch (MalformedURLException e){
            throw  e;
        }
    }

    /***
     * Sets the base URL for the @see{@link HttpClient}
     * @param url
     */
    public static void setBaseUrl(URL url){
        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        editor.putString("base_url", url.toString());
        editor.commit();
    }
}
