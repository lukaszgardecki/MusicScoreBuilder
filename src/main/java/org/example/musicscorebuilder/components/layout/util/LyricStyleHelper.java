package org.example.musicscorebuilder.components.layout.util;

import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;

import java.util.ArrayList;
import java.util.List;

public class LyricStyleHelper {

    public static class CharStyle {
        public boolean bold;
        public boolean italic;
        public boolean underline;

        public CharStyle(boolean bold, boolean italic, boolean underline) {
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
        }

        public boolean matches(CharStyle other) {
            return this.bold == other.bold && this.italic == other.italic && this.underline == other.underline;
        }
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
                textOutput.append(frag.getText());
                for (int i = 0; i < frag.getText().length(); i++) {
                    charStyles.add(new CharStyle(frag.isBold(), frag.isItalic(), frag.isUnderline()));
                }
            }
            if (!charStyles.isEmpty()) {
                CharStyle last = charStyles.get(charStyles.size() - 1);
                currentBold = last.bold;
                currentItalic = last.italic;
                currentUnderline = last.underline;
            }
        } else if (lyric != null && lyric.getText() != null && !lyric.getText().isEmpty()) {
            textOutput.append(lyric.getText());
            for (int i = 0; i < lyric.getText().length(); i++) {
                charStyles.add(new CharStyle(currentBold, currentItalic, currentUnderline));
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
            for (int i = selection.getStart(); i < selection.getEnd(); i++) {
                if (i < charStyles.size()) {
                    CharStyle s = charStyles.get(i);
                    if (mode == 1) s.bold = !s.bold;
                    if (mode == 2) s.italic = !s.italic;
                    if (mode == 3) s.underline = !s.underline;
                }
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

        for (int i = 0; i < removedCount; i++) {
            if (prefix < charStyles.size()) {
                charStyles.remove(prefix);
            }
        }

        for (int i = 0; i < insertedCount; i++) {
            int insertPos = Math.min(prefix + i, charStyles.size());
            charStyles.add(insertPos, new CharStyle(currentBold, currentItalic, currentUnderline));
        }

        while (charStyles.size() > newLen) {
            charStyles.removeLast();
        }
        while (charStyles.size() < newLen) {
            charStyles.add(new CharStyle(currentBold, currentItalic, currentUnderline));
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
                    : new CharStyle(currentBold, currentItalic, currentUnderline);

            int chunkEnd = i;
            while (chunkEnd < actualEnd && (chunkEnd >= charStyles.size() || charStyles.get(chunkEnd).matches(current))) {
                chunkEnd++;
            }

            String chunk = text.substring(i, chunkEnd);
            Text t = new Text(chunk);
            FontWeight weight = current.bold ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = current.italic ? FontPosture.ITALIC : FontPosture.REGULAR;
            t.setFont(Font.font(baseFont.getFamily(), weight, posture, baseFont.getSize()));

            width += t.getLayoutBounds().getWidth();
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
                    : new CharStyle(currentBold, currentItalic, currentUnderline);

            int end = start;
            while (end < text.length() && (end >= charStyles.size() || charStyles.get(end).matches(current))) {
                end++;
            }

            String chunk = text.substring(start, end);
            Text textNode = new Text(chunk);

            FontWeight weight = current.bold ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = current.italic ? FontPosture.ITALIC : FontPosture.REGULAR;

            textNode.setFont(Font.font(baseFont.getFamily(), weight, posture, baseFont.getSize()));
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
                    : new CharStyle(currentBold, currentItalic, currentUnderline);

            int end = start;
            while (end < text.length()) {
                CharStyle styleAtEnd = (end < charStyles.size())
                        ? charStyles.get(end)
                        : new CharStyle(currentBold, currentItalic, currentUnderline);
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