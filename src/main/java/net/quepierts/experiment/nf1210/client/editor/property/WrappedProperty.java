package net.quepierts.experiment.nf1210.client.editor.property;

import lombok.AllArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Supplier;

@AllArgsConstructor
public class WrappedProperty<T> implements Property<T> {
    private final Consumer<T> setter;
    private final Supplier<T> getter;

    @Override
    public T getObject() {
        return this.getter.get();
    }

    @Override
    public void setObject(T value) {
        this.setter.accept(value);
    }
}
