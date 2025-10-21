package net.quepierts.experiment.nf1210.client.editor.widget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class BufferedWidget extends AbstractWidget {

    private final RenderTarget frameBuffer;
    private boolean shouldRepaint = true;

    public BufferedWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);

        this.frameBuffer = new TextureTarget(width, height, false, true);
    }

    @Override
    protected void renderWidget(
            @NotNull GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (this.shouldRepaint) {

            this.shouldRepaint = false;
        }

        this.frameBuffer.bindRead();

    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.resizeBuffer(width, height);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.resizeBuffer(width, this.frameBuffer.height);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        this.resizeBuffer(this.frameBuffer.width, height);
    }

    public void requestRepaint() {
        this.shouldRepaint = true;
    }

    protected void resizeBuffer(int width, int height) {
        int maxWidth = Math.min(width, this.frameBuffer.width);
        int maxHeight = Math.min(height, this.frameBuffer.height);

        this.frameBuffer.resize(maxWidth, maxHeight, true);
    }
}
