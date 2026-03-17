import javafx.scene.media.AudioClip;

public class SoundManager {

    private static AudioClip clickSound;

    public static void load() {
        try {
            clickSound = new AudioClip(
                    SoundManager.class.getResource("/sounds/sh2-recieve-item.mp3").toExternalForm()
            );
        } catch (Exception e) {
            System.out.println("bro what are you DOING!");
        }
    }

    public static void playClick() {
        if (clickSound != null) clickSound.play();
    }

    public static void attachToButton(javafx.scene.control.Button button) {
        button.setOnMouseClicked(e -> playClick());
    }
}