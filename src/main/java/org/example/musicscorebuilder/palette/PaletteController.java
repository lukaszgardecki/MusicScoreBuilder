package org.example.musicscorebuilder.palette;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class PaletteController {
    @FXML private GridPane timeSignatureGrid;
    @FXML private GridPane barLinesGrid;

    @FXML
    public void initialize() {
        new TimeSignatureSectionController(timeSignatureGrid).build();
        new BarlinesSectionController(barLinesGrid).build();
    }
}