package net.quepierts.experiment.nf1210.client.editor.widget;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.quepierts.experiment.nf1210.client.editor.property.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class LayoutComponent implements Renderable, GuiEventListener {
    private final IntegerProperty x;
    private final IntegerProperty y;
    private final IntegerProperty width;
    private final IntegerProperty height;

    @Setter
    @Getter
    private boolean active;

    @Setter
    @Getter
    private boolean focused;

    @Getter
    private boolean hovered;

    @Getter
    private boolean pressed;

    @Getter
    private boolean wasHovered;

    protected LayoutComponent(int x, int y, int width, int height) {
        this.x = IntegerProperty.of(x);
        this.y = IntegerProperty.of(y);
        this.width = IntegerProperty.of(width);
        this.height = IntegerProperty.of(height);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.active) {
            return;
        }

        this.updateHoveredStatus(mouseX, mouseY);
        this.onRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        GuiEventListener.super.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return GuiEventListener.super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return GuiEventListener.super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return GuiEventListener.super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        return GuiEventListener.super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return GuiEventListener.super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return GuiEventListener.super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return GuiEventListener.super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.active
                && pMouseX > this.x.getInt() && pMouseX < this.x.getInt() + this.width.getInt()
                && pMouseY > this.y.getInt() && pMouseY < this.y.getInt() + this.height.getInt();
    }

    public void setX(int x) {
        this.x.setInt(x);
    }

    public void setY(int y) {
        this.y.setInt(y);
    }

    public int getX() {
        return this.x.getInt();
    }

    public int getY() {
        return this.y.getInt();
    }

    public void setPosition(int x, int y) {
        this.x.setInt(x);
        this.y.setInt(y);
    }

    public int getWidth() {
        return this.width.getInt();
    }

    public int getHeight() {
        return this.height.getInt();
    }

    public void setSize(int width, int height) {
        this.width.setInt(width);
        this.height.setInt(height);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.x.getInt(), this.y.getInt(), this.width.getInt(), this.height.getInt());
    }

    protected void updateHoveredStatus(int mouseX, int mouseY) {
        this.hovered = this.isMouseOver(mouseX, mouseY);
        if (this.hovered && !this.wasHovered) {
            this.onMouseEntered(mouseX, mouseY);
        } else if (!this.hovered && this.wasHovered) {
            this.onMouseLeaved(mouseX, mouseY);
        }
        this.wasHovered = this.hovered;
    }

    protected void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

    }

    protected void onMousePressed(double mouseX, double mouseY, int button) {
    }

    protected void onMouseReleased(double mouseX, double mouseY, int button) {

    }

    protected void onMouseDragging(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

    }

    protected void onMouseEntered(int mouseX, int mouseY) {

    }

    protected void onMouseLeaved(int mouseX, int mouseY) {

    }

    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    protected boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    protected boolean onCharTyped(char codePoint, int modifiers) {
        return false;
    }

    protected void onPaste(Object object, String pasteboard) {
    }
}
