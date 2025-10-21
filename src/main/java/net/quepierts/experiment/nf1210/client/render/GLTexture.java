package net.quepierts.experiment.nf1210.client.render;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

public interface GLTexture {
    int getTextureId();

    int getGlType();

    int getGlPixelFormat();

    int getGlPixelType();

    default void bind() {
        GL32.glBindTexture(this.getGlType(), this.getTextureId());
    }

    default void unbind() {
        GL30.glBindTexture(this.getGlType(), 0);
    }
}
