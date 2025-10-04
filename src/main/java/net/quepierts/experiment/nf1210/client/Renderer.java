package net.quepierts.experiment.nf1210.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quepierts.experiment.Experiment;
import net.quepierts.experiment.client.reference.Shaders;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Renderer {

    @SubscribeEvent
    public static void onRenderLevel(final RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();

        if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Renderer.onAfterParticles(event);
        }
    }

    private static void onAfterParticles(final RenderLevelStageEvent event) {
        final RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        final int width = target.width;
        final int height = target.height;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);

        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, width, height);

        Shaders.WHITE.use();

        target.bindWrite(true);
        quad();

        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void quad() {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(0.0F, 0.0F, 500.0F).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(1.0F, 0.0F, 500.0F).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(1.0F, 1.0F, 500.0F).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(0.0F, 1.0F, 500.0F).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
