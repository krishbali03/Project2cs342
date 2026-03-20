package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//wrapper
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointsResponse {
    public PointsProperties properties;
}