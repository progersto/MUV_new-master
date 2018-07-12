package com.muvit.passenger.AsyncTask;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by prats on 3/16/2015.
 */
public class GetAsyncTask extends AsyncTask<Void, Void, String> {

    private String url;
    private OnAsyncResult onAsyncResult;
    private Boolean resultFlag;
    private Uri.Builder builder;

    public GetAsyncTask(String url, OnAsyncResult listner, Uri.Builder builder) {
        this.url = url;
        this.onAsyncResult = listner;
        resultFlag = false;
        this.builder = builder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        String final_url = url.replaceAll(" ", "%20");
        try {
            URL url = new URL(final_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

           /* Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("firstParam", paramValue1)
                    .appendQueryParameter("secondParam", paramValue2)
                    .appendQueryParameter("thirdParam", paramValue3);*/
            String query = builder.build().getEncodedQuery();
            //Log.e("OFFLINE QUERY IS : ", query);
            if(!TextUtils.isEmpty(query)){
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
            }


            httpURLConnection.connect();
            Log.e("Response Code:", "Response Code: " + httpURLConnection.getResponseCode());
            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
            //String response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            String response = getStringFromInputStream(in);
            Log.e("Response : ", response);
            resultFlag = true;
            return response;
        } catch (SocketTimeoutException e1) {
            resultFlag = false;
            return "Connection has timed out. Do you want to retry?";

        } catch (Exception e) {
            e.printStackTrace();
            resultFlag = false;
            return "Unexpected error has occurred";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (resultFlag) {
            if (onAsyncResult != null) {
                onAsyncResult.OnSuccess(result);
            }
        } else {
            if (onAsyncResult != null) {
                onAsyncResult.OnFailure(result);
            }
        }
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer)))
            writer.write(buffer, 0, n);
        return writer.toString();
    }
}