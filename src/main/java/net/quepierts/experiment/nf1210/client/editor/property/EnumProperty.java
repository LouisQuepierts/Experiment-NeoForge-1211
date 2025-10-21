package net.quepierts.experiment.nf1210.client.editor.property;

import lombok.AllArgsConstructor;
import net.quepierts.experiment.nf1210.client.editor.DisplayableType;

@AllArgsConstructor
public class EnumProperty<T extends DisplayableType> implements Property<T> {
    private T value;

    public static <T extends DisplayableType> EnumProperty<T> of(T value) {
        return new EnumProperty<>(value);
    }

    @Override
    public T getObject() {
        return this.value;
    }

    @Override
    public void setObject(T value) {
        this.value = value;
    }
}
