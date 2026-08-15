package org.example.musicscorebuilder.components.layout.util;

import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LyricStyleHelper {

    public static class CharStyle {
        public final boolean bold;
        public final boolean italic;
        public final boolean underline;

        private static final CharStyle[] CACHE = new CharStyle[8];

        static {
            for (int i = 0; i < 8; i++) {
                boolean b = (i & 1) != 0;
                boolean it = (i & 2) != 0;
                boolean u = (i & 4) != 0;
                CACHE[i] = new CharStyle(b, it, u);
            }
        }

        private CharStyle(boolean bold, boolean italic, boolean underline) {
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
        }

        public static CharStyle get(boolean bold, boolean italic, boolean underline) {
            int index = (bold ? 1 : 0) | (italic ? 2 : 0) | (underline ? 4 : 0);
            return CACHE[index];
        }

        public boolean matches(CharStyle other) {
            return this == other || (this.bold == other.bold && this.italic == other.italic && this.underline == other.underline);
        }
    }

    private static final Text MEASURE_TEXT = new Text();
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    private static Font getFont(String family, FontWeight weight, FontPosture posture, double size) {
        String key = family + "_" + weight + "_" + posture + "_" + size;
        return FONT_CACHE.computeIfAbsent(key, k -> Font.font(family, weight, posture, size));
    }

    private final List<CharStyle> charStyles = new ArrayList<>();
    private boolean currentBold = false;
    private boolean currentItalic = false;
    private boolean currentUnderline = false;

    public boolean isCurrentBold() { return currentBold; }
    public boolean isCurrentItalic() { return currentItalic; }
    public boolean isCurrentUnderline() { return currentUnderline; }

    public void loadLyric(Lyric lyric, StringBuilder textOutput) {
        charStyles.clear();
        textOutput.setLength(0);

        if (lyric != null && lyric.getFragments() != null && !lyric.getFragments().isEmpty()) {
            for (LyricFragment frag : lyric.getFragments()) {
                String text = frag.getText();
                textOutput.append(text);
                CharStyle style = CharStyle.get(frag.isBold(), frag.isItalic(), frag.isUnderline());
                for (int i = 0, len = text.length(); i < len; i++) {
                    charStyles.add(style);
                }
            }
            if (!charStyles.isEmpty()) {
                CharStyle last = charStyles.get(charStyles.size() - 1);
                currentBold = last.bold;
                currentItalic = last.italic;
                currentUnderline = last.underline;
            }
        } else if (lyric != null && lyric.getText() != null && !lyric.getText().isEmpty()) {
            String text = lyric.getText();
            textOutput.append(text);
            CharStyle style = CharStyle.get(currentBold, currentItalic, currentUnderline);
            for (int i = 0, len = text.length(); i < len; i++) {
                charStyles.add(style);
            }
        }
    }

    public void updateActiveStyleForCaret(int caretPos) {
        if (caretPos > 0 && caretPos <= charStyles.size()) {
            CharStyle s = charStyles.get(caretPos - 1);
            currentBold = s.bold;
            currentItalic = s.italic;
            currentUnderline = s.underline;
        }
    }

    public void toggleStyle(int mode, IndexRange selection) {
        if (selection != null && selection.getLength() > 0) {
            int start = selection.getStart();
            int end = Math.min(selection.getEnd(), charStyles.size());
            for (int i = start; i < end; i++) {
                CharStyle s = charStyles.get(i);
                boolean b = (mode == 1) ? !s.bold : s.bold;
                boolean it = (mode == 2) ? !s.italic : s.italic;
                boolean u = (mode == 3) ? !s.underline : s.underline;
                charStyles.set(i, CharStyle.get(b, it, u));
            }
        } else {
            if (mode == 1) currentBold = !currentBold;
            if (mode == 2) currentItalic = !currentItalic;
            if (mode == 3) currentUnderline = !currentUnderline;
        }
    }

    public void syncCharStyles(String oldText, String newText) {
        if (oldText == null) oldText = "";
        if (newText == null) newText = "";

        int oldLen = oldText.length();
        int newLen = newText.length();

        int prefix = 0;
        while (prefix < oldLen && prefix < newLen && oldText.charAt(prefix) == newText.charAt(prefix)) {
            prefix++;
        }

        int suffix = 0;
        while (suffix < (oldLen - prefix) && suffix < (newLen - prefix)
                && oldText.charAt(oldLen - 1 - suffix) == newText.charAt(newLen - 1 - suffix)) {
            suffix++;
        }

        int removedCount = oldLen - prefix - suffix;
        int insertedCount = newLen - prefix - suffix;

        if (removedCount > 0 && prefix < charStyles.size()) {
            int removeEnd = Math.min(prefix + removedCount, charStyles.size());
            charStyles.subList(prefix, removeEnd).clear();
        }

        if (insertedCount > 0) {
            CharStyle currentStyle = CharStyle.get(currentBold, currentItalic, currentUnderline);
            int insertPos = Math.min(prefix, charStyles.size());
            for (int i = 0; i < insertedCount; i++) {
                charStyles.add(insertPos + i, currentStyle);
            }
        }

        while (charStyles.size() > newLen) {
            charStyles.removeLast();
        }
        CharStyle defaultStyle = CharStyle.get(currentBold, currentItalic, currentUnderline);
        while (charStyles.size() < newLen) {
            charStyles.add(defaultStyle);
        }
    }

    public double measureWidth(String text, int start, int end, Font baseFont) {
        if (text == null || start >= end || start >= text.length()) return 0.0;
        int actualEnd = Math.min(end, text.length());

        double width = 0.0;
        int i = start;
        while (i < actualEnd) {
            CharStyle current = (i < charStyles.size())
                    ? charStyles.get(i)
                    : CharStyle.get(currentBold, currentItalic, currentUnderline);

            int chunkEnd = i;
            while (chunkEnd < actualEnd && (chunkEnd >= charStyles.size() || charStyles.get(chunkEnd).matches(current))) {
                chunkEnd++;
            }

            String chunk = text.substring(i, chunkEnd);

            FontWeight weight = current.bold ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = current.italic ? FontPosture.ITALIC : FontPosture.REGULAR;
            Font font = getFont(baseFont.getFamily(), weight, posture, baseFont.getSize());

            MEASURE_TEXT.setText(chunk);
            MEASURE_TEXT.setFont(font);

            width += MEASURE_TEXT.getLayoutBounds().getWidth();
            i = chunkEnd;
        }
        return width;
    }

    public void rebuildTextFlow(TextFlow textDisplay, String text, Font baseFont) {
        textDisplay.getChildren().clear();
        if (text == null || text.isEmpty()) return;

        int start = 0;
        while (start < text.length()) {
            CharStyle current = (start < charStyles.size())
                    ? charStyles.get(start)
                    : CharStyle.get(currentBold, currentItalic, currentUnderline);

            int end = start;
            while (end < text.length() && (end >= charStyles.size() || charStyles.get(end).matches(current))) {
                end++;
            }

            String chunk = text.substring(start, end);
            Text textNode = new Text(chunk);

            FontWeight weight = current.bold ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = current.italic ? FontPosture.ITALIC : FontPosture.REGULAR;

            textNode.setFont(getFont(baseFont.getFamily(), weight, posture, baseFont.getSize()));
            textNode.setUnderline(current.underline);
            textNode.setFill(Color.BLACK);

            textDisplay.getChildren().add(textNode);
            start = end;
        }
    }

    public List<LyricFragment> exportToFragments(String text) {
        List<LyricFragment> result = new ArrayList<>();
        if (text == null || text.isEmpty()) return result;

        int start = 0;
        while (start < text.length()) {
            CharStyle current = (start < charStyles.size())
                    ? charStyles.get(start)
                    : CharStyle.get(currentBold, currentItalic, currentUnderline);

            int end = start;
            while (end < text.length()) {
                CharStyle styleAtEnd = (end < charStyles.size())
                        ? charStyles.get(end)
                        : CharStyle.get(currentBold, currentItalic, currentUnderline);
                if (!styleAtEnd.matches(current)) {
                    break;
                }
                end++;
            }
            result.add(new LyricFragment(text.substring(start, end), current.bold, current.italic, current.underline));
            start = end;
        }
        return result;
    }
}