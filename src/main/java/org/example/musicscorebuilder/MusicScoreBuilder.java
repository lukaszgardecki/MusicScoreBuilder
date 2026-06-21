package org.example.musicscorebuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MusicScoreBuilder extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MusicScoreBuilder.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/styles/buttons.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/toolbar.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/sheet.css")).toExternalForm()
        );

        stage.setTitle("MusicScore Builder");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}
