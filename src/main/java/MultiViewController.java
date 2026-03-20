import api.WeatherResult;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import weather.Period;
import java.io.IOException;
import java.util.List;

public class MultiViewController {

    @FXML private Button Return;

    //Day 1
    @FXML private ImageView dayicon1;
    @FXML private Text daytemp1;
    @FXML private Text percipday1;
    @FXML private Text WindDay1;
    @FXML private Text WIndDIrectDay1;

    //Night 1
    @FXML private ImageView nighticon1;
    @FXML private Text nighttemp1;
    @FXML private Text percipnight1;
    @FXML private Text Windnight1;
    @FXML private Text WIndDIrectNight1;

    //Day 2
    @FXML private ImageView dayicon2;
    @FXML private Text daytemp2;
    @FXML private Text percipday2;
    @FXML private Text WindDay2;
    @FXML private Text WIndDIrectDay2;

    //Night 2
    @FXML private ImageView nighticon2;
    @FXML private Text nighttemp2;
    @FXML private Text PercipNight2;
    @FXML private Text Windnight2;
    @FXML private Text WIndDIrectNight2;

    //Day 3
    @FXML private ImageView dayicon3;
    @FXML private Text daytemp3;
    @FXML private Text percipday3;
    @FXML private Text WindDay3;
    @FXML private Text WIndDIrectDay3;

    //Night 3
    @FXML private ImageView nighticon3;
    @FXML private Text nighttemp3;
    @FXML private Text PercipNight3;
    @FXML private Text Windnight3;
    @FXML private Text WIndDIrectNight3;

    //Dates
    @FXML private Text Day1;
    @FXML private Text Day2;
    @FXML private Text Day3;

    private WeatherResult weatherData;
    private String locationName;

    // Grabs the data without needing another api call, in theory. Cant get it to work
    public void initData(WeatherResult data, String locationName) {
        this.weatherData = data;
        this.locationName = locationName;
        populateForecast();
    }

    @FXML
    public void initialize() {
        if (Return != null) {
            Return.setOnAction(e -> swapToMainScene(e));
        }
    }

    private void populateForecast() {
        if (weatherData == null || weatherData.forecast == null || weatherData.forecast.isEmpty()) {
            System.out.println("[Forecast Issue] Something is wrong with the forecast data api call.");
            return;
        }

        List<Period> periods = weatherData.forecast;

        // NWS api call comes from period in a very fixed way, we can organize the date forcast for 6 days using a list
        fillTemplate(periods, 0, dayicon1,   daytemp1,   percipday1,   WindDay1,   WIndDIrectDay1);
        fillTemplate(periods, 1, nighticon1, nighttemp1, percipnight1, Windnight1, WIndDIrectNight1);
        fillTemplate(periods, 2, dayicon2,   daytemp2,   percipday2,   WindDay2,   WIndDIrectDay2);
        fillTemplate(periods, 3, nighticon2, nighttemp2, PercipNight2, Windnight2, WIndDIrectNight2);
        fillTemplate(periods, 4, dayicon3,   daytemp3,   percipday3,   WindDay3,   WIndDIrectDay3);
        fillTemplate(periods, 5, nighticon3, nighttemp3, PercipNight3, Windnight3, WIndDIrectNight3);


        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("EEE, MMM d");
        if (Day1 != null && periods.size() > 0) Day1.setText(fmt.format(periods.get(0).startTime));
        if (Day2 != null && periods.size() > 2) Day2.setText(fmt.format(periods.get(2).startTime));
        if (Day3 != null && periods.size() > 4) Day3.setText(fmt.format(periods.get(4).startTime));

        System.out.println("[3 day cast] Multiview scene complete.");
    }

    private void fillTemplate(List<Period> periods, int index,
                              ImageView icon, Text temp, Text precip,
                              Text wind, Text windDir) {

        if (index >= periods.size()) {
            if (temp    != null) temp.setText("-");
            if (precip  != null) precip.setText("-");
            if (wind    != null) wind.setText("-");
            if (windDir != null) windDir.setText("-");
            return;
        }

        Period p = periods.get(index);

        if (icon    != null) icon.setImage(new Image(getLocalIconPath(p)));
        if (temp    != null) temp.setText(p.temperature + "°");
        if (wind    != null) wind.setText(p.windSpeed    != null ? p.windSpeed    : "-");
        if (windDir != null) windDir.setText(p.windDirection != null ? p.windDirection : "-");

        if (precip != null) {
            int chance = 0;
            if (p.probabilityOfPrecipitation != null) {
                chance = p.probabilityOfPrecipitation.value;
            }
            precip.setText(chance + "%");
        }
    }


    //same logic for icon matching found in the main controller
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
            System.out.println("[Missing Icon] Icon not found: " + fileName + " - using default.");
            String fallback = isDay ? "Sunny.png" : "Clear-night.png";
            resource = getClass().getResource("/images/WeatherIcons/" + fallback);
        }
        return resource.toExternalForm();
    }

    private void swapToMainScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/main.fxml"));
            Parent mainRoot = loader.load();

            MainController mainController = loader.getController();
            mainController.initData(weatherData, locationName);

            Scene mainScene = new Scene(mainRoot, 1280, 720);
            mainScene.getStylesheets().add(getClass().getResource("/CSS/global.css").toExternalForm());

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(mainScene);
            window.show();
        } catch (IOException e) {
            System.out.println("Error returning to main scene: " + e.getMessage()); //catch exeptions
        }
    }
}