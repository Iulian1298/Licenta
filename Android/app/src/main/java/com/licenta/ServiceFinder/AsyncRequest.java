package com.licenta.ServiceFinder;


import android.os.AsyncTask;
import android.util.Log;

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
    private StringBuilder stringBuilder;
    private SharedPreferencesManager mPreferencesManager;

    public AsyncRequest(SharedPreferencesManager preferencesManager, Listener listener) {
        mPreferencesManager = preferencesManager;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            URL url = new URL(params[1]);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(params[0]);
            //connection.setChunkedStreamingMode(0);
            httpURLConnection.setRequestProperty("Authorization", mPreferencesManager.getToken());


            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    httpURLConnection.getInputStream()));
            String line = "";
            stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AsyncHttpRequest", "doInBackground: content: " + stringBuilder);
            if (stringBuilder != null) {
                stringBuilder.append("}");
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onResult(stringBuilder.toString());
    }
}