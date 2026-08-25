package org.example.musicscorebuilder.palette;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class PaletteController {
    @FXML private GridPane beamGrid;
    @FXML private GridPane timeSignatureGrid;
    @FXML private GridPane barLinesGrid;
    @FXML private GridPane keySignatureGrid;
    @FXML private GridPane layoutGrid;

    @FXML
    public void initialize() {
        new BeamSectionController(beamGrid).build();
        new TimeSignatureSectionController(timeSignatureGrid).build();
        new BarlinesSectionController(barLinesGrid).build();
        new KeySignatureSectionController(keySignatureGrid).build();
        new LayoutSectionController(layoutGrid).build();
    }
}