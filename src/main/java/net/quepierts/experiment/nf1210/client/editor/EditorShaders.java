package net.quepierts.experiment.nf1210.client.editor;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.shader.ShaderHolder;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.MOD)
public class EditorShaders {
    public static final ShaderHolder<ShaderInstance> COLOR_FIELD = ShaderHolder.of(
            Experiment.rl("editor/color_field"),
            DefaultVertexFormat.POSITION_TEX
    );

    @SubscribeEvent
    public static void onRegisterShader(final RegisterShadersEvent event) throws IOException {
        COLOR_FIELD.register(event);
    }
}
