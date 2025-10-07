package net.quepierts.experiment.nf1210.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.Client;
import net.quepierts.experiment.nf1210.client.event.ResizeEvent;
import net.quepierts.experiment.nf1210.client.reference.Shaders;
import net.quepierts.experiment.nf1210.client.shader.RayMarchingInstance;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL32;

import java.util.Random;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Renderer {
    public static final ResourceLocation NOISE = Experiment.rl("textures/noise/output_128x128_tri.png");

    private static RenderTarget tempTarget;
    private static RenderTarget halfTarget;

    private static VertexBuffer quadBuffer;

    private static CloudChunkTexture cloudTexture;

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
        halfTarget.resize(event.getWidth() / 2, event.getHeight() / 2, true);
    }

    public static void init() {
        Window window = Minecraft.getInstance().getWindow();
        tempTarget = new TextureTarget(window.getWidth(), window.getHeight(), false, true);
        halfTarget = new TextureTarget(window.getWidth() / 2, window.getHeight() / 2, false, true);
        halfTarget.setFilterMode(GL32.GL_LINEAR);
        tempTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        halfTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        quadBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        quadBuffer.bind();
        quadBuffer.upload(bufferbuilder.buildOrThrow());
        VertexBuffer.unbind();

        byte[] bytes = new byte[CloudChunk.BUFFER_SIZE];
        Random random = new Random();
        for (int i = 0; i < CloudChunk.BUFFER_SIZE; i++) {
            if (random.nextInt(4) == 3) {
                bytes[i] = (byte) 255;
            }
        }
        CloudChunk cloud = new CloudChunk();
        cloud.put(bytes);

        cloudTexture = new CloudChunkTexture(cloud);

        Client.CLOSER.add(() -> {
            tempTarget.destroyBuffers();
            halfTarget.destroyBuffers();
        });
        Client.CLOSER.add(quadBuffer);
        Client.CLOSER.add(cloud);
        Client.CLOSER.add(cloudTexture);
    }

    private static void onAfterParticles(final RenderLevelStageEvent event) {
        final RenderTarget target = Minecraft.getInstance().getMainRenderTarget();

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();

        VertexBuffer.unbind();

        RayMarchingInstance raymarching = Shaders.RAY_MARCHING_VOX.getInstance();

        final Camera camera = event.getCamera();
        final Vec3 cameraPosition = camera.getPosition();
        final Vector3f position = cameraPosition.toVector3f();

        raymarching.CAMERA_POSITION.set(position);

        final Matrix4f projectionMatrix = new Matrix4f(event.getProjectionMatrix());
        final Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        final Matrix4f modelViewMatrix = new Matrix4f().rotation(quaternionf);

        modelViewMatrix.translate(
                -position.x,
                -position.y,
                -position.z
        );

        raymarching.INVERSE_PROJECTION_MATRIX.set(projectionMatrix.invert());
        raymarching.INVERSE_VIEW_MATRIX.set(modelViewMatrix.invert());

        raymarching.setSampler("uDepthSampler", target.getDepthTextureId());

        halfTarget.clear(true);
        halfTarget.bindWrite(true);
        raymarching.apply();

        cloudTexture.bind(1);
        Uniform.uploadInteger(raymarching.LOC_VOXEL_SAMPLER, 1);
        draw();

        cloudTexture.unbind(1);
        raymarching.clear();

        target.bindWrite(true);

        RenderSystem.enableBlend();
        ShaderInstance combine = Shaders.COMBINE.getInstance();
        combine.setSampler("uDiffuseSampler", halfTarget.getColorTextureId());
        combine.apply();
        draw();
        combine.clear();
        RenderSystem.disableBlend();
    }

    private static void draw() {
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
