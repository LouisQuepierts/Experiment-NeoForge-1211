package net.quepierts.experiment.nf1210.client.editor.property;

public interface Property<T> {
    T getObject();

    void setObject(T value);

    default double getNumber() {
        return 0.0;
    }

    default void setNumber(double value) {

    }
}
