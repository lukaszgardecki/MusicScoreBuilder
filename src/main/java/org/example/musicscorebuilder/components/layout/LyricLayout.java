package org.example.musicscorebuilder.components.layout;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.musicscorebuilder.components.layout.util.LyricHyphenCalculator;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.SyllableType;
import org.example.musicscorebuilder.managers.FontManager;

import java.util.ArrayList;
import java.util.List;

public class LyricLayout {

    public record FragmentLayout(
            LyricFragment fragment,
            double width,
            double relativeX,
            double underlineRelativeY,
            FontWeight weight,
            FontPosture posture,
            double fontSizeSp
    ) {
        public Font getFont(double sp) {
            Font baseFont = FontManager.getFreeSerifFont(fontSizeSp * sp);
            return Font.font(baseFont.getFamily(), weight, posture, fontSizeSp * sp);
        }
    }

    public record HyphenLayout(
            double modelX,
            double modelY,
            double scaleX,
            double fontSizeSp
    ) {
        public Font getFont(double sp) {
            Font baseFont = FontManager.getFreeSerifFont(fontSizeSp * sp);
            return Font.font(baseFont.getFamily(), fontSizeSp * sp);
        }
    }

    private final Lyric lyric;
    private final NoteLayout noteLayout;
    private final List<FragmentLayout> fragmentLayouts = new ArrayList<>();
    private double totalWidth = 0.0;

    public LyricLayout(Lyric lyric, NoteLayout noteLayout) {
        this.lyric = lyric;
        this.noteLayout = noteLayout;
        refresh();
    }

    public Lyric getLyric() { return lyric; }
    public NoteLayout getNoteLayout() { return noteLayout; }
    public int getVerse() { return lyric.getVerse(); }
    public SyllableType getType() { return lyric.getType(); }
    public double getTotalWidth() { return totalWidth; }
    public List<FragmentLayout> getFragmentLayouts() { return fragmentLayouts; }

    public double getFontSize() {
        if (lyric != null && lyric.getFontSize() != null && lyric.getFontSize() > 0.0) {
            return lyric.getFontSize();
        }
        return noteLayout.getScoreStyle().getNoteLyricFontSize();
    }

    public double getModelY() {
        StaffLayout staff = noteLayout.getStaff();
        if (staff == null) return 0.0;
        double staffBottomY = staff.getY() + staff.getHeight();
        return staffBottomY + 2.5 + ((getVerse() - 1) * 1.5);
    }

    public double getNoteCenterX() {
        return noteLayout.getX() + (noteLayout.getFontWidth() / 2.0);
    }

    public double getStartX() {
        return getNoteCenterX() - (totalWidth / 2.0);
    }

    public void refresh() {
        fragmentLayouts.clear();
        totalWidth = 0.0;

        if (lyric == null || lyric.getText() == null || lyric.getText().trim().isEmpty()) {
            return;
        }

        List<LyricFragment> fragments = lyric.getFragments();
        if (fragments == null || fragments.isEmpty()) {
            fragments = List.of(new LyricFragment(lyric.getText(), false, false, false));
        }

        double fontSizeSp = getFontSize();
        Font baseFont = FontManager.getFreeSerifFont(fontSizeSp);
        String fontFamily = baseFont.getFamily();

        double currentRelX = 0.0;

        for (LyricFragment frag : fragments) {
            FontWeight weight = frag.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = frag.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;
            Font font = Font.font(fontFamily, weight, posture, fontSizeSp);

            Text measureText = new Text(frag.getText());
            measureText.setFont(font);

            double fragWidthModel = measureText.getLayoutBounds().getWidth();
            double underlineRelYModel = measureText.getLayoutBounds().getHeight() * 0.8;

            fragmentLayouts.add(new FragmentLayout(
                    frag,
                    fragWidthModel,
                    currentRelX,
                    underlineRelYModel,
                    weight,
                    posture,
                    fontSizeSp
            ));

            currentRelX += fragWidthModel;
        }

        totalWidth = currentRelX;
    }

    public List<HyphenLayout> computeHyphenLayouts(ScoreLayout scoreLayout) {
        return LyricHyphenCalculator.calculateHyphens(this, scoreLayout);
    }
}