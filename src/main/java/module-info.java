module org.example.musicscorebuilder {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires javafx.graphics;

    opens org.example.musicscorebuilder to javafx.fxml;
    opens org.example.musicscorebuilder.components.music to javafx.fxml;
    opens org.example.musicscorebuilder.components.views to javafx.fxml;

    exports org.example.musicscorebuilder;
    exports org.example.musicscorebuilder.components.music;
    exports org.example.musicscorebuilder.components.views;
    exports org.example.musicscorebuilder.components.layout;
    opens org.example.musicscorebuilder.components.layout to javafx.fxml;
    exports org.example.musicscorebuilder.palette;
    opens org.example.musicscorebuilder.palette to javafx.fxml;
    exports org.example.musicscorebuilder.components.layout.util;
    opens org.example.musicscorebuilder.components.layout.util to javafx.fxml;
    exports org.example.musicscorebuilder.managers;
    opens org.example.musicscorebuilder.managers to javafx.fxml;
    exports org.example.musicscorebuilder.controller;
    opens org.example.musicscorebuilder.controller to javafx.fxml;
    exports org.example.musicscorebuilder.util;
    opens org.example.musicscorebuilder.util to javafx.fxml;
    exports org.example.musicscorebuilder.components.layout.engine;
    opens org.example.musicscorebuilder.components.layout.engine to javafx.fxml;
}