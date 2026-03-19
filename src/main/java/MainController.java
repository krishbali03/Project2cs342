import api.NWSClient;
import api.WeatherResult;
import api.IpApiClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import weather.Period;

public class MainController {

    // 1. UI Elements
    @FXML private VBox root;
    @FXML private TextField searchBar;
    @FXML private Button currentLocation;
    @FXML private Button search;
    @FXML private Label cityName;
    @FXML private HBox hourlyHBox;
    @FXML private ImageView mainWeatherIcon;
    @FXML private HBox dailyHBox;

    // 2. Data State
    private WeatherResult currentWeather;
    private double currentLat = 41.8781;
    private double currentLon = -87.6298;
    private String currentLocationName = "Chicago, IL";

    @FXML
    public void initialize() {
        // Set up the button click events
        search.setOnAction(e -> handleSearch());

//        currentLocation.setOnAction(e -> {
//            loadCurrentLocation();
//        });

        // Initialize the City Name label style so it's visible on the dark background
        cityName.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        // Automatically fetch your IP location as soon as the app turns on
        loadCurrentLocation();
    }

    private void handleSearch() {
        String query = searchBar.getText();
        if (query == null || query.isEmpty()) return;

        System.out.println("User searched for: " + query);
        cityName.setText("Loading " + query + "...");

        // Note: You can plug in your GeocodingClient.search(query) here later
    }

    private void loadCurrentLocation() {
        cityName.setText("Locating...");

        new Thread(() -> {
            // Grab our array: [status, city, state, lat, lon]
            String[] locData = IpApiClient.getLocation();

            Platform.runLater(() -> {
                if (locData != null) {
                    // Update our variables using the array slots
                    currentLocationName = locData[1] + ", " + locData[2]; // City, State
                    currentLat = Double.parseDouble(locData[3]);          // Lat
                    currentLon = Double.parseDouble(locData[4]);          // Lon
                } else {
                    System.out.println("Location failed, defaulting to previous coordinates.");
                }

                // Fetch the weather using the new coordinates
                loadWeather(currentLat, currentLon, currentLocationName);
            });
        }).start();
    }

    private void loadWeather(double lat, double lon, String name) {
        new Thread(() -> {
            // Fetch data in the background so the UI doesn't freeze
            WeatherResult result = NWSClient.getWeather(lat, lon);

            Platform.runLater(() -> {
                if (result != null) {
                    currentWeather = result;
                    currentLocationName = name;
                    updateDisplay();
                } else {
                    cityName.setText("Failed to load weather.");
                }
            });
        }).start();
    }

    private void updateDisplay() {
        // 1. Update the main city label
        cityName.setText(currentLocationName);

        // 2. Clear the hourly scroll box and fill it with new data
        hourlyHBox.getChildren().clear();

        // Populate Hourly Data
        if (currentWeather != null && currentWeather.hourly != null) {
            for (int i = 0; i < Math.min(12, currentWeather.hourly.size()); i++) {
                Period hour = currentWeather.hourly.get(i);

                // Create the individual card background
                VBox hourCard = new VBox(8);
                hourCard.setStyle("-fx-background-color: #1E1E27; -fx-padding: 10; -fx-background-radius: 8; -fx-alignment: center; -fx-min-width: 60; -fx-pref-height: 75;");

                // Format Time Label (e.g., "3 PM")
                String timeStr = new java.text.SimpleDateFormat("h a").format(hour.startTime);
                Label timeLabel = new Label(timeStr);
                timeLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px;");

                // Format Temp Label (e.g., "72°")
                Label tempLabel = new Label(hour.temperature + "°");
                tempLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

                // Add both labels to the card, and the card to the scroll row
                hourCard.getChildren().addAll(timeLabel, tempLabel);
                hourlyHBox.getChildren().add(hourCard);
            }
        }

        // --- 3. Populate 7-Day Forecast Data ---
        dailyHBox.getChildren().clear();

        if (currentWeather != null && currentWeather.forecast != null) {
            for (int i = 0; i < currentWeather.forecast.size(); i++) {
                Period currentPeriod = currentWeather.forecast.get(i);

                // We only want to create a card for the Daytime periods
                if (currentPeriod.isDaytime) {

                    // Create the Daily Card
                    VBox dayCard = new VBox(5);
                    dayCard.setStyle("-fx-background-color: #000000; -fx-background-radius: 10; -fx-alignment: center; -fx-min-width: 120; -fx-pref-height: 190;");

                    // 1. Day Name (Convert "Monday" to "Mon")
                    String dayName = currentPeriod.name;
                    if (dayName.length() > 3 && !dayName.equals("Today")) {
                        dayName = dayName.substring(0, 3);
                    }
                    Label dayLabel = new Label(dayName);
                    dayLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 16px;");

                    // 2. Weather Icon (Temporary magnifying glass)
                    ImageView icon = new ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/searchIcon.png")));
                    icon.setFitHeight(48);
                    icon.setFitWidth(48);

                    // 3. High / Low Temperature
                    String highLow = currentPeriod.temperature + "°";

                    // Check if there is a "Night" period right after this one to get the Low Temp
                    if (i + 1 < currentWeather.forecast.size()) {
                        Period nightPeriod = currentWeather.forecast.get(i + 1);
                        highLow += " / " + nightPeriod.temperature + "°";
                    }

                    Label tempLabel = new Label(highLow);
                    tempLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

                    // Add everything to the card, and add the card to the scroll box
                    dayCard.getChildren().addAll(dayLabel, icon, tempLabel);
                    dailyHBox.getChildren().add(dayCard);
                }
            }
        }
    }
}