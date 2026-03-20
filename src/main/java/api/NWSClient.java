package api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import weather.Period;
import weather.Root;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class NWSClient {
    //just in case some locations are unreachable we will blindly trust the federal government
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();
    //make sure we dont crash on null or mysterious entries
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
    //once we make our request we take our data and put it into two array lists
    //one representing the forecast for the days
    //the other representing the hourly forecast
    //we combine together those two array lists with the power of friendship
    //to make one awesome weather result
    public static WeatherResult getWeather(double lat, double lon) {
        PointsProperties points = getPoints(lat, lon);
        if (points == null) return null;
        ArrayList<Period> forecast = getForecast(points.forecast);
        ArrayList<Period> hourly = getForecast(points.forecastHourly);
        return new WeatherResult(forecast, hourly);
    }
    //turn out lon and lat into grids
    public static PointsProperties getPoints(double lat, double lon) {
        String json = get("https://api.weather.gov/points/" + lat + "," + lon);
        if (json == null) return null;
        try {
            return mapper.readValue(json, PointsResponse.class).properties;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //return the forecast from our url
    public static ArrayList<Period> getForecast(String url) {
        String json = get(url);
        if (json == null) return null;
        try {
            return mapper.readValue(json, Root.class).properties.periods;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //actually make the requests establish who we are and what our intentions are
    public static String get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "WeatherAPP/1.0 CS342Project")
                    .header("Accept", "application/geo+json")
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}