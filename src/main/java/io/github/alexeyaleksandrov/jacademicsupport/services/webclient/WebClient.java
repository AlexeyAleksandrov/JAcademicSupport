package io.github.alexeyaleksandrov.jacademicsupport.services.webclient;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class WebClient {
    public String get(String uri)
    {
        try {
            URL url = new URL(uri);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            httpURLConnection.disconnect();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
