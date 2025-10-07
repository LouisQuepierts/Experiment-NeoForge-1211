package net.quepierts.experiment.nf1210.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import lombok.Getter;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class CloudChunkTexture implements AutoCloseable {

    private final CloudChunk chunk;
    @Getter private final int textureId;

    public CloudChunkTexture(final CloudChunk chunk) {
        int texID = GL32.glGenTextures();
        this.textureId = texID;
        this.chunk = chunk;

        final ByteBuffer buffer = chunk.getBuffer();
        buffer.rewind();

        GL32.glBindTexture(GL32.GL_TEXTURE_3D, texID);
        GL32.glTexImage3D(
                GL32.GL_TEXTURE_3D,
                0,
                GL32.GL_R8,
                CloudChunk.CHUNK_SIZE,
                CloudChunk.CHUNK_SIZE,
                CloudChunk.CHUNK_SIZE,
                0,
                GL32.GL_RED,
                GL32.GL_UNSIGNED_BYTE,
                buffer
        );

        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_S, GL32.GL_CLAMP_TO_EDGE);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_T, GL32.GL_CLAMP_TO_EDGE);
        GL32.glTexParameteri(GL32.GL_TEXTURE_3D, GL32.GL_TEXTURE_WRAP_R, GL32.GL_CLAMP_TO_EDGE);
    }

    public void update() {
        if (this.chunk.isDirty()) {
            final ByteBuffer buffer = this.chunk.getBuffer();
            buffer.rewind();

            GL32.glBindTexture(GL32.GL_TEXTURE_3D, this.textureId);
            GL32.glTexSubImage3D(
                    GL32.GL_TEXTURE_3D,
                    0,
                    0,
                    0,
                    0,
                    CloudChunk.CHUNK_SIZE,
                    CloudChunk.CHUNK_SIZE,
                    CloudChunk.CHUNK_SIZE,
                    GL32.GL_RED,
                    GL32.GL_UNSIGNED_BYTE,
                    buffer
            );

            this.chunk.clearDirty();
        }
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
    public void close() throws Exception {
        GL32.glDeleteTextures(this.textureId);
    }
}
