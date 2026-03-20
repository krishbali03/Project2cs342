import api.NWSClient;
import api.WeatherResult;
import api.IpApiClient;
import api.GeocodingClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import weather.Period;
import java.io.IOException;
import java.util.List;

public class MainController {

    // FXML Variables
    @FXML private VBox root;
    @FXML private TextField Search;
    @FXML private Button SearchGO;
    @FXML private Button GrabCurrentLocation;
    @FXML private Text Location;
    @FXML private Text CurrentTemp;
    @FXML private ImageView CurrentIcon;
    @FXML private Button MultidayScene;
    @FXML private HBox hourlyHBox;


    @FXML private Text DailyHIgh;
    @FXML private Text DailyLow;
    @FXML private Text CurrentWindSpeed;
    @FXML private Text CurrentWindDirection;
    @FXML private Text ChanceOfPercipitation;

    // State Variables
    private WeatherResult currentWeather;
    private double currentLat = 41.8781;
    private double currentLon = -87.6298;
    private String currentLocationName = "Chicago, IL";
    private boolean dataPreloaded = false;

    @FXML
    public void initialize() {
        if (!dataPreloaded) {
            loadCurrentLocation();
        }
        System.out.println("[APP START] Initializing Main Controller...");

        if (Search != null) Search.setOnAction(e -> handleSearch());
        if (SearchGO != null) SearchGO.setOnAction(e -> handleSearch());
        if (GrabCurrentLocation != null) GrabCurrentLocation.setOnAction(e -> loadCurrentLocation());
        if (MultidayScene != null) MultidayScene.setOnAction(e -> swapToMultiView(e));

        if (dataPreloaded) {
            updateDisplay();
        }
    }

    public void initData(WeatherResult data, String locationName) {
        this.currentWeather = data;
        this.currentLocationName = locationName;
        this.dataPreloaded = true; //does not work, tried to keep it saved but couldnt find the bug
    }

    @FXML
    private void handleSearch() {
        String query = Search.getText();
        if (query == null || query.trim().isEmpty()) return;

        System.out.println("\n [SEARCH INITIATED]");
        System.out.println("1. User Typed: " + query);
        if (Location != null) Location.setText("Searching atlas...");

        new Thread(() -> {
            System.out.println("2. Requesting coordinates from Geocoding API...");
            List<GeocodingClient.GeoResult> results = GeocodingClient.search(query);

            Platform.runLater(() -> {
                if (results != null && !results.isEmpty()) {
                    GeocodingClient.GeoResult geo = results.get(0);
                    currentLocationName = geo.display_name;
                    System.out.println("3. [All Good] Found coordinates for: " + currentLocationName);
                    System.out.println("   => PARSED SEARCH LAT: " + geo.getLat());
                    System.out.println("   => PARSED SEARCH LON: " + geo.getLon());

                    loadWeather(geo.getLat(), geo.getLon(), currentLocationName);
                    Search.clear();
                } else {
                    System.out.println("3. [No Bueno] Geocoding could not find that city.");
                    if (Location != null) Location.setText("That place does not exist.");
                }
            });
        }).start();
    }

    private void loadCurrentLocation() {
        System.out.println("\n [Doxing you]");
        if (Location != null) Location.setText("Finding your location...");

        new Thread(() -> {
            String[] locData = IpApiClient.getLocation();

            Platform.runLater(() -> {
                if (locData != null) {
                    currentLocationName = locData[1] + ", " + locData[2];
                    currentLat = Double.parseDouble(locData[3]);
                    currentLon = Double.parseDouble(locData[4]);
                    System.out.println("[We're in] IP located at: " + currentLocationName);
                    System.out.println("   => IP STARTUP LAT: " + currentLat);
                    System.out.println("   => IP STARTUP LON: " + currentLon);


                } else {
                    System.out.println("[Failed Dox] IP Location failed, defaulting to Chicago.");
                }
                loadWeather(currentLat, currentLon, currentLocationName);
            });
        }).start();
    }

    //Grabs the weather and passes the search to the geocodingclient api which then passes it to the nws api
    private void loadWeather(double lat, double lon, String name) {
        if (Location != null) Location.setText("Reading the clouds...");

        lat = Math.round(lat * 10000.0) / 10000.0;
        lon = Math.round(lon * 10000.0) / 10000.0;

        final double finalLat = lat;
        final double finalLon = lon;
        //nws doesnt play nice with long digits after the decimal. must be capped in order to work


        new Thread(() -> {
            System.out.println("4. [Trying to grab weather] NWS API -> Lat: " + finalLat + ", Lon: " + finalLon);
            WeatherResult result = NWSClient.getWeather(finalLat, finalLon);

            Platform.runLater(() -> {
                if (result != null) {
                    System.out.println("5. [Very Good] We read the sky! Updating frontend.");
                    currentWeather = result;
                    currentLocationName = name;
                    updateDisplay();
                } else {
                    System.out.println("5. [Very bad] NWS API issue.");
                    if (Location != null) Location.setText("Couldn't retrieve weather.");
                }
            });
        }).start();
    }

    // Parses through shared names from the nws's naming convention and grabs the locally stored icons with the highest match based on

