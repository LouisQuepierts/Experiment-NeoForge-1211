package net.quepierts.experiment.nf1210.client.editor.inspector;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Getter
public abstract class InspectorWidget implements GuiEventListener {
    private final int height;

    @Setter
    private boolean focused = false;

    @Getter
    private boolean hovered = false;

    protected InspectorWidget(int height) {
        this.height = height;
    }

    public final void render(GuiGraphics graphics, int width, int mouseX, int mouseY, float partialTick, boolean hovered) {
        if (hovered != this.hovered) {
            if (hovered) {
                this.onMouseEntered(mouseX, mouseY);
            } else {
                this.onMouseLeaved(mouseX, mouseY);
            }
        }

        this.onRender(graphics, width, mouseX, mouseY, partialTick, hovered);

        this.hovered = hovered;
    }

    protected void onRender(GuiGraphics graphics, int width, int mouseX, int mouseY, float partialTick, boolean hovered) {

    }

    public void onMousePressed(double mouseX, double mouseY, int button, int width) {

    }

    public void onMouseReleased(double mouseX, double mouseY, int button, int width) {

    }

    public void onMouseDragging(double mouseX, double mouseY, int button, double deltaX, double deltaY, int width) {

    }

    public void onMouseEntered(int mouseX, int mouseY) {

    }

    public void onMouseLeaved(int mouseX, int mouseY) {

    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean onCharTyped(char codePoint, int modifiers) {
        return false;
    }

    public void onPaste(InspectorWidget copy, String clipboard) {
    }

    public void onPaste(String clipboard) {

    }
}
