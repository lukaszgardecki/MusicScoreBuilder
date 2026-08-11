package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class LyricFragment {
    private String text;
    private boolean bold;
    private boolean italic;
    private boolean underline;

    public LyricFragment() {
        this.text = "";
    }

    @JsonCreator
    public LyricFragment(
            @JsonProperty("text") String text,
            @JsonProperty("bold") boolean bold,
            @JsonProperty("italic") boolean italic,
            @JsonProperty("underline") boolean underline
    ) {
        this.text = text != null ? text : "";
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }

    public String getText() { return text; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isUnderline() { return underline; }

    public void setText(String text) { this.text = text; }
    public void setBold(boolean bold) { this.bold = bold; }
    public void setItalic(boolean italic) { this.italic = italic; }
    public void setUnderline(boolean underline) { this.underline = underline; }
}