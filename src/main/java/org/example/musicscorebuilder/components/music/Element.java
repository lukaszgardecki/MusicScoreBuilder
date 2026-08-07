package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "elType"
)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property = "@id"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Barline.class, name = "barline"),
        @JsonSubTypes.Type(value = Clef.class, name = "clef"),
        @JsonSubTypes.Type(value = TimeSignature.class, name = "timeSignature"),
        @JsonSubTypes.Type(value = KeySignature.class, name = "keySignature"),
        @JsonSubTypes.Type(value = Note.class, name = "note"),
        @JsonSubTypes.Type(value = Rest.class, name = "rest")
})
public abstract class Element {

    @JsonIgnore
    protected Measure parent;

    protected Element() {}

    public Element(Measure parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public Measure getParent() { return parent; }

    public void setParent(Measure parent) { this.parent = parent; }
    public void hasChanged() {
        if (parent != null) parent.setDirty(true);
    }
}
