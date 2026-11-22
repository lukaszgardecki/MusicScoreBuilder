package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.SyllableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LyricLayout {

    private static final List<HyphenLayout> EMPTY_HYPHENS = Collections.emptyList();

    @FunctionalInterface
    public interface TextMeasurer {
        TextBounds measure(String text, double fontSize, boolean bold, boolean italic);

        record TextBounds(double width, double height) {}
    }

    // Rekord do szybkiego wyszukiwania w cache bez alokacji Stringów
    private record CacheKey(String text, float fontSize, boolean bold, boolean italic) {}

    // Szybki, bezkonfliktowy cache pomiarów tekstu
    private static final Map<CacheKey, TextMeasurer.TextBounds> MEASURE_CACHE = new ConcurrentHashMap<>(1024);

    private static TextMeasurer defaultMeasurer = (text, fontSize, bold, italic) -> {
        double width = (text != null) ? text.length() * fontSize * 0.55 : 0.0;
        double height = fontSize * 1.2;
        return new TextMeasurer.TextBounds(width, height);
    };

    public static void setDefaultMeasurer(TextMeasurer measurer) {
        if (measurer != null) {
            defaultMeasurer = measurer;
            MEASURE_CACHE.clear();
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

    private final Lyric lyric;
    private final NoteLayout noteLayout;
    private final int verse;
    private final List<FragmentLayout> fragmentLayouts = new ArrayList<>(2);
    private TextMeasurer textMeasurer;
    private double totalWidth = 0.0;
    private boolean isDirty = true;

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

    public void markDirty() {
        this.isDirty = true;
    }

    public double getTotalWidth() {
        if (isDirty) refresh();
        return totalWidth;
    }

    public List<FragmentLayout> getFragmentLayouts() {
        if (isDirty) refresh();
        return fragmentLayouts;
    }

    public double getStartX() {
        return getNoteCenterX() - (getTotalWidth() / 2.0);
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

    public void refresh() {
        fragmentLayouts.clear();
        totalWidth = 0.0;
        isDirty = false;
        if (lyric == null) return;

        List<LyricFragment> fragments = lyric.getFragments();
        int fragCount = fragments.size();
        if (fragCount == 0) return;

        double fontSizeSp = getFontSize();
        float fontKey = (float) fontSizeSp;
        double currentRelX = 0.0;

        for (int i = 0; i < fragCount; i++) {
            LyricFragment frag = fragments.get(i);
            if (frag == null || frag.getText() == null || frag.getText().isEmpty()) continue;

            boolean bold = frag.isBold();
            boolean italic = frag.isItalic();
            String text = frag.getText();

            CacheKey key = new CacheKey(text, fontKey, bold, italic);
            TextMeasurer.TextBounds bounds = MEASURE_CACHE.get(key);

            if (bounds == null) {
                bounds = textMeasurer.measure(text, fontSizeSp, bold, italic);
                MEASURE_CACHE.put(key, bounds);
            }

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

    public List<HyphenLayout> computeHyphenLayouts() {
        SyllableType type = getType();
        if (type == SyllableType.SINGLE || noteLayout == null || noteLayout.getNote() == null) {
            return EMPTY_HYPHENS;
        }

        SystemLayout currentSystem = getSystem(noteLayout);
        double currentAbsX = getAbsX(noteLayout);
        double fontSize = getFontSize();

        if (type == SyllableType.BEGIN || type == SyllableType.MIDDLE) {
            NoteLayout nextNote = noteLayout.getNextNoteInVoice();
            if (nextNote != null) {
                SystemLayout nextSystem = getSystem(nextNote);
                double nextAbsX = getAbsX(nextNote);

                if (currentSystem == nextSystem && currentSystem != null) {
                    double startAbsX = currentAbsX + getStartX() + getTotalWidth();
                    double endAbsX = nextAbsX;

                    LyricLayout nextLyric = findLyricForVerse(nextNote, getVerse());
                    if (nextLyric != null && !nextLyric.getFragmentLayouts().isEmpty()) {
                        endAbsX += nextLyric.getStartX();
                    } else {
                        endAbsX += nextNote.getFontWidth() / 2.0;
                    }

                    double distance = endAbsX - startAbsX;
                    double margin = fontSize * 0.15;
                    double availableSpace = distance - (2 * margin);

                    if (availableSpace > 0) {
                        double defaultHyphenWidth = fontSize * 0.35;
                        double scaleX = Math.min(1.0, Math.max(0.15, availableSpace / defaultHyphenWidth));

                        double hyphenAbsX = startAbsX + (distance / 2.0);
                        double hyphenModelX = hyphenAbsX - currentAbsX;

                        List<HyphenLayout> result = new ArrayList<>(1);
                        result.add(new HyphenLayout(hyphenModelX, getY(), scaleX, fontSize));
                        return result;
                    }
                } else {
                    double hyphenModelX = getStartX() + getTotalWidth() + (fontSize * 0.25);
                    List<HyphenLayout> result = new ArrayList<>(1);
                    result.add(new HyphenLayout(hyphenModelX, getY(), 0.8, fontSize));
                    return result;
                }
            }
        }

        if (type == SyllableType.MIDDLE || type == SyllableType.END) {
            NoteLayout prevNote = noteLayout.getPrevNoteInVoice();
            if (prevNote != null) {
                SystemLayout prevSystem = getSystem(prevNote);
                if (currentSystem != prevSystem && currentSystem != null) {
                    double hyphenModelX = getStartX() - (fontSize * 0.35);
                    List<HyphenLayout> result = new ArrayList<>(1);
                    result.add(new HyphenLayout(hyphenModelX, getY(), 0.8, fontSize));
                    return result;
                }
            }
        }

        return EMPTY_HYPHENS;
    }

    public List<HyphenLayout> computeHyphenLayouts(ScoreLayout scoreLayout) {
        return computeHyphenLayouts();
    }

    private static LyricLayout findLyricForVerse(NoteLayout note, int targetVerse) {
        List<LyricLayout> lyrics = note.getLyrics();
        if (lyrics == null) return null;
        int count = lyrics.size();
        for (int i = 0; i < count; i++) {
            LyricLayout ly = lyrics.get(i);
            if (ly.getVerse() == targetVerse) {
                return ly;
            }
        }
        return null;
    }

    private static SystemLayout getSystem(NoteLayout target) {
        if (target == null) return null;
        StaffLayout staff = target.getStaff();
        return (staff != null && staff.getParent() != null) ? staff.getParent().getParent() : null;
    }

    private static double getAbsX(NoteLayout target) {
        if (target == null) return 0.0;
        double absX = target.getX();
        SegmentLayout segment = target.getSegment();
        if (segment != null) {
            absX += segment.getX();
            MeasureLayout measure = segment.getParent();
            if (measure != null) {
                absX += measure.getX();
            }
        }
        return absX;
    }
}