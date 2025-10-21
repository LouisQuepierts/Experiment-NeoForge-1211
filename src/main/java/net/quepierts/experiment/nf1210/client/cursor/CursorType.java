package net.quepierts.experiment.nf1210.client.cursor;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.experiment.nf1210.Experiment;
import org.lwjgl.glfw.GLFW;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CursorType {
    ARROW(GLFW.GLFW_ARROW_CURSOR, Experiment.rl("arrow")),
    IBEAM(GLFW.GLFW_IBEAM_CURSOR, Experiment.rl("ibeam")),
    CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR, Experiment.rl("crosshair")),
    POINTING(GLFW.GLFW_POINTING_HAND_CURSOR, Experiment.rl("pointing")),
    RESIZE_EW(GLFW.GLFW_RESIZE_EW_CURSOR, Experiment.rl("resize_ew")),
    RESIZE_NS(GLFW.GLFW_RESIZE_NS_CURSOR, Experiment.rl("resize_ns")),
    RESIZE_NWSE(GLFW.GLFW_RESIZE_NWSE_CURSOR, Experiment.rl("resize_nwse")),
    RESIZE_NESW(GLFW.GLFW_RESIZE_NESW_CURSOR, Experiment.rl("resize_nesw")),
    RESIZE_ALL(GLFW.GLFW_RESIZE_ALL_CURSOR, Experiment.rl("resize_all")),
    NOT_ALLOWED(GLFW.GLFW_NOT_ALLOWED_CURSOR, Experiment.rl("not_allowed"));

    private final int glfwCode;
    private final ResourceLocation resourceLocation;

    public static final ImmutableList<CursorType> VALUES;

    static {
        VALUES = ImmutableList.copyOf(CursorType.values());
    }
}
