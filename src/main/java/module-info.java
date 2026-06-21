module org.example.musicscorebuilder {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    opens org.example.musicscorebuilder to javafx.fxml;
    exports org.example.musicscorebuilder;
}