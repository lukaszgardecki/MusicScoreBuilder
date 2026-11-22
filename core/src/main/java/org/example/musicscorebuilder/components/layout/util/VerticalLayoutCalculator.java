//package org.example.musicscorebuilder.components.layout.util;
//
//import org.example.musicscorebuilder.components.layout.*;
//import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
//
//import java.util.List;
//
//public class VerticalLayoutCalculator {
//
//    public void applySystemsVerticalLayout(ScoreLayout scoreLayout, ScoreStyle style) {
//        List<PageLayout> pages = scoreLayout.getPages();
//        for (int p = 0; p < pages.size(); p++) {
//            List<SystemLayout> systems = pages.get(p).getSystems();
//            for (int s = 0; s < systems.size(); s++) {
//                applyStaffVerticalLayout(systems.get(s), style);
//            }
//        }
//    }
//
//    public void applyStaffVerticalLayout(SystemLayout system, ScoreStyle style) {
//        List<MeasureLayout> measures = system.getMeasures();
//        if (measures.isEmpty()) return;
//
//        int staffCount = measures.get(0).getStaffs().size();
//        if (staffCount == 0) return;
//
//        double currentY = 0.0;
//        double prevNominalHeight = 0.0;
//        double prevLyricBottomOverflow = 0.0;
//
//        for (int k = 0; k < staffCount; k++) {
//            // 1. Obliczenie maxSystemNoteBodyOverflow w systemie
//            double maxSystemNoteBodyOverflow = 0.0;
//            for (MeasureLayout measure : measures) {
//                if (k < measure.getStaffs().size()) {
//                    maxSystemNoteBodyOverflow = Math.max(maxSystemNoteBodyOverflow, measure.getStaffs().get(k).getNoteBodyBottomOverflow());
//                }
//            }
//
//            // 2. Ustawienie tymczasowego Y oraz overflow ciała nut
//            for (MeasureLayout measure : measures) {
//                if (k < measure.getStaffs().size()) {
//                    StaffLayout staff = measure.getStaffs().get(k);
//                    staff.setSystemNoteBodyBottomOverflow(maxSystemNoteBodyOverflow);
//                    staff.setY(currentY);
//                }
//            }
//
//            // 3. Wyznaczenie przepełnień dla bieżącego poziomu Y
//            double maxTopOverflow = 0.0;
//            double maxLyricBottomOverflow = 0.0;
//            double nominalHeight = 0.0;
//
//            for (MeasureLayout measure : measures) {
//                if (k < measure.getStaffs().size()) {
//                    StaffLayout staff = measure.getStaffs().get(k);
//                    maxTopOverflow = Math.max(maxTopOverflow, staff.getTopOverflow());
//                    nominalHeight = Math.max(nominalHeight, staff.getHeight());
//                    maxLyricBottomOverflow = Math.max(maxLyricBottomOverflow, staff.getLyricBottomOverflow());
//                }
//            }
//
//            // 4. Obliczenie docelowego Y
//            if (k == 0) {
//                currentY = 0.0;
//            } else {
//                if (prevLyricBottomOverflow > 0.0 || maxTopOverflow > 0.0) {
//                    double absoluteBottomTextY = currentY + prevNominalHeight + prevLyricBottomOverflow;
//                    currentY = absoluteBottomTextY + maxTopOverflow;
//                } else {
//                    currentY += style.getStaffSpacing();
//                }
//
//                for (MeasureLayout measure : measures) {
//                    if (k < measure.getStaffs().size()) {
//                        measure.getStaffs().get(k).setY(currentY);
//                    }
//                }
//            }
//
//            // 5. Ustawienie odstępu pod poprzednią pięciolinią
//            if (k > 0) {
//                int prevStaffIdx = k - 1;
//                double prevStaffY = measures.get(0).getStaffs().get(prevStaffIdx).getY();
//                double clearance = (currentY - prevStaffY) - prevNominalHeight;
//
//                for (MeasureLayout measure : measures) {
//                    if (prevStaffIdx < measure.getStaffs().size()) {
//                        measure.getStaffs().get(prevStaffIdx).setSpaceBelow(clearance);
//                    }
//                }
//            }
//
//            prevNominalHeight = nominalHeight;
//            prevLyricBottomOverflow = maxLyricBottomOverflow;
//        }
//
//        // 6. Wyczyszczenie odstępu dla ostatniej pięciolinii
//        int lastStaffIdx = staffCount - 1;
//        for (MeasureLayout measure : measures) {
//            if (lastStaffIdx < measure.getStaffs().size()) {
//                measure.getStaffs().get(lastStaffIdx).setSpaceBelow(0.0);
//            }
//        }
//    }
//
//    public void applyPagesVerticalLayout(ScoreLayout scoreLayout, ScoreStyle style) {
//        List<PageLayout> pages = scoreLayout.getPages();
//        for (int p = 0; p < pages.size(); p++) {
//            PageLayout page = pages.get(p);
//            List<PageBlockLayout> blocks = page.getBlocks();
//            if (blocks.isEmpty()) continue;
//
//            double currentY = page.getMarginTop();
//
//            for (int i = 0; i < blocks.size(); i++) {
//                PageBlockLayout block = blocks.get(i);
//                if (block instanceof SystemLayout system) {
//                    boolean nextIsSystem = (i + 1 < blocks.size()) && (blocks.get(i + 1) instanceof SystemLayout);
//                    double bottomOverflow = system.getBottomOverflow();
//                    double spacing = nextIsSystem ? style.getSystemSpacing() : 0.0;
//
//                    system.setSpaceBelow(spacing);
//                    currentY += system.getTopOverflow();
//                    system.setY(currentY);
//
//                    // Zachowanie pełnego odstępu miedzy systemami z uwzględnieniem ewentualnego przepełnienia
//                    currentY += (system.getNominalHeight() + bottomOverflow + spacing);
//                } else {
//                    block.setY(currentY);
//                    currentY += block.getHeight();
//                }
//            }
//        }
//    }
//}

