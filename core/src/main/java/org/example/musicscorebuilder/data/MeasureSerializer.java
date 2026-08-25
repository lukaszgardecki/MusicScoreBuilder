package org.example.musicscorebuilder.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.example.musicscorebuilder.components.music.Measure;

import java.io.IOException;

public class MeasureSerializer extends JsonSerializer<Measure> {

    @Override
    public void serialize(Measure m, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        Measure prev = m.getPrev();

        if (prev == null || isTimeSigChanged(prev, m)) {
            gen.writeObjectField("timeSig", m.getTimeSignature());
        }

        if (prev == null || isKeySigChanged(prev, m)) {
            gen.writeObjectField("keySig", m.getKeySignature());
        }

        if (prev == null && m.getStaves() != null && !m.getStaves().isEmpty()) {
            gen.writeObjectField("staves", m.getStaves());
        }

        if (m.getRightBarline() != null && m.getBarlineStyle() != null
                && m.getBarlineStyle() != org.example.musicscorebuilder.components.music.BarlineStyle.SINGLE) {
            gen.writeObjectField("barline", m.getRightBarline());
        }

        if (m.hasSystemBreak()) {
            gen.writeBooleanField("systemBreak", true);
        }

        if (m.getSegments() != null && !m.getSegments().isEmpty()) {
            gen.writeObjectField("segs", m.getSegments());
        }

        gen.writeEndObject();
    }

    private boolean isTimeSigChanged(Measure prev, Measure current) {
        if (current.getTimeSignature() == null) return false;
        if (prev.getTimeSignature() == null) return true;
        return prev.getTimeSignature().getBeat() != current.getTimeSignature().getBeat()
                || prev.getTimeSignature().getBeatType() != current.getTimeSignature().getBeatType()
                || prev.getTimeSignature().getType() != current.getTimeSignature().getType();
    }

    private boolean isKeySigChanged(Measure prev, Measure current) {
        if (current.getKeySignature() == null) return false;
        if (prev.getKeySignature() == null) return true;
        return prev.getKeySignature().getFifths() != current.getKeySignature().getFifths();
    }
}