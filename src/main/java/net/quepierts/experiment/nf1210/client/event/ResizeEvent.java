package net.quepierts.experiment.nf1210.client.event;

import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import net.neoforged.bus.api.Event;

@Getter
public final class ResizeEvent extends Event {

    private final int width;
    private final int height;

    public ResizeEvent(final Window window) {
        this.width = window.getWidth();
        this.height = window.getHeight();
    }

}
