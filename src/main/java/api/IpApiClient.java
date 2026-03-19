package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IpApiClient {

    public static String[] getLocation() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/json/"))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.body());

            if (node.has("status") && node.get("status").asText().equals("success")) {
                String city = node.get("city").asText();
                String region = node.get("region").asText();
                String lat = node.get("lat").asText();
                String lon = node.get("lon").asText();

                return new String[]{ "success", city, region, lat, lon };
            }
        } catch (Exception e) {
            System.out.println("Could not get IP location: " + e.getMessage());
        }
        return null;
    }
}