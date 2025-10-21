package net.quepierts.experiment.nf1210.client.cursor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.experiment.nf1210.mixin.accessor.client.NativeImageAccessor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import java.nio.ByteBuffer;

public class CursorInstance extends Pointer.Default implements AutoCloseable {
    public static CursorInstance create(CursorType type) {
        long address = GLFW.glfwCreateStandardCursor(type.getGlfwCode());
        return new CursorInstance(address);
    }

    public static CursorInstance create(ResourceLocation icon, int xhot, int yhot) {
        Minecraft.getInstance().getTextureManager().getTexture(icon).bind();

        int[] pWidth = new int[1];
        int[] pHeight = new int[1];

        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, pWidth);
        GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, pHeight);

        int width = pWidth[0];
        int height = pHeight[0];

        if (width != 0 && height != 0) {
            try (NativeImage image = new NativeImage(width, height, false)) {
                image.downloadTexture(0, false);

                int size = image.format().components() * width * height;
                ByteBuffer mem = MemoryUtil.memAlloc(size);
                MemoryUtil.memCopy(((NativeImageAccessor) (Object) image).getAddress(), MemoryUtil.memAddress(mem), size);

                long address = GLFW.glfwCreateCursor(
                        new GLFWImage(ByteBuffer.allocateDirect(GLFWImage.SIZEOF))
                                .width(width)
                                .height(height)
                                .pixels(mem),
                        xhot,
                        yhot
                );

                if (address == 0) {
                    GLFW.nglfwGetError(0);
                }

                return new CursorInstance(address);
            } catch (Exception ignored) {}
        }

        return null;
    }

    private boolean activated = true;

    private CursorInstance(long address) {
        super(address);

        if (address == 0) {
            this.activated = false;
        }
    }

    public void use() {
        if (!this.activated || this.address == 0) {
            return;
        }

        long window = Minecraft.getInstance().getWindow().getWindow();
        GLFW.glfwSetCursor(window, this.address);
    }

    public void use(long window) {
        if (!this.activated || this.address == 0) {
            return;
        }

        GLFW.glfwSetCursor(window, this.address);
    }

    @Override
    public void close() {
        if (!this.activated || this.address == 0) {
            return;
        }

        GLFW.glfwDestroyCursor(this.address);
        this.activated = false;
    }
}
