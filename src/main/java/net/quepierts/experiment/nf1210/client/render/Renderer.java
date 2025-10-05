package net.quepierts.experiment.nf1210.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.event.ResizeEvent;
import net.quepierts.experiment.nf1210.client.reference.Shaders;
import net.quepierts.experiment.nf1210.client.shader.RayMarchingInstance;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Renderer {

    private static RenderTarget tempTarget;
    private static VertexBuffer quadBuffer;

    @SubscribeEvent
    public static void onRenderLevel(final RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();

        if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Renderer.onAfterParticles(event);
        }
    }

    @SubscribeEvent
    public static void onResize(final ResizeEvent event) {
        tempTarget.resize(event.getWidth(), event.getHeight(), true);
    }

    @SubscribeEvent
    public static void onShutdown(final GameShuttingDownEvent event) {
        tempTarget.destroyBuffers();
        quadBuffer.close();
    }

    public static void init() {
        Window window = Minecraft.getInstance().getWindow();
        tempTarget = new TextureTarget(window.getWidth(), window.getHeight(), true, true);

        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        quadBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        quadBuffer.bind();
        quadBuffer.upload(bufferbuilder.buildOrThrow());
        VertexBuffer.unbind();
    }

    private static void onAfterParticles(final RenderLevelStageEvent event) {
        final RenderTarget target = Minecraft.getInstance().getMainRenderTarget();

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();

        VertexBuffer.unbind();

        RayMarchingInstance instance = Shaders.RAY_MARCHING.getInstance();

        final Camera camera = event.getCamera();
        final Vec3 cameraPosition = camera.getPosition();
        final Vector3f position = cameraPosition.toVector3f();

        instance.CAMERA_POSITION.set(position);


        final Matrix4f projectionMatrix = new Matrix4f(event.getProjectionMatrix());
        final Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        final Matrix4f modelViewMatrix = new Matrix4f().rotation(quaternionf);

        modelViewMatrix.translate(
                -position.x,
                -position.y,
                -position.z
        );

        instance.INVERSE_PROJECTION_MATRIX.set(projectionMatrix.invert());
        instance.INVERSE_VIEW_MATRIX.set(modelViewMatrix.invert());

        instance.setSampler("ScreenSampler", target.getColorTextureId());
        instance.setSampler("DepthSampler", target.getDepthTextureId());

        tempTarget.bindWrite(true);
        instance.apply();
        quad();
        instance.clear();

        target.bindWrite(false);
        tempTarget.blitToScreen(target.width, target.height);

    }

    private static void quad() {
        /*BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(bufferbuilder.buildOrThrow());*/

        quadBuffer.bind();
        quadBuffer.draw();
        VertexBuffer.unbind();
    }
}
