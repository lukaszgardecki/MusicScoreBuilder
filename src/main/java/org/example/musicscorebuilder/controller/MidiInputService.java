package org.example.musicscorebuilder.controller;

import javafx.application.Platform;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.music.util.MeasureNoteInserter;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

public class MidiInputService {
    private static MidiInputService instance;
    private final List<Transmitter> openTransmitters = new ArrayList<>();
    private final List<MidiDevice> openDevices = new ArrayList<>();

    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();

    private MidiInputService() {}

    public static synchronized MidiInputService getInstance() {
        if (instance == null) {
            instance = new MidiInputService();
        }
        return instance;
    }

    public void startListening() {
        stopListening();
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            tryConnectDevice(info);
        }
    }

    public void stopListening() {
        openTransmitters.forEach(Transmitter::close);
        openTransmitters.clear();

        openDevices.stream()
                .filter(MidiDevice::isOpen)
                .forEach(MidiDevice::close);
        openDevices.clear();
    }

    private void tryConnectDevice(MidiDevice.Info info) {
        try {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            if (device.getMaxTransmitters() != 0) {
                Transmitter transmitter = device.getTransmitter();
                transmitter.setReceiver(new MidiInputReceiver());
                device.open();

                openTransmitters.add(transmitter);
                openDevices.add(device);
            }
        } catch (MidiUnavailableException ignored) {}
    }

    private class MidiInputReceiver implements Receiver {
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage sm) {
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                    int midiNote = sm.getData1();
                    Platform.runLater(() -> handleMidiNotePressed(midiNote));
                }
            }
        }

        @Override
        public void close() {}
    }

    private void handleMidiNotePressed(int midiNote) {
        if (!modeManager.isInsertMode()) return;

        InsertionContext context = InsertionContext.from(scoreNavigator.getLastCursor());
        if (context == null) return;

        Note note = createNoteFromMidi(midiNote, context.measure());
        if (!isPitchWithinLedgerBounds(note.getPitch(), context.clef(), context.maxLedgerLines())) {
            return;
        }

        Segment nextSegment = MeasureNoteInserter.insertNote(
                context.measure(),
                context.segment(),
                context.staffId(),
                note
        );

        if (nextSegment != null) {
            scheduleCursorUpdate(nextSegment, context.staffId());
        }

        stateManager.notifyScoreChanged();
    }

    private Note createNoteFromMidi(int midiNote, Measure parentMeasure) {
        int octave = (midiNote / 12) - 1;
        int noteInOctave = midiNote % 12;
        ParsedPitch pitch = parseMidiPitch(noteInOctave);

        return new Note(
                modeManager.getCurrentVoice(),
                pitch.step(),
                pitch.alter(),
                octave,
                modeManager.getCurrentNoteType(),
                BeamType.NONE,
                modeManager.isDotted() ? 1 : 0,
                parentMeasure
        );
    }

    private boolean isPitchWithinLedgerBounds(Pitch pitch, Clef clef, int maxLedgerLines) {
        if (clef == null || pitch == null) return true;

        ClefType clefType = clef.getType();
        int stepDifference = pitch.getAbsoluteDiatonicStep() - clefType.getDiatonicStep();
        double relativeYInSpaces = clefType.getOffsetY() - (stepDifference * 0.5);
        double minAllowedRelativeY = -maxLedgerLines;
        double maxAllowedRelativeY = 4.0 + maxLedgerLines;

        return relativeYInSpaces >= minAllowedRelativeY && relativeYInSpaces <= maxAllowedRelativeY;
    }

    private void scheduleCursorUpdate(Segment nextSegment, int staffId) {
        int voice = modeManager.getCurrentVoice();
        stateManager.setPostRefreshAction(newScoreLayout -> {
            Selectable newSelectable = findTargetSelectable(newScoreLayout, nextSegment, staffId, voice);
            if (newSelectable != null) {
                scoreNavigator.setCursorLayoutQuietly(new CursorLayout(newSelectable));
            }
        });
    }

    private Selectable findTargetSelectable(ScoreLayout layout, Segment segment, int staffId, int voice) {
        NoteRestElement targetElement = findTargetElement(segment, staffId, voice);
        if (targetElement == null) return null;

        return LayoutHitTester.findSelectableForElement(
                layout.getPages(),
                segment,
                staffId,
                targetElement
        );
    }

    private NoteRestElement findTargetElement(Segment segment, int staffId, int voice) {
        List<Element> elements = segment.getElementsByStaff(staffId);
        if (elements == null || elements.isEmpty()) return null;

        for (Element el : elements) {
            if (el instanceof NoteRestElement nre && nre.getVoice() == voice) {
                return nre;
            }
        }

        for (Element el : elements) {
            if (el instanceof NoteRestElement nre) {
                return nre;
            }
        }

        return null;
    }

    private record InsertionContext(
            Segment segment,
            int staffId,
            Measure measure,
            Clef clef,
            int maxLedgerLines
    ) {
        static InsertionContext from(CursorLayout cursor) {
            if (cursor == null || cursor.getSegment() == null || cursor.getStaff() == null) {
                return null;
            }

            Segment segment = cursor.getSegment().getSegment();
            Staff staff = cursor.getStaff().getStaff();
            if (segment == null || staff == null) return null;

            Measure measure = segment.getParent();
            if (measure == null) return null;

            Clef clef = staff.getDefaultClef();
            int maxLedgers = cursor.getSegment().getScoreStyle().getNoteMaxLedgerLines();

            return new InsertionContext(segment, staff.getIndex(), measure, clef, maxLedgers);
        }
    }

    private record ParsedPitch(PitchStep step, int alter) {}

    private ParsedPitch parseMidiPitch(int noteInOctave) {
        return switch (noteInOctave) {
            case 0  -> new ParsedPitch(PitchStep.C, 0);
            case 1  -> new ParsedPitch(PitchStep.C, 1);
            case 2  -> new ParsedPitch(PitchStep.D, 0);
            case 3  -> new ParsedPitch(PitchStep.E, -1);
            case 4  -> new ParsedPitch(PitchStep.E, 0);
            case 5  -> new ParsedPitch(PitchStep.F, 0);
            case 6  -> new ParsedPitch(PitchStep.F, 1);
            case 7  -> new ParsedPitch(PitchStep.G, 0);
            case 8  -> new ParsedPitch(PitchStep.A, -1);
            case 9  -> new ParsedPitch(PitchStep.A, 0);
            case 10 -> new ParsedPitch(PitchStep.B, -1);
            case 11 -> new ParsedPitch(PitchStep.B, 0);
            default -> new ParsedPitch(PitchStep.C, 0);
        };
    }
}