package net.quepierts.experiment.nf1210.client.editor.inspector;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.quepierts.experiment.nf1210.client.cursor.CursorManager;
import net.quepierts.experiment.nf1210.client.cursor.CursorType;
import net.quepierts.experiment.nf1210.client.editor.property.FloatProperty;

public class InspectorFloatSlider extends InspectorModifyWidget<Float> {
    private final float min;
    private final float max;
    private final float step;

    private float value;

    private boolean hover = false;

    public InspectorFloatSlider(Component message, FloatProperty property, float min, float max, float step) {
        super(36, message, property);

        this.min = min;
        this.max = max;

        this.value = property.getFloat();
        this.step = step;
    }

    @Override
    protected void onRender(GuiGraphics graphics, int width, int mouseX, int mouseY, float partialTick, boolean hovered) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, this.message, 0, 4, 0xffffffff);

        String text = Float.toString(this.value);
        int textWidth = font.width(text);
        graphics.drawString(font, text, width - textWidth, 4, 0xffffffff);
        RenderSystem.enableBlend();

        int length = width - 10;
        float interpolate = (float) (this.value - this.min) / (this.max - this.min);
        int offset = 4 + (int) (length * interpolate);

        this.hover = hovered && mouseY > 14;
        graphics.fill(0, 14, width, 34, 0xbb000000);
        graphics.fill(4, 23, width - 4, 25, 0x88ffffff);
        graphics.fill(offset, 17, offset + 2, 31, 0xffffffff);

        if (this.hover) {
            graphics.renderOutline(0, 14, width, 20, 0xffffffff);
        }
    }

    @Override
    public void onMousePressed(double mouseX, double mouseY, int button, int width) {
        if (button != 0 || mouseY <= 14) {
            return;
        }

        if (mouseX < 4 || mouseX > width - 4) {
            return;
        }

        CursorManager.INSTANCE.use(CursorType.POINTING);
        float interpolate = Math.clamp(((float) mouseX - 4) / (width - 10), 0.0f, 1.0f);
        float target = (this.max - this.min) * interpolate + this.min;
        float value = Math.round(target / this.step) * this.step;

        if (value != this.value) {
            this.value = value;
            this.property.setNumber(value);
        }
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, int button, int width) {
        CursorManager.INSTANCE.use(CursorType.ARROW);
    }

    @Override
    public void onMouseDragging(double mouseX, double mouseY, int button, double deltaX, double deltaY, int width) {
        if (button != 0) {
            return;
        }

        float interpolate = Math.clamp(((float) mouseX - 4) / (width - 10), 0.0f, 1.0f);
        float target = (this.max - this.min) * interpolate + this.min;
        float value = Math.round(target / this.step) * this.step;

        if (value != this.value) {
            this.value = value;
            this.property.setNumber(value);
        }
    }

    @Override
    public void onPaste(InspectorWidget copy, String clipboard) {
        if (copy instanceof InspectorFloatSlider slider) {
            this.value = slider.value;
            this.property.setNumber(this.value);
        }
    }
}