    private String getLocalIconPath(Period p) {
        String forecast = p.shortForecast.toLowerCase();
        boolean isDay = p.isDaytime;
        String fileName = isDay ? "Sunny.png" : "Clear-night.png";

        if (forecast.contains("thunderstorm")) {
            fileName = forecast.contains("severe") ? "Severe-thunderstorm.png" : "Scattered-thunderstorm.png";
        } else if (forecast.contains("blizzard")) {
            fileName = "Blizzard.png";
        } else if (forecast.contains("snow")) {
            fileName = "Snow.png";
        } else if (forecast.contains("rain") || forecast.contains("showers")) {
            if (forecast.contains("heavy")) {
                fileName = "Heavy-rain.png";
            } else if (forecast.contains("scattered")) {
                fileName = isDay ? "Scattered-showers.png" : "Scattered-showers-night.png";
            } else {
                fileName = isDay ? "Rain.png" : "Rain-night.png";
            }
        } else if (forecast.contains("cloudy") || forecast.contains("overcast")) {
            if (forecast.contains("partly") || forecast.contains("mostly")) {
                fileName = isDay ? "Partly-cloudy.png" : "Partly-cloudy-night.png";
            } else {
                fileName = "Cloudy.png";
            }
        } else if (forecast.contains("fog")) {
            fileName = "Fog.png";
        } else if (forecast.contains("wind")) {
            fileName = "Wind.png";
        } else if (forecast.contains("drizzle")) {
            fileName = isDay ? "Drizzle.png" : "Drizzle-night.png";
        }

        java.net.URL resource = getClass().getResource("/images/WeatherIcons/" + fileName);
        if (resource == null) {
            System.out.println("[WARN] Icon not found: " + fileName + " - using default.");
            String fallback = isDay ? "Sunny.png" : "Clear-night.png";
            resource = getClass().getResource("/images/WeatherIcons/" + fallback);
        }
        return resource.toExternalForm();
    }

    //
    // Grabs the hi and lo temps using the day period
    // Only scans the first two since we need to only grab todays day and night temps

    private int[] getHiLo() {
        int hi = Integer.MIN_VALUE;
        int lo = Integer.MAX_VALUE;

        if (currentWeather.forecast != null) {
            int checked = 0;
            for (Period p : currentWeather.forecast) {
                if (checked >= 2) {
                    break;
                }
                if (p.isDaytime  && hi == Integer.MIN_VALUE) {
                    hi = p.temperature;
                };
                if (!p.isDaytime && lo == Integer.MAX_VALUE) {
                    lo = p.temperature;
                };
                checked++;
            }
        }

        if (hi == Integer.MIN_VALUE) hi = lo;  // edge case when its late and todays day is no longer being brought in by the nws api
        if (lo == Integer.MAX_VALUE) lo = hi;

        return new int[]{hi, lo};
    }

    // update logic

    private void updateDisplay() {
        if (currentWeather == null || currentWeather.hourly == null || currentWeather.hourly.isEmpty()) return;

        Period current = currentWeather.hourly.get(0);

        // Main icon
        if (CurrentIcon != null) {
            CurrentIcon.setImage(new javafx.scene.image.Image(getLocalIconPath(current)));
        }

        // Location + current temp
        if (Location != null)    Location.setText(currentLocationName);
        if (CurrentTemp != null) CurrentTemp.setText("Current temp: " + current.temperature + "°");

        // Hi / Lo  (from daily forecast endpoint)
        int[] hiLo = getHiLo();
        if (DailyHIgh != null) DailyHIgh.setText("High: " + hiLo[0] + "°");
        if (DailyLow != null) DailyLow.setText(" Low: "  + hiLo[1] + "°");

        // Wind speed + direction
        // windSpeed is already a formatted string from NWS, e.g. "12 mph" or "5 to 10 mph"
        if (CurrentWindSpeed != null) CurrentWindSpeed.setText( current.windSpeed != null ? "Current wind: " + current.windSpeed     : "-");
        if (CurrentWindDirection != null) CurrentWindDirection.setText(current.windDirection != null ? "Wind Direction: " + current.windDirection : "-");

        if (ChanceOfPercipitation != null) {
            int chance = 0;
            if (current.probabilityOfPrecipitation != null) {
                chance = current.probabilityOfPrecipitation.value;
            }
            ChanceOfPercipitation.setText(chance + "%");
        }

        // Hourly scroll
        if (hourlyHBox != null) {
            List<Node> panels = hourlyHBox.getChildren();

            for (int i = 0; i < Math.min(panels.size(), currentWeather.hourly.size()); i++) {
                if (!(panels.get(i) instanceof VBox)) continue;

                VBox panel = (VBox) panels.get(i);
                Period hourData = currentWeather.hourly.get(i);
                List<Node> cardChildren = panel.getChildren();

                if (cardChildren.size() < 3) continue;

                // Icon
                if (cardChildren.get(0) instanceof ImageView) {
                    ((ImageView) cardChildren.get(0))
                            .setImage(new javafx.scene.image.Image(getLocalIconPath(hourData)));
                }

                // Temp
                if (cardChildren.get(1) instanceof Text) {
                    ((Text) cardChildren.get(1)).setText(hourData.temperature + "°");
                }

                // Time
                if (cardChildren.get(2) instanceof Text) {
                    String timeStr = new java.text.SimpleDateFormat("h a").format(hourData.startTime);
                    ((Text) cardChildren.get(2)).setText(timeStr);
                }
            }
            System.out.println("6. [ITS WORKING] Hourly panels set.");
        }
    }

    private void swapToMultiView(ActionEvent event) {
        if (currentWeather == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Multiview.fxml"));
            Parent forecastRoot = loader.load();

            // Pass weather data to MultiViewController BEFORE showing the scene
            MultiViewController multiController = loader.getController();
            multiController.initData(currentWeather, currentLocationName);

            Scene forecastScene = new Scene(forecastRoot, 1280, 720);
            forecastScene.getStylesheets().add(getClass().getResource("/CSS/global.css").toExternalForm());

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(forecastScene);
            window.show();
        } catch (IOException e) {
            System.out.println("Error loading forecast scene: " + e.getMessage());
        }
    }
}