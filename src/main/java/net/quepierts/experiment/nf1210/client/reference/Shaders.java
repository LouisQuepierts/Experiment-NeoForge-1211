package net.quepierts.experiment.nf1210.client.reference;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.shader.RayMarchingInstance;
import net.quepierts.experiment.nf1210.client.shader.ShaderHolder;
import net.quepierts.experiment.nf1210.client.shader.ShaderList;

import java.io.IOException;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Shaders {
    public static final ShaderHolder<ShaderInstance> WHITE;

    public static final ShaderHolder<RayMarchingInstance> RAY_MARCHING;

    private static final ShaderList INSTANCES;

    @SubscribeEvent
    public static void onRegisterShader(final RegisterShadersEvent event) throws IOException {
        INSTANCES.onRegisterShader(event);
    }

    static {
        INSTANCES = new ShaderList(Experiment.MODID);

        WHITE = INSTANCES.register(
                "white",
                DefaultVertexFormat.POSITION_TEX
        );

        RAY_MARCHING = INSTANCES.register(
                "ray_marching",
                DefaultVertexFormat.POSITION_TEX,
                RayMarchingInstance::new
        );
    }
}
