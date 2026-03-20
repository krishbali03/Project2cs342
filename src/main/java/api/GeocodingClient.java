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
    //Help turn our JSON into objects to make life awesome and pretty
    private static final ObjectMapper mapper = new ObjectMapper();
    //Allows us to make request at https://nominatim.openstreetmap.org
    private static final HttpClient client = HttpClient.newHttpClient();
    //Use our client to make a request and get back a list of cities that match input
    public static List<GeoResult> search(String query) {
        try {
            String encoded = query.replace(" ", "+");
            String url = "https://nominatim.openstreetmap.org/search"
                    + "?q=" + encoded //gets rid of spaces since nominatim is ugly and weird
                    + "&format=json"
                    + "&limit=6" //i do not know why i went with 6
                    + "&countrycodes=us"
                    + "&featuretype=city";
            //Unfortunately we CANNOT make 100000000000 anonymous requests
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "WeatherApp/1.0 CS342Project")
                    .build();
            //actually sending our request to the API and getting back the locations and placing
            //them inside of GeoResult after we mapped them and have them as objects
            String json = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            GeoResult[] results = mapper.readValue(json, GeoResult[].class);
            return Arrays.asList(results); //Make the data easier to work with so we dont just return results
        } catch (Exception e) {
            return List.of();
        }
    }
    //We want to ingnore anything that isn't lat lon and name
    //this awesome class helps simplify cities so we only get information we need for NWSClient
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoResult {
        public String display_name;
        public String lat;
        public String lon;
        //unfortunately A LOT of the names are quite ugly
        //sooooo we fix that by only extrating the useful parts of the name
        //this works for MOST locations, however there are a few that look weird
        //but is that my concern? NO, i DO NOT care about Gary Indiana
        public String getShortName() {
            if (display_name == null) return "";
            String[] parts = display_name.split(",");
            String city = parts[0].trim();
            String state = parts.length >= 3 ? parts[2].trim() : "";
            return state.isEmpty() ? city : city + ", " + state;
        }
        //take out lat and lon so that we can easily deliver them to NWSClient
        public double getLat() { return Double.parseDouble(lat); }
        public double getLon() { return Double.parseDouble(lon); }
        @Override
        //When we want so display the name to anything that isn't an ugly piece of code
        //we put on some makeup and look presentable
        public String toString() { return getShortName(); }


    }
}