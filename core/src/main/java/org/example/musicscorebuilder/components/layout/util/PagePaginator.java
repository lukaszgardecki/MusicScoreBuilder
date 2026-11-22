//package org.example.musicscorebuilder.components.layout.util;
//
//import org.example.musicscorebuilder.components.frames.FrameLayout;
//import org.example.musicscorebuilder.components.layout.*;
//import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
//import org.example.musicscorebuilder.components.music.BraceType;
//import org.example.musicscorebuilder.components.music.frames.Frame;
//
//public class PagePaginator {
//    private final SystemJustifier systemJustifier;
//    private final FrameBuilder frameBuilder;
//    private final VerticalLayoutCalculator verticalCalculator;
//
//    public PagePaginator(SystemJustifier systemJustifier, FrameBuilder frameBuilder, VerticalLayoutCalculator verticalCalculator) {
//        this.systemJustifier = systemJustifier;
//        this.frameBuilder = frameBuilder;
//        this.verticalCalculator = verticalCalculator;
//    }
//
//    public boolean canFitMeasureInSystem(PageLayout page, SystemLayout system, MeasureLayout measureLayout, double courtesyPadding) {
//        double requiredSpace = measureLayout.getWidth() + courtesyPadding;
//        double availableSpace = page.getEffectiveWidth() - system.getWidth();
//        return availableSpace >= requiredSpace;
//    }
//
//    public SystemLayout finalizeSystemAndCreateNext(SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType, MeasureLayout nextMeasureLayout, ScoreStyle style) {
//        verticalCalculator.applyStaffVerticalLayout(currentSystem, style);
//        systemJustifier.justify(currentSystem);
//
//        PageLayout currentPage = currentSystem.getPageLayout();
//        boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < currentSystem.getHeight() + style.getSystemSpacing();
//
//        if (noSpaceForNextSystem) {
//            currentPage = createPageLayout(scoreLayout);
//            scoreLayout.addPageLayout(currentPage);
//        }
//
//        SystemLayout newSystem = addNewSystemToPage(currentPage, systemBraceType, style);
//        nextMeasureLayout.setX(newSystem.getWidth());
//        nextMeasureLayout.setParent(newSystem);
//
//        return newSystem;
//    }
//
//    public SystemLayout addNewSystemToPage(PageLayout pageLayout, BraceType systemBraceType, ScoreStyle style) {
//        var newSystem = new SystemLayout(pageLayout, systemBraceType);
//        newSystem.setSpaceBelow(style.getSystemSpacing());
//        pageLayout.addBlock(newSystem);
//        return newSystem;
//    }
//
//    public PageLayout createPageLayout(ScoreLayout scoreLayout) {
//        return new PageLayout(scoreLayout, scoreLayout.getPages().size());
//    }
//
//    public SystemLayout insertFrameBlock(Frame frameData, SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType, ScoreStyle style) {
//        PageLayout currentPage = currentSystem.getPageLayout();
//
//        if (!currentSystem.getMeasures().isEmpty()) {
//            systemJustifier.justify(currentSystem);
//        } else {
//            currentPage.getBlocks().remove(currentSystem);
//        }
//
//        FrameLayout frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);
//
//        if (currentPage.getRemainingHeight() < frameLayout.getHeight()) {
//            currentPage = createPageLayout(scoreLayout);
//            scoreLayout.addPageLayout(currentPage);
//            frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);
//        }
//
//        currentPage.addBlock(frameLayout);
//        return addNewSystemToPage(currentPage, systemBraceType, style);
//    }
//}

package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.frames.FrameLayout;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.BraceType;
import org.example.musicscorebuilder.components.music.frames.Frame;

public class PagePaginator {
    private final SystemJustifier systemJustifier;
    private final FrameBuilder frameBuilder;
    private final VerticalLayoutCalculator verticalCalculator;

    public PagePaginator(SystemJustifier systemJustifier, FrameBuilder frameBuilder, VerticalLayoutCalculator verticalCalculator) {
        this.systemJustifier = systemJustifier;
        this.frameBuilder = frameBuilder;
        this.verticalCalculator = verticalCalculator;
    }

    public boolean canFitMeasureInSystem(PageLayout page, SystemLayout system, MeasureLayout measureLayout, double courtesyPadding) {
        double requiredSpace = measureLayout.getWidth() + courtesyPadding;
        double availableSpace = page.getEffectiveWidth() - system.getWidth();
        return availableSpace >= requiredSpace;
    }

    public SystemLayout finalizeSystemAndCreateNext(SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType, MeasureLayout nextMeasureLayout, ScoreStyle style) {

//        long t = System.nanoTime();

        verticalCalculator.applyStaffVerticalLayout(currentSystem, style);

//        System.out.println(
//                "applyStaffVerticalLayout="
//                        + ((System.nanoTime() - t) / 1e6)
//        );

//        t = System.nanoTime();

        systemJustifier.justify(currentSystem);

//        System.out.println(
//                "justify="
//                        + ((System.nanoTime() - t) / 1e6)
//        );

        PageLayout currentPage = currentSystem.getPageLayout();
        boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < currentSystem.getHeight() + style.getSystemSpacing();

        if (noSpaceForNextSystem) {
            currentPage = createPageLayout(scoreLayout);
            scoreLayout.addPageLayout(currentPage);
        }

        SystemLayout newSystem = addNewSystemToPage(currentPage, systemBraceType, style);
        nextMeasureLayout.setX(newSystem.getWidth());
        nextMeasureLayout.setParent(newSystem);

        return newSystem;
    }

    public SystemLayout addNewSystemToPage(PageLayout pageLayout, BraceType systemBraceType, ScoreStyle style) {
        var newSystem = new SystemLayout(pageLayout, systemBraceType);
        newSystem.setSpaceBelow(style.getSystemSpacing());
        pageLayout.addBlock(newSystem);
        return newSystem;
    }

    public PageLayout createPageLayout(ScoreLayout scoreLayout) {
        return new PageLayout(scoreLayout, scoreLayout.getPages().size());
    }

    public SystemLayout insertFrameBlock(Frame frameData, SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType, ScoreStyle style) {
        PageLayout currentPage = currentSystem.getPageLayout();

        if (!currentSystem.getMeasures().isEmpty()) {
            systemJustifier.justify(currentSystem);
        } else {
            currentPage.getBlocks().remove(currentSystem);
        }

        FrameLayout frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);

        if (currentPage.getRemainingHeight() < frameLayout.getHeight()) {
            currentPage = createPageLayout(scoreLayout);
            scoreLayout.addPageLayout(currentPage);
            frameLayout.setParent(currentPage);
        }

        currentPage.addBlock(frameLayout);
        return addNewSystemToPage(currentPage, systemBraceType, style);
    }
}