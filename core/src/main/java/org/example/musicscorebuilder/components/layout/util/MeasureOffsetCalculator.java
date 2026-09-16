package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.List;


public class MeasureOffsetCalculator {

    /**
     * Zwraca wizualny początek taktu, tzn.:
     * - punkt lewej kreski taktowej w przypadku taktów innych niż pierwszy w systemie
     * - punkt rozpoczęcia nut w przypadku pierwszego taktu w systemie
     *
     * @param ml layout taktu, którego początek wizualny chcemy wyznaczyć
     * @return wartość x początku taktu
     */
    public static double calculateStartXOffset(MeasureLayout ml) {
        SystemLayout system = ml.getParent();
        List<MeasureLayout> systemMeasures = system.getMeasures();
        int indexInSystem = systemMeasures.indexOf(ml);
        boolean isSystemStart = (indexInSystem == 0);

        if (isSystemStart) {
            return MeasureOffsetCalculator.calculateSystemStartOffsetX(ml);
        }
        if (indexInSystem > 0) {
            return MeasureOffsetCalculator.calculateMidSystemStartOffsetX(systemMeasures.get(indexInSystem - 1));
        }
        return 0.0;
    }

    /**
     * Zwraca wizualny koniec taktu, tzn.:
     * - punkt prawej kreski taktowej
     *
     * @param ml layout taktu, którego koniec wizualny chcemy wyznaczyć
     * @return wartość x końca taktu
     */
    public static double calculateEndXOffset(MeasureLayout ml) {
        double endXOffset = ml.getWidth();
        List<SegmentLayout> segments = ml.getSegments();

        if (segments != null && !segments.isEmpty()) {
            SegmentLayout lastSeg = segments.get(segments.size() - 1);
            if (lastSeg.getType() == SegmentType.BARLINE) {
                endXOffset -= lastSeg.getWidth();
            }
        }

        return endXOffset;
    }

    private static double calculateSystemStartOffsetX(MeasureLayout ml) {
        double offset = 0.0;
        if (ml.getSegments() != null) {
            for (SegmentLayout seg : ml.getSegments()) {
                if (seg.getType() == SegmentType.NOTEREST) {
                    break;
                }
                offset += seg.getWidth();
            }
        }
        return offset;
    }

    private static double calculateMidSystemStartOffsetX(MeasureLayout prevMl) {
        List<SegmentLayout> prevSegments = prevMl.getSegments();
        if (prevSegments != null && !prevSegments.isEmpty()) {
            SegmentLayout lastSeg = prevSegments.get(prevSegments.size() - 1);
            return -lastSeg.getMarginRight();
        }
        return 0.0;
    }
}
