import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class JavaFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Font.loadFont(getClass().getResourceAsStream("/fonts/AppleGaramond-Light.ttf"), 14);
		SoundManager.load();

		try {
			Parent root = FXMLLoader.load(getClass().getResource("/FXML/main.fxml"));

			Scene main = new Scene(root, 1280, 720);

			main.getStylesheets().add(getClass().getResource("/CSS/global.css").toExternalForm());
			primaryStage.setTitle("Weather App");
			primaryStage.setResizable(false);
			primaryStage.setScene(main);
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}