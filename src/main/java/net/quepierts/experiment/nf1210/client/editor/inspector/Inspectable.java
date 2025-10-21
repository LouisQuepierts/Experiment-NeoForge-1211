package net.quepierts.experiment.nf1210.client.editor.inspector;

public interface Inspectable {
    void onInspect(InspectorBuilder builder);

    default boolean isAvailable() {
        return true;
    }
}
