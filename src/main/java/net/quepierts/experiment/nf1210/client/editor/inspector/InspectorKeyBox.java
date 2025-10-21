package net.quepierts.experiment.nf1210.client.editor.inspector;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.quepierts.experiment.nf1210.client.editor.property.Property;

public class InspectorKeyBox extends InspectorModifyWidget<InputConstants.Key> {
    private boolean active = false;
    private InputConstants.Key key;

    public InspectorKeyBox(Component message, Property<InputConstants.Key> property) {
        super(22, message, property);
        this.key = property.getObject();
    }

    @Override
    protected void onRender(GuiGraphics graphics, int width, int mouseX, int mouseY, float partialTick, boolean hovered) {
        int buttonWidth = Math.min(width / 2, 100);
        int left = width - buttonWidth;

        graphics.drawWordWrap(Minecraft.getInstance().font, this.message, 0, 8, left, 0xffffffff);
        RenderSystem.enableBlend();

        int hover = hovered ? mouseY / 20 : -1;
        graphics.fill(left, 2, width, 22, 0xbb000000);
        if (this.active && this.isFocused()) {
            graphics.renderOutline(left, 2, buttonWidth, 20, 0xffbbbbff);
        } else if (hover == 0) {
            graphics.renderOutline(left, 2, buttonWidth, 20, 0xffffffff);
        }

        int half = buttonWidth / 2;
        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                this.key.getDisplayName(),
                left + half, 8,
                0xffffffff
        );
    }

    @Override
    public void onMousePressed(double mouseX, double mouseY, int button, int width) {
        if (button != 0) {
            return;
        }

        this.active = !this.active;
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active) {
            return false;
        }

        this.key = InputConstants.getKey(keyCode, scanCode);
        this.property.setObject(this.key);
        this.active = false;
        return true;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.active = false;
        }
    }

    @Override
    public void onPaste(InspectorWidget copy, String clipboard) {
        if (copy instanceof InspectorKeyBox box) {
            this.key = box.key;
            this.property.setObject(this.key);
        }
    }
}
