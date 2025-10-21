package net.quepierts.experiment.nf1210.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import lombok.Getter;
import net.quepierts.experiment.nf1210.Closeable;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

@Getter
public class Texture3D implements Closeable, GLTexture {

    private final int textureId;

    private final int glInternalFormat;
    private final int glPixelFormat;
    private final int glPixelType;

    private final int width;
    private final int height;
    private final int depth;

    public Texture3D(
            int width,
            int height,
            int depth,
            int glInternalFormat,
            int glPixelFormat,
            int glPixelType
    ) {
        this.textureId = GL32.glGenTextures();
        this.glInternalFormat = glInternalFormat;
        this.glPixelFormat = glPixelFormat;
        this.glPixelType = glPixelType;
        this.width = width;
        this.height = height;
        this.depth = depth;
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
        GL32.glTexImage3D(
                GL32.GL_TEXTURE_3D,
                0,
                glInternalFormat,
                width,
                height,
                depth,
                0,
                glPixelFormat,
                glPixelType,
                0
        );
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
    }

    public Texture3D(
            int width,
            int height,
            int depth,
            int glInternalFormat,
            int glPixelFormat,
            int glPixelType,
            int glTextureFilter,
            int glWrapMode
    ) {
        this.textureId = GL32.glGenTextures();
        this.glInternalFormat = glInternalFormat;
        this.glPixelFormat = glPixelFormat;
        this.glPixelType = glPixelType;
        this.width = width;
        this.height = height;
        this.depth = depth;
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
        GL32.glTexImage3D(
                GL32.GL_TEXTURE_3D,
                0,
                glInternalFormat,
                width,
                height,
                depth,
                0,
                glPixelFormat,
                glPixelType,
                0
        );
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_MAG_FILTER, glTextureFilter);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_MIN_FILTER, glTextureFilter);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_S, glWrapMode);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_T, glWrapMode);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_R, glWrapMode);
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
    }

    public void setFilter(final int glTextureFilter) {
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_MAG_FILTER, glTextureFilter);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_MIN_FILTER, glTextureFilter);
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
    }

    public void setWrap(final int glWrapMode) {
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_S, glWrapMode);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_T, glWrapMode);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_R, glWrapMode);
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
    }

    public void upload(@Nullable ByteBuffer buffer) {
        this.uploadImage(0, 0, 0, this.width, this.height, this.depth, buffer);
    }

    public void uploadImage(
            int x, int y, int z,
            int w, int h, int d,
            @Nullable ByteBuffer buffer
    ) {
        if (buffer == null) {
            this.clearImage(x, y, z, w, h, d);
        } else {
            GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
            GL32.glTexSubImage3D(
                    GL32.GL_TEXTURE_3D,
                    0,
                    x, y, z,
                    w, h, d,
                    glPixelFormat,
                    glPixelType,
                    buffer
            );
            GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
        }
    }

    public void clearImage(
            int x, int y, int z,
            int w, int h, int d
    ) {
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
        GL32.glTexSubImage3D(
                GL32.GL_TEXTURE_3D,
                0,
                x, y, z,
                w, h, d,
                glPixelFormat,
                glPixelType,
                0
        );
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
    }

    public void bind(final int textureUnit) {
        GlStateManager._activeTexture(GL32.GL_TEXTURE0 + textureUnit);
        GlStateManager._bindTexture(0);
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
    }

    public void unbind(final int textureUnit) {
        GlStateManager._activeTexture(GL32.GL_TEXTURE0 + textureUnit);
        GlStateManager._bindTexture(0);
        GL32.glBindTexture(GL32.GL_TEXTURE_3D, 0);
    }

    @Override
    public void close() {
        GL32.glDeleteTextures(this.textureId);
    }

    @Override
    public int getGlType() {
        return GL30.GL_TEXTURE_3D;
    }
}
