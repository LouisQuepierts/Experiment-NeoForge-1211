package net.quepierts.experiment.nf1210.client.reference;

import com.mojang.blaze3d.vertex.*;
import net.quepierts.experiment.nf1210.AutoCloser;
import net.quepierts.experiment.nf1210.client.Client;

public class VertexBuffers {

    public static final AutoCloser.Holder<VertexBuffer> QUAD = AutoCloser.Holder.of(
            () -> {
                BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
                bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
                bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
                bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
                VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                buffer.bind();
                buffer.upload(bufferbuilder.buildOrThrow());
                VertexBuffer.unbind();
                return buffer;
            },
            Client.CLOSER
    );

    public static final AutoCloser.Holder<VertexBuffer> IDENTITY_QUAD = AutoCloser.Holder.<VertexBuffer>of(
            () -> {
                BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 0.0F);
                bufferbuilder.addVertex(1.0F, 0.0F, 0.0F).setUv(1.0F, 0.0F);
                bufferbuilder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
                bufferbuilder.addVertex(0.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
                VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                buffer.bind();
                buffer.upload(bufferbuilder.buildOrThrow());
                VertexBuffer.unbind();
                return buffer;
            },
            Client.CLOSER
    );

    public static void tryInit() {
        QUAD.getValue();
        IDENTITY_QUAD.getValue();
    }
}
