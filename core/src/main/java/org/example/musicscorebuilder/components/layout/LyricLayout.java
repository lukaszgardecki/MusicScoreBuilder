package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.SyllableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LyricLayout {

    private static final List<HyphenLayout> EMPTY_HYPHENS = Collections.emptyList();

    @FunctionalInterface
    public interface TextMeasurer {
        TextBounds measure(String text, double fontSize, boolean bold, boolean italic);

        record TextBounds(double width, double height) {}
    }

    private static TextMeasurer defaultMeasurer = (text, fontSize, bold, italic) -> {
        double width = (text != null) ? text.length() * fontSize * 0.55 : 0.0;
        double height = fontSize * 1.2;
        return new TextMeasurer.TextBounds(width, height);
    };

    public static void setDefaultMeasurer(TextMeasurer measurer) {
        if (measurer != null) {
            defaultMeasurer = measurer;
        }
    }

    public record FragmentLayout(
            LyricFragment fragment,
            double width,
            double relativeX,
            double underlineRelativeY,
            boolean bold,
            boolean italic,
            double fontSizeSp
    ) {}

    public record HyphenLayout(
            double modelX,
            double modelY,
            double scaleX,
            double fontSizeSp
    ) {}

    private record NotePos(SystemLayout system, double absX, NoteLayout noteLayout) {}

    private final Lyric lyric;
    private final NoteLayout noteLayout;
    private final int verse;
    private final List<FragmentLayout> fragmentLayouts = new ArrayList<>();
    private TextMeasurer textMeasurer;
    private double totalWidth = 0.0;
    private int lastKnownHash = -1;

    public LyricLayout(Lyric lyric, NoteLayout noteLayout) {
        this(lyric, noteLayout, defaultMeasurer);
    }

    public LyricLayout(Lyric lyric, NoteLayout noteLayout, TextMeasurer measurer) {
        this.lyric = lyric;
        this.noteLayout = noteLayout;
        this.verse = (lyric != null) ? lyric.getVerse() : 1;
        this.textMeasurer = (measurer != null) ? measurer : defaultMeasurer;
        refresh();
    }

    public Lyric getLyric() { return lyric; }
    public NoteLayout getNoteLayout() { return noteLayout; }
    public int getVerse() { return verse; }
    public SyllableType getType() { return lyric.getType(); }

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
        if (lyric != null && lyric.getFontSize() != null && lyric.getFontSize() > 0.0) {
            return lyric.getFontSize();
        }
        return (noteLayout != null && noteLayout.getScoreStyle() != null)
                ? noteLayout.getScoreStyle().getNoteLyricFontSize()
                : 12.0;
    }

    public double getRelY() {
        if (noteLayout == null) return 0.0;
        StaffLayout staff = noteLayout.getStaff();
        if (staff == null) return 0.0;
        return staff.getHeight() + staff.getSystemNoteBodyBottomOverflow();
    }

    public double getY() {
        if (noteLayout == null) return 0.0;
        StaffLayout staff = noteLayout.getStaff();
        if (staff == null) return 0.0;
        return staff.getY() + getRelY();
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
        if (lyric == null) return 0;
        int h = 17;
        h = 31 * h + lyric.getVerse();
        h = 31 * h + (lyric.getType() != null ? lyric.getType().hashCode() : 0);
        if (lyric.getFontSize() != null) {
            h = 31 * h + Double.hashCode(lyric.getFontSize());
        }
        List<LyricFragment> frags = lyric.getFragments();
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
        lastKnownHash = calculateLyricHash();
        if (lyric == null) return;

        List<LyricFragment> fragments = lyric.getFragments();
        if (fragments.isEmpty()) return;

        double fontSizeSp = getFontSize();
        double currentRelX = 0.0;

        for (LyricFragment frag : fragments) {
            if (frag == null || frag.getText() == null || frag.getText().isEmpty()) continue;

            boolean bold = frag.isBold();
            boolean italic = frag.isItalic();

            TextMeasurer.TextBounds bounds = textMeasurer.measure(frag.getText(), fontSizeSp, bold, italic);

            double fragWidthModel = bounds.width();
            double underlineRelYModel = bounds.height() * 0.8;

            fragmentLayouts.add(new FragmentLayout(
                    frag,
                    fragWidthModel,
                    currentRelX,
                    underlineRelYModel,
                    bold,
                    italic,
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
                    double margin = fontSize * 0.15;
                    double availableSpace = distance - (2 * margin);

                    if (availableSpace > 0) {
                        double defaultHyphenWidth = fontSize * 0.35;
                        double scaleX = Math.min(1.0, Math.max(0.15, availableSpace / defaultHyphenWidth));

                        double hyphenAbsX = startAbsX + (distance / 2.0);
                        double hyphenModelX = hyphenAbsX - currentPos.absX();
                        result.add(new HyphenLayout(hyphenModelX, getY(), scaleX, fontSize));
                    }
                } else {
                    double hyphenModelX = getStartX() + getTotalWidth() + (fontSize * 0.25);
                    result.add(new HyphenLayout(hyphenModelX, getY(), 0.8, fontSize));
                }
            }
        }

        if (type == SyllableType.MIDDLE || type == SyllableType.END) {
            NotePos prevPos = createNotePos(noteLayout.getPrevNoteInVoice());

            if (prevPos != null && currentPos.system() != prevPos.system()) {
                double hyphenModelX = getStartX() - (fontSize * 0.35);
                result.add(new HyphenLayout(hyphenModelX, getY(), 0.8, fontSize));
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