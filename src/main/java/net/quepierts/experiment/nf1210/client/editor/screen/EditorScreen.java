package net.quepierts.experiment.nf1210.client.editor.screen;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.quepierts.experiment.nf1210.client.Client;
import net.quepierts.experiment.nf1210.client.editor.EnumBoolean;
import net.quepierts.experiment.nf1210.client.editor.inspector.Inspectable;
import net.quepierts.experiment.nf1210.client.editor.inspector.InspectorBuilder;
import net.quepierts.experiment.nf1210.client.editor.widget.Inspector;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class EditorScreen extends Screen implements Inspectable {

    public static final EditorScreen INSTANCE = new EditorScreen();

    private final Inspector inspector;

    public EditorScreen() {
        super(Component.literal("shader preview"));

        Window window = Minecraft.getInstance().getWindow();
        int width = 120;
        int left = window.getGuiScaledWidth() - width;
        this.inspector = new Inspector(left, 0, width, window.getGuiScaledHeight());
        this.addRenderableWidget(this.inspector);

        this.inspector.setInspectObject(this);
    }

    @Override
    public void resize(@NotNull Minecraft pMinecraft, int pWidth, int pHeight) {
        this.width = pWidth;
        this.height = pHeight;

        Window window = Minecraft.getInstance().getWindow();
        int width = 120;
        int left = window.getGuiScaledWidth() - width;
        this.inspector.setRectangle(width, Minecraft.getInstance().getWindow().getGuiScaledHeight(), left, 0);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        for (Renderable renderable : this.renderables) {
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        //RenderHelper.fillArc(pGuiGraphics, pMouseX, pMouseY, 64, (pMouseX - 64) / (Mth.PI * 4), Mth.HALF_PI, 16f);
        /*RenderHelper.frameRoundRect(pGuiGraphics,
                20, 20,
                widthProperty.getInt(),
                heightProperty.getInt(),
                lineWidthProperty.getInt(),
                arcProperty.getInt()
        );*/
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null && this.getFocused().mouseClicked(mouseX, mouseY, button)) {
            if (button == 0) {
                this.setDragging(true);
            }
            return true;
        }

        Iterator<? extends GuiEventListener> iterator = this.children().iterator();

        GuiEventListener guieventlistener;
        do {
            if (!iterator.hasNext()) {
                return false;
            }

            guieventlistener = iterator.next();
        } while(!guieventlistener.mouseClicked(mouseX, mouseY, button));

        this.setFocused(guieventlistener);
        if (button == 0) {
            this.setDragging(true);
        }

        return true;
    }

    @Override
    public void onInspect(InspectorBuilder builder) {
        builder.title(Component.literal("Debug"))
                .enumBox(Component.literal("Enable"), Client.uEnableDebug, EnumBoolean.getValues())
                .title(Component.literal("Offset"))
                .floatSlider(Component.literal("X"), Client.uOffsetX, -32, 32, 1)
                .floatSlider(Component.literal("Y"), Client.uOffsetY, -32, 32, 1)
                .floatSlider(Component.literal("Z"), Client.uOffsetZ, -32, 32, 1)
                .space()
                .title(Component.literal("Chunk Amount"))
                .intSlider(Component.literal("X"), Client.uChunkAmountX, 16, 128, 1)
                .intSlider(Component.literal("Y"), Client.uChunkAmountY, 16, 128, 1)
                .intSlider(Component.literal("Z"), Client.uChunkAmountZ, 16, 128, 1);
    }
}
