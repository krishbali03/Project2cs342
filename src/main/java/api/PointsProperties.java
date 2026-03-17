package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PointsProperties {
    public String forecast;
    public String forecastHourly;
    public String gridId;
    public int gridX;
    public int gridY;
}