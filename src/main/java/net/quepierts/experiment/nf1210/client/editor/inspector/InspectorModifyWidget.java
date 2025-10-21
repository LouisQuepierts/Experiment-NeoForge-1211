package net.quepierts.experiment.nf1210.client.editor.inspector;

import net.minecraft.network.chat.Component;
import net.quepierts.experiment.nf1210.client.editor.property.Property;
import net.quepierts.experiment.nf1210.client.editor.property.WrappedProperty;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class InspectorModifyWidget<T> extends InspectorWidget {
    protected final Component message;
    protected final Property<T> property;

    protected InspectorModifyWidget(int height, Component message, Supplier<T> getter, Consumer<T> setter) {
        this(height, message, new WrappedProperty<>(setter, getter));
    }

    protected InspectorModifyWidget(int height, Component message, Property<T> property) {
        super(height);
        this.message = message;
        this.property = property;
    }
}
