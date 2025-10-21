package net.quepierts.experiment.nf1210.client.editor.property;

public interface Bindable<T> extends Property<T> {
    void bind(Property<T> other);

    void unbind();
}
