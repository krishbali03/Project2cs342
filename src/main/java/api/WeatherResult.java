package api;

import weather.Period;
import java.util.ArrayList;

public class WeatherResult {
    public ArrayList<Period> forecast;
    public ArrayList<Period> hourly;

    public WeatherResult(ArrayList<Period> forecast, ArrayList<Period> hourly) {
        this.forecast = forecast;
        this.hourly = hourly;
    }
}