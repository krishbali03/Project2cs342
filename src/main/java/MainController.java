import api.NWSClient;
import api.WeatherResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import weather.Period;

public class MainController {

    @FXML private Canvas weatherCanvas;
    @FXML private Label subtitleLabel;
    @FXML private Label examineLabel;
    @FXML private StackPane root;
    @FXML private Button testButton;

    private static WeatherResult cachedWeather = null;
    private static double cachedLat = 41.8781;
    private static double cachedLon = -87.6298;
    private static String cachedLocation = "Chicago, IL";
    private static int cachedPeriodIndex = 0;
    private static int cachedHourIndex = 0;
    private WeatherResult currentWeather;
    private double currentLat = 41.8781;
    private double currentLon = -87.6298;
    private int selectedPeriodIndex = 0;
    private int selectedHourIndex = 0;
    private String currentLocationName = "Chicago, IL";
    private Image bgImage;

    public void setSelectedPeriod(int index) {
        selectedPeriodIndex = index;
        cachedPeriodIndex = index;
    }
    public void setSelectedHour(int hour) {
        selectedHourIndex = hour;
        cachedHourIndex = hour;
    }
    public void setLocationName(String name) {
        currentLocationName = name;
        cachedLocation = name;
    }
    public void setWeatherData(WeatherResult result, double lat, double lon) {
        currentWeather = result;
        currentLat = lat;
        currentLon = lon;
    }
    private boolean isOverSign(double x, double y) {
        return x >= 805 && x <= 1206 && y >= 166 && y <= 482;
    }

    private void drawSignHighlight() {
        javafx.scene.canvas.GraphicsContext gc = weatherCanvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.15));
        gc.fillRect(805, 166, 401, 316);
    }
    @FXML
    public void initialize() {
        bgImage = new Image(getClass().getResourceAsStream("/sprites/background.png"));

        weatherCanvas.setOnMouseMoved(e -> {
            if (isOverSign(e.getX(), e.getY())) {
                examineLabel.setText("Examine.");
                redrawCanvas();
                drawSignHighlight();
            } else {
                examineLabel.setText("");
                redrawCanvas();
            }
        });
        weatherCanvas.setOnMouseClicked(e -> {
            if (isOverSign(e.getX(), e.getY())) {
                SoundManager.playClick();
                switchToSignScene();
            }
        });

        loadWeather(currentLat, currentLon);
    }

    private void loadWeather(double lat, double lon) {
        new Thread(() -> {
            WeatherResult result = NWSClient.getWeather(lat, lon);
            Platform.runLater(() -> {
                if (result == null) {
                    subtitleLabel.setText("Could not retrieve weather data.");
                    return;
                }
                currentWeather = result;
                cachedWeather = result;
                redrawCanvas();
                subtitleLabel.setText(getAtmosphericSubtitle(result.forecast.get(0).shortForecast));
            });
        }).start();
    }

    private void redrawCanvas() {
        javafx.scene.canvas.GraphicsContext gc = weatherCanvas.getGraphicsContext2D();
        gc.drawImage(bgImage, 0, 0, 1278, 782);
        drawSignText(currentWeather);
    }

    public void refresh() {
        Platform.runLater(() -> {
            redrawCanvas();
            if (currentWeather != null) {
                subtitleLabel.setText(getAtmosphericSubtitle(
                        currentWeather.forecast.get(0).shortForecast));
            }
        });
    }

    private void drawSignText(WeatherResult result) {
        if (result == null) return;

        Period selected = null;

        try {
            if (result.hourly != null && !result.hourly.isEmpty()) {
                Period dayPeriod = result.forecast.get(Math.min(selectedPeriodIndex, result.forecast.size() - 1));
                String targetDate = new java.text.SimpleDateFormat("EEE MMM d").format(dayPeriod.startTime);
                int count = 0;
                for (Period p : result.hourly) {
                    String periodDate = new java.text.SimpleDateFormat("EEE MMM d").format(p.startTime);
                    if (periodDate.equals(targetDate)) {
                        if (count == selectedHourIndex) { selected = p; break; }
                        count++;
                    }
                }
            }
            if (selected == null) {
                selected = result.forecast.get(Math.min(selectedPeriodIndex, result.forecast.size() - 1));
            }
        } catch (Exception e) {
            selected = result.forecast.get(0);
        }

        javafx.scene.canvas.GraphicsContext gc = weatherCanvas.getGraphicsContext2D();
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setFill(javafx.scene.paint.Color.WHITE);

        javafx.scene.text.Font light38 = javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Light.ttf"), 38);
        javafx.scene.text.Font light42 = javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Light.ttf"), 42);
        javafx.scene.text.Font light24 = javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Light.ttf"), 24);
        javafx.scene.text.Font light52 = javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Light.ttf"), 52);

        gc.setFont(light38);
        gc.fillText("Welcome to", 1005, 245);

        gc.setFont(light42);
        gc.fillText(currentLocationName, 1005, 295);

        gc.setFont(light24);
        gc.fillText(new java.text.SimpleDateFormat("EEEE, MMMM d yyyy").format(selected.startTime), 1005, 355);

        gc.setFont(light24);
        gc.fillText(new java.text.SimpleDateFormat("h:mm a").format(selected.startTime), 1005, 390);

        gc.setFont(light52);
        gc.fillText(selected.temperature + "°F", 1005, 462);
    }

    private void switchToSignScene() {
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(500), root
        );
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sign.fxml"));
                Parent newRoot = loader.load();
                SignController controller = loader.getController();
                controller.setWeatherData(currentWeather, currentLat, currentLon);
                controller.setDateIndex(selectedPeriodIndex / 2);
                controller.setHourIndex(selectedHourIndex);
                controller.setLocationName(currentLocationName);
                controller.refreshDisplay();
                Stage stage = (Stage) weatherCanvas.getScene().getWindow();
                stage.setScene(new Scene(newRoot, 1278, 782));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        SoundManager.playClick();
        fade.play();
    }

    private String getAtmosphericSubtitle(String shortForecast) {
        String f = shortForecast.toLowerCase();
        if (f.contains("thunder") || f.contains("storm")) return "the thunder feels like its bashing my brain";
        if (f.contains("snow")) return "snow? so pure so white";
        if (f.contains("fog")) return "i can't see anything...";
        if (f.contains("rain")) return "its been pouring for hours";
        if (f.contains("cloud")) return "the sun wants to hide today";
        return "pretty clear";
    }
}