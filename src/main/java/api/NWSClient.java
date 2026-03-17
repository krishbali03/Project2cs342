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

    public static WeatherResult getWeather(double lat, double lon) {
        PointsProperties points = getPoints(lat, lon);
        if (points == null) return null;

        ArrayList<Period> forecast = getForecast(points.forecast);
        ArrayList<Period> hourly = getForecast(points.forecastHourly);

        return new WeatherResult(forecast, hourly);
    }

    public static PointsProperties getPoints(double lat, double lon) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.gov/points/" + lat + "," + lon))
                .header("User-Agent", "WeatherApp/1.0")
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        PointsResponse r = getPointsObject(response.body());
        if (r == null) {
            System.err.println("Failed to parse points JSON");
            return null;
        }
        return r.properties;
    }

    public static ArrayList<Period> getForecast(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WeatherApp/1.0")
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Root r = getForecastObject(response.body());
        if (r == null) {
            System.err.println("Failed to parse forecast JSON");
            return null;
        }
        return r.properties.periods;
    }

    public static PointsResponse getPointsObject(String json) {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        PointsResponse toRet = null;
        try {
            toRet = om.readValue(json, PointsResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return toRet;
    }

    public static Root getForecastObject(String json) {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        Root toRet = null;
        try {
            toRet = om.readValue(json, Root.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return toRet;
    }
}