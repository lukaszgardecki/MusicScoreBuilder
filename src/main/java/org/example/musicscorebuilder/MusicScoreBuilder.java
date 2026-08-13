package org.example.musicscorebuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.musicscorebuilder.controller.util.audio.PianoPlayer;
import org.example.musicscorebuilder.managers.FontManager;

import java.io.IOException;
import java.util.Objects;

public class MusicScoreBuilder extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MusicScoreBuilder.class.getResource("main-view.fxml"));
        FontManager.loadFonts();
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/styles/center.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/left.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/right.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/toolbar.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/menubar.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/sheet.css")).toExternalForm()
        );

        stage.setOnCloseRequest(event -> closeApp());

        stage.setTitle("MusicScore Builder");
        stage.setWidth(1500);
        stage.setHeight(800);

        stage.setMinWidth(1500);
        stage.setMinHeight(800);

        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void closeApp() {
        PianoPlayer.getInstance().close();
        Platform.exit();
        System.exit(0);
    }
}