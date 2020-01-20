package com.licenta.YCM;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncRequest extends AsyncTask<String, Void, Void> {


    public interface Listener {
        void onResult(String result);
    }

    private Listener mListener;
    private StringBuilder responseOutput;
    private SharedPreferencesManager mPreferencesManager;

    public AsyncRequest(SharedPreferencesManager preferencesManager, Listener listener) {
        mPreferencesManager = preferencesManager;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            URL url = new URL(params[1]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(params[0]);
            //connection.setChunkedStreamingMode(0);
            connection.setRequestProperty("Authorization", mPreferencesManager.getToken());


            BufferedReader br = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = "";
            responseOutput = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
            br.close();
            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AsyncHttpRequest", "doInBackground: content: " + responseOutput);
            if (responseOutput != null) {
                responseOutput.append("}");
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onResult(responseOutput.toString());
    }
}