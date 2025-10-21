package net.quepierts.experiment.nf1210.client.editor.inspector;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.quepierts.experiment.nf1210.client.editor.DisplayableType;
import net.quepierts.experiment.nf1210.client.editor.property.FloatProperty;
import net.quepierts.experiment.nf1210.client.editor.property.IntegerProperty;
import net.quepierts.experiment.nf1210.client.editor.property.Property;

import java.util.List;

@SuppressWarnings("unused")
public class InspectorBuilder {
    private final ImmutableList.Builder<InspectorWidget> builder = ImmutableList.builder();

    public InspectorBuilder space() {
        this.builder.add(new InspectorSpace(10));
        return this;
    }

    public InspectorBuilder space(int height) {
        this.builder.add(new InspectorSpace(height));
        return this;
    }

    public InspectorBuilder title(Component message) {
        this.builder.add(new InspectorTitle(message, 24));
        return this;
    }

    public <T extends DisplayableType> InspectorBuilder enumBox(Component message, Property<T> property, T[] values) {
        this.builder.add(new InspectorEnumBox<>(message, property, values));
        return this;
    }

    public InspectorBuilder keyInputBox(Component message, Property<InputConstants.Key> property) {
        this.builder.add(new InspectorKeyBox(message, property));
        return this;
    }

    public InspectorBuilder intSlider(Component message, IntegerProperty property, int min, int max, int step) {
        this.builder.add(new InspectorIntegerSlider(message, property, min, max, step));
        return this;
    }

    public InspectorBuilder floatSlider(Component message, FloatProperty property, float min, float max, float step) {
        this.builder.add(new InspectorFloatSlider(message, property, min, max, step));
        return this;
    }

    public InspectorBuilder editBox(Component message, Property<String> property) {
        this.builder.add(new InspectorEditBox(message, property));
        return this;
    }

    public InspectorBuilder colorPicker(Component message, IntegerProperty property) {
        this.builder.add(new InspectorColorPicker(message, property));
        return this;
    }

    public List<InspectorWidget> build() {
        return this.builder.build();
    }
}
