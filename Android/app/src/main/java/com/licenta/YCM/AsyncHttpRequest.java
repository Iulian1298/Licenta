package com.licenta.YCM;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncHttpRequest extends AsyncTask<String, Void, Void> {


    public interface Listener {
        void onResult(String result);
    }

    private Listener mListener;
    private StringBuilder responseOutput;

    public AsyncHttpRequest(Listener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            JSONObject jsonParam = new JSONObject();
            try {
                for (int i = 2; i < params.length; i += 2)
                    jsonParam.put(params[i], params[i + 1]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            String body = jsonParam.toString();

            URL url = new URL(params[1]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(params[0]);
            connection.setChunkedStreamingMode(0);

            if (params[0].equals("POST") || params[0].equals("PUT")) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(body.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.connect();

                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                os.write(body.getBytes());
                os.flush();
            }

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
            Log.e("AsyncHttpRequest", "doInBackground: error retrieving data from server (content length)");
            e.printStackTrace();
            Log.e("AsyncHttpRequest", "doInBackground: content: " + responseOutput);
            responseOutput.append("}");
            //responseOutput = new StringBuilder("{code : 99}");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onResult(responseOutput.toString());
    }
}