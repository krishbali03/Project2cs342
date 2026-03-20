import api.GeocodingClient;
import api.NWSClient;
import api.WeatherResult;
import com.sun.tools.javac.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import weather.Period;
import java.text.SimpleDateFormat;
import java.util.List;

public class SignController {

    @FXML private Button datePrev;
    @FXML private Button dateNext;
    @FXML private Label dateLabel;
    @FXML private Button timePrev;
    @FXML private Button timeNext;
    @FXML private Label timeLabel;
    @FXML private Label selectedLocation;
    @FXML private Label selectedTemp;
    @FXML private Label selectedForecast;
    @FXML private Label selectedWind;
    @FXML private Label selectedPrecip;
    @FXML private TextField locationSearch;
    @FXML private VBox searchResults;
    @FXML private Button backButton;
    @FXML private Label subtitleLabel;

    private WeatherResult currentWeather;
    private double currentLat;
    private double currentLon;
    private String currentLocationName = "Chicago, IL";
    private int dateIndex = 0;
    private int hourIndex = 0;
    public void setDateIndex(int index) {
        this.dateIndex = index;
    }

    public void setHourIndex(int index) {
        this.hourIndex = index;
    }
    private ContextMenu suggestionMenu = new ContextMenu();
    final javafx.animation.PauseTransition pause = new  javafx.animation.PauseTransition(javafx.util.Duration.millis(200));

    @FXML
    public void initialize() {
        datePrev.setOnMouseClicked(e -> { SoundManager.playClick(); changeDate(-1); });
        dateNext.setOnMouseClicked(e -> { SoundManager.playClick(); changeDate(1); });
        timePrev.setOnMouseClicked(e -> { SoundManager.playClick(); changeHour(-1); });
        timeNext.setOnMouseClicked(e -> { SoundManager.playClick(); changeHour(1); });
        backButton.setOnMouseClicked(e -> { SoundManager.playClick(); goBack(); });

        locationSearch.textProperty().addListener((obs, old, val) -> {
            if (val != null && val.trim().length() > 2){
                pause.setOnFinished(e -> searchCity(val.trim()));
                pause.playFromStart();
            }
            else{
                suggestionMenu.getItems().clear();
            }
        });
    }

    public void setWeatherData(WeatherResult result, double lat, double lon) {
        currentWeather = result;
        currentLat = lat;
        currentLon = lon;
    }

    public void setLocationName(String name) {
        currentLocationName = name;
    }
    public void refreshDisplay() {
        updateDisplay();
    }

    private void changeDate(int delta) {
        dateIndex = Math.max(0, Math.min(6, dateIndex + delta));
        hourIndex = 0;
        updateDisplay();
    }

    private void changeHour(int delta) {
        hourIndex = Math.max(0, Math.min(23, hourIndex + delta));
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentWeather == null || currentWeather.forecast == null) return;
        int forecastIndex = Math.min(dateIndex * 2, currentWeather.forecast.size() - 1);
        Period forecastPeriod = currentWeather.forecast.get(forecastIndex);
        dateLabel.setText(new java.text.SimpleDateFormat("EEE MMM d").format(forecastPeriod.startTime));
        Period hourlyPeriod = getHourlyPeriod();
        Period display = hourlyPeriod != null ? hourlyPeriod : forecastPeriod;
        selectedLocation.setText(currentLocationName);
        selectedTemp.setText(display.temperature + "° " + display.temperatureUnit);
        selectedForecast.setText(display.shortForecast);
        selectedWind.setText(display.windSpeed + " " + display.windDirection);
        selectedPrecip.setText(display.probabilityOfPrecipitation != null ?
                display.probabilityOfPrecipitation.value + "% precip" : "N/A");
        if (hourlyPeriod != null) {
            timeLabel.setText(new java.text.SimpleDateFormat("h:mm a").format(hourlyPeriod.startTime));
        } else {
            timeLabel.setText("sworry");
        }
    }
    private Period getHourlyPeriod() {
        if (currentWeather.hourly == null || currentWeather.forecast == null) return null;
        if (dateIndex * 2 >= currentWeather.forecast.size()) return null;

        Period dayPeriod = currentWeather.forecast.get(dateIndex * 2);
        String targetDate = new java.text.SimpleDateFormat("EEE MMM d").format(dayPeriod.startTime);

        int count = 0;
        for (Period p : currentWeather.hourly) {
            String periodDate = new java.text.SimpleDateFormat("EEE MMM d").format(p.startTime);
            if (periodDate.equals(targetDate)) {
                if (count == hourIndex) return p;
                count++;
            }
        }
        return null;
    }
    //fixxxxxx
    private void searchCity(String query) {
        new Thread(() -> {
            List<GeocodingClient.GeoResult> results = GeocodingClient.search(query);
            Platform.runLater(() -> {
                suggestionMenu.getItems().clear();
                suggestionMenu.hide();
                if (results == null || results.isEmpty()) return;
                List<GeocodingClient.GeoResult> top6 = results.stream().limit(6).collect(java.util.stream.Collectors.toList());
                for (GeocodingClient.GeoResult r : top6) {
                    MenuItem item = new MenuItem(r.getShortName());
                    item.setOnAction(e -> selectCity(r));
                    suggestionMenu.getItems().add(item);
                }
                if (locationSearch.getText() == null || locationSearch.getScene().getWindow() != null) {
                    suggestionMenu.show(locationSearch, javafx.geometry.Side.BOTTOM, 0, 0);
                }

            });
        }).start();
    }

    private void selectCity(GeocodingClient.GeoResult city) {
        currentLocationName = city.getShortName();
        locationSearch.setText(currentLocationName);
        suggestionMenu.hide();
        subtitleLabel.setText("Loading...");

        new Thread(() -> {
            WeatherResult result = NWSClient.getWeather(city.getLat(), city.getLon());
            Platform.runLater(() -> {
                if (result != null) {
                    currentWeather = result;
                    currentLat = city.getLat();
                    currentLon = city.getLon();
                    dateIndex = 0;
                    hourIndex = 0;
                    MainController.resetCache(result, city.getLat(), city.getLon(), currentLocationName);
                    updateDisplay();
                    subtitleLabel.setText("");
                } else {
                    subtitleLabel.setText("bro are you in ukatan?.");
                }
            });
        }).start();
    }

    private void goBack() {
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(500),
                (javafx.scene.layout.StackPane) backButton.getScene().getRoot()
        );
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            try {
                MainController.resetCache(currentWeather, currentLat, currentLon, currentLocationName);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
                Parent newRoot = loader.load();
                MainController controller = loader.getController();
                controller.setSelectedPeriod(dateIndex * 2);
                controller.setSelectedHour(hourIndex);
                controller.refresh();
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(newRoot, 1278, 782));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        fade.play();
    }
}