package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class GeocodingClient {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final HttpClient client = HttpClient.newHttpClient();

    public static List<GeoResult> search(String query) {
        try {
            String encoded = query.replace(" ", "+");
            String url = "https://nominatim.openstreetmap.org/search"
                    + "?q=" + encoded
                    + "&format=json"
                    + "&limit=6"
                    + "&countrycodes=us"
                    + "&featuretype=city";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "WeatherApp/1.0 CS342Project")
                    .build();

            String json = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            GeoResult[] results = mapper.readValue(json, GeoResult[].class);
            return Arrays.asList(results);
        } catch (Exception e) {
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoResult {
        public String display_name;
        public String lat;
        public String lon;

        public String getShortName() {
            if (display_name == null) return "";
            String[] parts = display_name.split(",");
            String city = parts[0].trim();
            String state = parts.length >= 3 ? parts[2].trim() : "";
            return state.isEmpty() ? city : city + ", " + state;
        }

        public double getLat() { return Double.parseDouble(lat); }
        public double getLon() { return Double.parseDouble(lon); }
        @Override
        public String toString() { return display_name; }


    }
}