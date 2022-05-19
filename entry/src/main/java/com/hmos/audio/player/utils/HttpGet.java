package com.hmos.audio.player.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpGet {

    private HttpURLConnection httpConn;
    private String charset;
    private final int CONNECT_TIMEOUT = 15000;
    private final int READ_TIMEOUT = 60000;

    public HttpGet(String requestURL, String charset, Map<String, String> headers) throws IOException {

        this.charset = charset;
        URL url = new URL(requestURL);

        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setConnectTimeout(CONNECT_TIMEOUT);
        httpConn.setReadTimeout(READ_TIMEOUT);
        httpConn.setRequestMethod("GET");

        if (headers != null && headers.size() > 0) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                httpConn.setRequestProperty(key, value);
            }
        }
    }

    public String finish() throws IOException {
        String response = "";

        int status = httpConn.getResponseCode();

        if (status == HttpURLConnection.HTTP_OK) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;

            while ((length = httpConn.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            response = result.toString(this.charset);
            httpConn.disconnect();
        } else {
            throw new IOException(status + "");
        }
        return response;
    }
}