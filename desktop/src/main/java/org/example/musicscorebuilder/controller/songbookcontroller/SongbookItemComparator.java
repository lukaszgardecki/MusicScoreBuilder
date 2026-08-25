package org.example.musicscorebuilder.controller.songbookcontroller;

import org.example.musicscorebuilder.components.SongbookItem;

import java.util.Comparator;

public class SongbookItemComparator implements Comparator<SongbookItem> {

    @Override
    public int compare(SongbookItem a, SongbookItem b) {
        if (a.type() == SongbookItem.Type.PARENT_DIR) return -1;
        if (b.type() == SongbookItem.Type.PARENT_DIR) return 1;

        if (a.type() != b.type()) {
            return a.type().compareTo(b.type());
        }

        String nameA = a.file() != null ? a.file().getName() : a.toString();
        String nameB = b.file() != null ? b.file().getName() : b.toString();

        return naturalCompare(nameA, nameB);
    }

    private int naturalCompare(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        int i1 = 0, i2 = 0;
        while (i1 < s1.length() && i2 < s2.length()) {
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);

            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                int num1 = 0;
                while (i1 < s1.length() && Character.isDigit(s1.charAt(i1))) {
                    num1 = num1 * 10 + (s1.charAt(i1) - '0');
                    i1++;
                }
                int num2 = 0;
                while (i2 < s2.length() && Character.isDigit(s2.charAt(i2))) {
                    num2 = num2 * 10 + (s2.charAt(i2) - '0');
                    i2++;
                }
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else {
                if (c1 != c2) {
                    return Character.compare(Character.toLowerCase(c1), Character.toLowerCase(c2));
                }
                i1++;
                i2++;
            }
        }
        return Integer.compare(s1.length(), s2.length());
    }
}