package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.List;

public class VerticalLayoutCalculator {

    public void applySystemsVerticalLayout(ScoreLayout scoreLayout, ScoreStyle style) {
        List<PageLayout> pages = scoreLayout.getPages();
        int pageCount = pages.size();
        for (int p = 0; p < pageCount; p++) {
            List<SystemLayout> systems = pages.get(p).getSystems();
            int systemCount = systems.size();
            for (int s = 0; s < systemCount; s++) {
                applyStaffVerticalLayout(systems.get(s), style);
            }
        }
    }

    public void applyStaffVerticalLayout(SystemLayout system, ScoreStyle style) {
        List<MeasureLayout> measures = system.getMeasures();
        int measureCount = measures.size();
        if (measureCount == 0) return;

        List<StaffLayout> firstMeasureStaffs = measures.get(0).getStaffs();
        int staffCount = firstMeasureStaffs.size();
        if (staffCount == 0) return;

        double currentY = 0.0;
        double prevNominalHeight = 0.0;
        double prevLyricBottomOverflow = 0.0;

        double noteBodyTime = 0;
        double topTime = 0;
        double lyricTime = 0;

        for (int k = 0; k < staffCount; k++) {
            double maxSystemNoteBodyOverflow = 0.0;
            double maxTopOverflow = 0.0;
            double maxLyricBottomOverflow = 0.0;
            double nominalHeight = 0.0;

            for (int m = 0; m < measureCount; m++) {
                List<StaffLayout> staffs = measures.get(m).getStaffs();
                if (k < staffs.size()) {
                    StaffLayout staff = staffs.get(k);
                    staff.setY(currentY);

                    long t1 = System.nanoTime();
                    double noteBodyOverflow = staff.getNoteBodyBottomOverflow();
                    noteBodyTime += System.nanoTime() - t1;


                    if (noteBodyOverflow > maxSystemNoteBodyOverflow) {
                        maxSystemNoteBodyOverflow = noteBodyOverflow;
                    }
                    long t2 = System.nanoTime();
                    double topOverflow = staff.getTopOverflow();
                    topTime += System.nanoTime() - t2;

                    if (topOverflow > maxTopOverflow) {
                        maxTopOverflow = topOverflow;
                    }

                    double height = staff.getHeight();
                    if (height > nominalHeight) {
                        nominalHeight = height;
                    }

                    long t3 = System.nanoTime();
                    double lyricOverflow = staff.getLyricBottomOverflow();
                    lyricTime += System.nanoTime() - t3;

                    if (lyricOverflow > maxLyricBottomOverflow) {
                        maxLyricBottomOverflow = lyricOverflow;
                    }
                }
            }

            for (int m = 0; m < measureCount; m++) {
                List<StaffLayout> staffs = measures.get(m).getStaffs();
                if (k < staffs.size()) {
                    staffs.get(k).setSystemNoteBodyBottomOverflow(maxSystemNoteBodyOverflow);
                }
            }

            if (k > 0) {
                if (prevLyricBottomOverflow > 0.0 || maxTopOverflow > 0.0) {
                    double absoluteBottomTextY = currentY + prevNominalHeight + prevLyricBottomOverflow;
                    currentY = absoluteBottomTextY + maxTopOverflow;
                } else {
                    currentY += style.getStaffSpacing();
                }

                for (int m = 0; m < measureCount; m++) {
                    List<StaffLayout> staffs = measures.get(m).getStaffs();
                    if (k < staffs.size()) {
                        staffs.get(k).setY(currentY);
                    }
                }

                int prevStaffIdx = k - 1;
                double prevStaffY = firstMeasureStaffs.get(prevStaffIdx).getY();
                double clearance = (currentY - prevStaffY) - prevNominalHeight;

                for (int m = 0; m < measureCount; m++) {
                    List<StaffLayout> staffs = measures.get(m).getStaffs();
                    if (prevStaffIdx < staffs.size()) {
                        staffs.get(prevStaffIdx).setSpaceBelow(clearance);
                    }
                }
            }

            prevNominalHeight = nominalHeight;
            prevLyricBottomOverflow = maxLyricBottomOverflow;
        }


        System.out.println(
                "noteBody=" + noteBodyTime / 1e6
        );

        System.out.println(
                "topOverflow=" + topTime / 1e6
        );

        System.out.println(
                "lyricOverflow=" + lyricTime / 1e6
        );

        int lastStaffIdx = staffCount - 1;
        for (int m = 0; m < measureCount; m++) {
            List<StaffLayout> staffs = measures.get(m).getStaffs();
            if (lastStaffIdx < staffs.size()) {
                staffs.get(lastStaffIdx).setSpaceBelow(0.0);
            }
        }
    }

    public void applyPagesVerticalLayout(ScoreLayout scoreLayout, ScoreStyle style) {
        List<PageLayout> pages = scoreLayout.getPages();
        int pageCount = pages.size();
        for (int p = 0; p < pageCount; p++) {
            PageLayout page = pages.get(p);
            List<PageBlockLayout> blocks = page.getBlocks();
            int blockCount = blocks.size();
            if (blockCount == 0) continue;

            double currentY = page.getMarginTop();

            for (int i = 0; i < blockCount; i++) {
                PageBlockLayout block = blocks.get(i);
                if (block instanceof SystemLayout system) {
                    boolean nextIsSystem = (i + 1 < blockCount) && (blocks.get(i + 1) instanceof SystemLayout);
                    double bottomOverflow = system.getBottomOverflow();
                    double spacing = nextIsSystem ? style.getSystemSpacing() : 0.0;

                    system.setSpaceBelow(spacing);
                    currentY += system.getTopOverflow();
                    system.setY(currentY);

                    currentY += (system.getNominalHeight() + bottomOverflow + spacing);
                } else {
                    block.setY(currentY);
                    currentY += block.getHeight();
                }
            }
        }
    }
}