package net.quepierts.experiment.nf1210.client.editor.property;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WrappedNumberProperty implements Property<Number> {
    private final NumberConsumer setter;
    private final NumberSupplier getter;

    @Override
    public Number getObject() {
        return this.getter.get();
    }

    @Override
    public void setObject(Number value) {
        this.setter.accept(value);
    }

    @Override
    public double getNumber() {
        return this.getter.get().doubleValue();
    }

    @Override
    public void setNumber(double value) {
        this.setter.accept(value);
    }

    @FunctionalInterface
    public interface NumberSupplier {
        Number get();
    }

    @FunctionalInterface
    public interface NumberConsumer {
        void accept(Number value);
    }
}
