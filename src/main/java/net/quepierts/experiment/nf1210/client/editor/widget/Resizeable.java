package net.quepierts.experiment.nf1210.client.editor.widget;

import net.quepierts.experiment.nf1210.client.cursor.CursorManager;

public interface Resizeable {
    HandleArea getHoveredArea();

    HandleArea getLastHoveredArea();

    void setHoveredArea(HandleArea area);

    void setLastHoveredArea(HandleArea area);

    boolean canDrag(HandleArea area);

    default void updateCursor(double mouseX, double mouseY) {
        HandleArea clicked = this.getHoveredArea();
        if (this.getLastHoveredArea() != clicked) {

            if (!this.canDrag(clicked)) {
                CursorManager.INSTANCE.reset();
                return;
            }

            CursorManager.INSTANCE.use(clicked.getCursor());
        }
    }
}
