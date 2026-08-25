package org.example.musicscorebuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.views.util.TextMeasurer;
import org.example.musicscorebuilder.managers.ClosingManager;
import org.example.musicscorebuilder.managers.FontManager;
import org.example.musicscorebuilder.managers.TextMeasurerService;

import java.io.IOException;
import java.util.Objects;

public class MusicScoreBuilder extends Application {
    private static final ClosingManager closingManager = ClosingManager.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MusicScoreBuilder.class.getResource("main-view.fxml"));
        LyricLayout.setDefaultMeasurer(new TextMeasurer());
        TextMeasurerService.setInstance(new FontManager());
        FontManager.loadFonts();
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/styles/center.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/left.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/right.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/forms.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/toolbar.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/menubar.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/styles/sheet.css")).toExternalForm()
        );

        stage.setOnCloseRequest(event -> {
            event.consume();
            closingManager.closeApp();
        });

        stage.setTitle("MusicScore Builder");
        stage.setWidth(1500);
        stage.setHeight(800);

        stage.setMinWidth(1500);
        stage.setMinHeight(800);

        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }
}