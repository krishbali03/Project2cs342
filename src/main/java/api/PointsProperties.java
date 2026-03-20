package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//we only care about this info from nws
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointsProperties {
    public String forecast;
    public String forecastHourly;
}