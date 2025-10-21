package net.quepierts.experiment.nf1210.client.editor.window;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WindowRegion {
    HEADER(false),
    INNER(false),
    EDGE_T(true),
    EDGE_B(true),
    EDGE_L(true),
    EDGE_R(true),
    EDGE_TL(true),
    EDGE_TR(true),
    EDGE_BL(true),
    EDGE_BR(true);

    private final boolean edge;
}
