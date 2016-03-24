import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;

/**
 * Created by User on 24/03/2016.
 */
public class test extends Application{

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);


        primaryStage.show();
        Media media = new Media("E:/01%20-%20Daniele/Musica/Anime/Attack%20on%20Titans/Call%20your%20name%20");
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);

    }
}
