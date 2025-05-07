package com.ADO.ADOPersonal.util;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

public class Util {

    public Util() throws IOException, InterruptedException {
    }

    public static LocalDate getDateFromString(String dateAsString) {

        LocalDate date = null;

        if (StringUtils.hasLength(dateAsString)) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yy", Locale.ENGLISH);
            date = LocalDate.parse(dateAsString.trim(), formatter);

        }
        return date;
    }

    public static StringBuffer openConnectionForPostRequestGraphDB(String url, String token, JSONObject object) throws Exception {


        url = url.contains(" ") ? url.replaceAll(" ", "%20") : url;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest =
                HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(object)))
                        .headers("Authorization", token, "Content-Type", "application/json").uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        StringBuffer resp = new StringBuffer();
        resp.append(response.body());
        return resp;
    }

    public static ResponseEntity<String> openConnectionForPutRequest(String url, String issueKey, String userName, String token, Map<String, Object> object) throws Exception {


        url = url.contains(" ") ? url.replaceAll(" ", "%20") : url;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(userName, token);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(object, headers);


        restTemplate.exchange(url, HttpMethod.PUT, request,String.class);


        return ResponseEntity.ok().body("response");
    }


    public static StringBuffer openConnection(String url, String token) throws Exception {
        url = url.contains(" ") ? url.replaceAll(" ", "%20") : url;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().GET().headers("Authorization", token, "Content-Type", "application/json").uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringBuffer resp = new StringBuffer();
        resp.append(response.body());
        return resp;

    }





    public static StringBuffer openConnectionBasic(String url, String username, String password) throws Exception {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);

        url = url.contains(" ") ? url.replaceAll(" ", "%20") : url;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().GET().header("Authorization", authHeader).header("Content-Type", "application/json").uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringBuffer resp = new StringBuffer();
        resp.append(response.body());
        return resp;
    }
}
