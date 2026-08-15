package org.example.musicscorebuilder.components.layout;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.SyllableType;
import org.example.musicscorebuilder.managers.FontManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LyricLayout {

    private static final List<HyphenLayout> EMPTY_HYPHENS = Collections.emptyList();
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    public static Font getFont(FontWeight weight, FontPosture posture, double size) {
        double rounded = Math.round(size * 10.0) / 10.0;
        String key = weight + "_" + posture + "_" + rounded;
        return FONT_CACHE.computeIfAbsent(key, k -> {
            Font baseFont = FontManager.getFreeSerifFont(rounded);
            if (weight == FontWeight.BOLD || posture == FontPosture.ITALIC) {
                return Font.font(baseFont.getFamily(), weight, posture, rounded);
            }
            return baseFont;
        });
    }

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
            return LyricLayout.getFont(weight, posture, fontSizeSp * sp);
        }
    }

    public record HyphenLayout(
            double modelX,
            double modelY,
            double scaleX,
            double fontSizeSp
    ) {
        public Font getFont(double sp) {
            return LyricLayout.getFont(FontWeight.NORMAL, FontPosture.REGULAR, fontSizeSp * sp);
        }
    }

    private record NotePos(SystemLayout system, double absX, NoteLayout noteLayout) {}

    private final Lyric lyric;
    private final NoteLayout noteLayout;
    private final int verse;
    private final List<FragmentLayout> fragmentLayouts = new ArrayList<>();
    private double totalWidth = 0.0;
    private int lastKnownHash = -1;

    public LyricLayout(Lyric lyric, NoteLayout noteLayout) {
        this.lyric = lyric;
        this.noteLayout = noteLayout;
        this.verse = (lyric != null) ? lyric.getVerse() : 1;
        refresh();
    }

    public Lyric getLyric() {
        if (noteLayout != null && noteLayout.getNote() != null) {
            Lyric current = noteLayout.getNote().getLyric(verse);
            if (current != null) return current;
        }
        return lyric;
    }

    public NoteLayout getNoteLayout() { return noteLayout; }
    public int getVerse() { return verse; }

    public SyllableType getType() {
        Lyric l = getLyric();
        return (l != null) ? l.getType() : SyllableType.SINGLE;
    }

    public double getTotalWidth() {
        checkAndRefreshIfStale();
        return totalWidth;
    }

    public List<FragmentLayout> getFragmentLayouts() {
        checkAndRefreshIfStale();
        return fragmentLayouts;
    }

    public double getStartX() {
        checkAndRefreshIfStale();
        return getNoteCenterX() - (totalWidth / 2.0);
    }

    public double getFontSize() {
        Lyric l = getLyric();
        if (l != null && l.getFontSize() != null && l.getFontSize() > 0.0) {
            return l.getFontSize();
        }
        return (noteLayout != null && noteLayout.getScoreStyle() != null)
                ? noteLayout.getScoreStyle().getNoteLyricFontSize()
                : 12.0;
    }

    public double getModelY() {
        if (noteLayout == null) return 0.0;
        StaffLayout staff = noteLayout.getStaff();
        if (staff == null) return 0.0;
        double staffBottomY = staff.getY() + staff.getHeight();
        return staffBottomY + 2.5 + ((getVerse() - 1) * 1.5);
    }

    public double getNoteCenterX() {
        if (noteLayout == null) return 0.0;
        return noteLayout.getX() + (noteLayout.getFontWidth() / 2.0);
    }

    public void checkAndRefreshIfStale() {
        int currentHash = calculateLyricHash();
        if (currentHash != lastKnownHash) {
            refresh();
        }
    }

    private int calculateLyricHash() {
        Lyric l = getLyric();
        if (l == null) return 0;
        int h = 17;
        h = 31 * h + l.getVerse();
        h = 31 * h + (l.getType() != null ? l.getType().hashCode() : 0);
        if (l.getFontSize() != null) {
            h = 31 * h + Double.hashCode(l.getFontSize());
        }
        List<LyricFragment> frags = l.getFragments();
        if (frags != null) {
            for (LyricFragment f : frags) {
                if (f != null && f.getText() != null) {
                    h = 31 * h + f.getText().hashCode();
                    h = 31 * h + (f.isBold() ? 1 : 0);
                    h = 31 * h + (f.isItalic() ? 2 : 0);
                    h = 31 * h + (f.isUnderline() ? 4 : 0);
                }
            }
        }
        return h;
    }

    public void refresh() {
        fragmentLayouts.clear();
        totalWidth = 0.0;

        Lyric currentLyric = getLyric();
        lastKnownHash = calculateLyricHash();
        if (currentLyric == null) return;

        List<LyricFragment> fragments = currentLyric.getFragments();
        if (fragments.isEmpty()) return;

        double fontSizeSp = getFontSize();
        double currentRelX = 0.0;

        for (LyricFragment frag : fragments) {
            if (frag == null || frag.getText() == null || frag.getText().isEmpty()) continue;

            FontWeight weight = frag.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = frag.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;
            Font font = getFont(weight, posture, fontSizeSp);

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
        SyllableType type = getType();
        if (type == SyllableType.SINGLE || scoreLayout == null || noteLayout == null || noteLayout.getNote() == null) {
            return EMPTY_HYPHENS;
        }

        List<HyphenLayout> result = new ArrayList<>();
        NotePos currentPos = createNotePos(this.noteLayout);
        if (currentPos == null) return EMPTY_HYPHENS;

        double fontSize = getFontSize();

        // 1. Myślnik PO sylabie (dla BEGIN i MIDDLE)
        if (type == SyllableType.BEGIN || type == SyllableType.MIDDLE) {
            NotePos nextPos = createNotePos(noteLayout.getNextNoteInVoice());

            if (nextPos != null) {
                if (currentPos.system() == nextPos.system()) {
                    double startAbsX = currentPos.absX() + getStartX() + getTotalWidth();
                    double endAbsX = nextPos.absX();

                    LyricLayout nextLyric = null;
                    if (nextPos.noteLayout().getLyrics() != null) {
                        for (LyricLayout l : nextPos.noteLayout().getLyrics()) {
                            if (l.getVerse() == getVerse()) {
                                nextLyric = l;
                                break;
                            }
                        }
                    }

                    if (nextLyric != null && !nextLyric.getFragmentLayouts().isEmpty()) {
                        endAbsX += nextLyric.getStartX();
                    } else {
                        endAbsX += nextPos.noteLayout().getFontWidth() / 2.0;
                    }

                    double distance = endAbsX - startAbsX;
                    double margin = fontSize * 0.15; // Bezpieczny odstęp od tekstu z obu stron
                    double availableSpace = distance - (2 * margin);

                    if (availableSpace > 0) {
                        double defaultHyphenWidth = fontSize * 0.35;
                        double scaleX = Math.min(1.0, Math.max(0.15, availableSpace / defaultHyphenWidth));

                        double hyphenAbsX = startAbsX + (distance / 2.0);
                        double hyphenModelX = hyphenAbsX - currentPos.absX();
                        result.add(new HyphenLayout(hyphenModelX, getModelY(), scaleX, fontSize));
                    }
                } else {
                    // Koniec obecnego systemu (złamanie linii)
                    double hyphenModelX = getStartX() + getTotalWidth() + (fontSize * 0.25);
                    result.add(new HyphenLayout(hyphenModelX, getModelY(), 0.8, fontSize));
                }
            }
        }

        // 2. Myślnik PRZED sylabą na nowej linii (dla MIDDLE i END)
        if (type == SyllableType.MIDDLE || type == SyllableType.END) {
            NotePos prevPos = createNotePos(noteLayout.getPrevNoteInVoice());

            if (prevPos != null && currentPos.system() != prevPos.system()) {
                // Początek nowego systemu (złamanie linii)
                double hyphenModelX = getStartX() - (fontSize * 0.35);
                result.add(new HyphenLayout(hyphenModelX, getModelY(), 0.8, fontSize));
            }
        }

        return result;
    }

    private NotePos createNotePos(NoteLayout target) {
        if (target == null) return null;

        SystemLayout system = (target.getStaff() != null) ? target.getStaff().getParent().getParent() : null;

        double absX = target.getX();
        if (target.getSegment() != null) {
            absX += target.getSegment().getX();
            if (target.getSegment().getParent() != null) {
                absX += target.getSegment().getParent().getX();
            }
        }
        return new NotePos(system, absX, target);
    }
}