package net.quepierts.experiment.nf1210.client.editor.widget;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.quepierts.experiment.nf1210.client.cursor.CursorType;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum HandleArea {
    INNER(false, CursorType.ARROW),
    EDGE_N(true, CursorType.RESIZE_NS),
    EDGE_S(true, CursorType.RESIZE_NS),
    EDGE_W(true, CursorType.RESIZE_EW),
    EDGE_E(true, CursorType.RESIZE_EW),
    EDGE_NW(true, CursorType.RESIZE_NWSE),
    EDGE_NE(true, CursorType.RESIZE_NESW),
    EDGE_SW(true, CursorType.RESIZE_NESW),
    EDGE_SE(true, CursorType.RESIZE_NWSE);

    private final boolean edge;
    private final CursorType cursor;
}
