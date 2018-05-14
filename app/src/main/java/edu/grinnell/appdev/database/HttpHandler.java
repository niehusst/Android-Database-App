package edu.grinnell.appdev.database;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Liam2 on 5/2/18.
 * Code from stackoverflow user Alex Jolig at:
 * https://stackoverflow.com/questions/13196234/simple-parse-json-from-url-on-android-and-display-in-listview
 */

public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    //empty constructor
    public HttpHandler() {}

    /**
     * @breif
     *
     * @param reqUrl - the url that data will be taken from
     * @return
     */
    public String makeServiceCall(String reqUrl) {
        String response = null;

        //try connecting to the input url
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    /**
     * @breif turns an InputStream into a string using stringbuilder
     *
     * @param i_stream - a InputStream, JSON file being read from
     * @return
     */
    private String convertStreamToString(InputStream i_stream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(i_stream));
        StringBuilder strBuilder = new StringBuilder();
        //temp data holder
        String line;
        try {
            //try building the string from i_stream
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //cleanup
                i_stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return strBuilder.toString();
    }

}
