package api;

import weather.Period;
import java.util.ArrayList;
//this is what all of our weather looks like
//i mean once we do all that ugly api nonsense all the weather becomes array lists
public class WeatherResult {
    public ArrayList<Period> forecast;
    public ArrayList<Period> hourly;
    public WeatherResult(ArrayList<Period> forecast, ArrayList<Period> hourly) {
        this.forecast = forecast;
        this.hourly = hourly;
    }
}