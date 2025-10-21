package net.quepierts.experiment.nf1210.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.editor.screen.EditorScreen;
import org.lwjgl.glfw.GLFW;

public class KeySystem {
    public static final Lazy<KeyMapping> KEY_DEBUG;

    @EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Register {
        @SubscribeEvent
        public static void onRegisterKey(final RegisterKeyMappingsEvent event) {
            event.register(KEY_DEBUG.get());
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class Handler {
        @SubscribeEvent
        public static void onKey(final InputEvent.Key event) {
            if (KEY_DEBUG.get().isDown()) {
                Minecraft.getInstance().setScreen(new EditorScreen());
            }
        }
    }

    static {
        KEY_DEBUG = Lazy.of(
                () -> new KeyMapping(
                        "key.experiment.debug",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_BRACKET,
                        "key.categories.experiment"
                )
        );
    }
}
