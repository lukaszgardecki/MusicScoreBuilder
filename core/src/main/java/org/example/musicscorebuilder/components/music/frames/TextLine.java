package org.example.musicscorebuilder.components.music.frames;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.musicscorebuilder.components.music.LyricFragment;

import java.util.ArrayList;
import java.util.List;

public class TextLine {

    @JsonProperty("fragments")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<LyricFragment> fragments = new ArrayList<>();

    @JsonProperty("fontSize")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double fontSize;

    public TextLine() {}

    @JsonCreator
    public TextLine(
            @JsonProperty("fragments") List<LyricFragment> fragments,
            @JsonProperty("fontSize") Double fontSize
    ) {
        this.fragments = fragments != null ? fragments : new ArrayList<>();
        this.fontSize = fontSize;
    }

    public void addFragment(LyricFragment fragment) {
        if (fragment == null || fragment.getText() == null || fragment.getText().isEmpty()) {
            return;
        }

        if (!fragments.isEmpty()) {
            LyricFragment last = fragments.get(fragments.size() - 1);
            if (last.isBold() == fragment.isBold()
                    && last.isItalic() == fragment.isItalic()
                    && last.isUnderline() == fragment.isUnderline()) {

                last.setText(last.getText() + fragment.getText());
                return;
            }
        }

        fragments.add(new LyricFragment(
                fragment.getText(),
                fragment.isBold(),
                fragment.isItalic(),
                fragment.isUnderline()
        ));
    }

    public List<LyricFragment> getFragments() { return fragments; }
    public void setFragments(List<LyricFragment> fragments) { this.fragments = fragments; }

    public Double getFontSize() { return fontSize; }
    public void setFontSize(Double fontSize) { this.fontSize = fontSize; }
}