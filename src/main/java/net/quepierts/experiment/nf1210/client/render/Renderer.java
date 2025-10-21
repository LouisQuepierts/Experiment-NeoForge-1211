package net.quepierts.experiment.nf1210.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.Client;
import net.quepierts.experiment.nf1210.client.editor.EnumBoolean;
import net.quepierts.experiment.nf1210.client.event.ResizeEvent;
import net.quepierts.experiment.nf1210.client.reference.RenderTargets;
import net.quepierts.experiment.nf1210.client.reference.Shaders;
import net.quepierts.experiment.nf1210.client.reference.VertexBuffers;
import net.quepierts.experiment.nf1210.client.shader.RayMarchingInstance;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.util.Random;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Renderer {
    public static final ResourceLocation NOISE = Experiment.rl("textures/noise/hdr_rgba_0.png");

    private static final int FILTER_MODE = GL30.GL_NEAREST;

    private static RenderTarget tempTarget;
    private static RenderTarget targetM2;
    private static RenderTarget targetM4;
    private static RenderTarget targetM8;

    private static CloudChunkBuffer cloudBuffer;

    @SubscribeEvent
    public static void onRenderLevel(final RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();

        if (stage == RenderLevelStageEvent.Stage.AFTER_SKY) {
            if (Client.uEnableDebug.getObject() == EnumBoolean.TRUE) {
                Renderer.onAfterParticles(event);
            }
        }
    }

    @SubscribeEvent
    public static void onResize(final ResizeEvent event) {
        tempTarget.resize(event.getWidth(), event.getHeight(), true);
        targetM2.resize(event.getWidth() / 2, event.getHeight() / 2, true);
        targetM4.resize(event.getWidth() / 4, event.getHeight() / 4, true);
        targetM8.resize(event.getWidth() / 8, event.getHeight() / 8, true);
        targetM2.setFilterMode(FILTER_MODE);
        targetM4.setFilterMode(FILTER_MODE);
        targetM8.setFilterMode(FILTER_MODE);
    }

    public static void init() {
        Window window = Minecraft.getInstance().getWindow();
        tempTarget = new TextureTarget(window.getWidth(), window.getHeight(), false, true);
        targetM2 = new TextureTarget(window.getWidth() / 2, window.getHeight() / 2, true, true);
        targetM4 = new TextureTarget(window.getWidth() / 4, window.getHeight() / 4, true, true);
        targetM8 = new TextureTarget(window.getWidth() / 8, window.getHeight() / 8, true, true);

        targetM2.setFilterMode(FILTER_MODE);
        targetM4.setFilterMode(FILTER_MODE);
        targetM8.setFilterMode(FILTER_MODE);
        tempTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        targetM2.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        targetM4.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        targetM8.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        byte[] bytes = new byte[CloudChunk.BUFFER_SIZE];
        Random random = new Random();
        for (int i = 0; i < CloudChunk.BUFFER_SIZE; i++) {
            if (random.nextInt(4) == 0) {
                bytes[i] = (byte) (1 + random.nextInt(255));
            }
        }
        CloudChunk cloud = new CloudChunk();
        cloud.put(bytes);

        cloudBuffer = new CloudChunkBuffer();

        for (int x = 0; x < CloudChunkBuffer.CHUNK_AMOUNT; x++) {
            for (int y = 0; y < CloudChunkBuffer.CHUNK_AMOUNT; y++) {
                for (int z = 0; z < CloudChunkBuffer.CHUNK_AMOUNT; z++) {
                    if (random.nextInt(4) == 0) {
                        cloudBuffer.link(x, y, z, cloud);
                    }
                }
            }
        }

        cloudBuffer.uploadMapping();

        Client.CLOSER.add(() -> {
            tempTarget.destroyBuffers();
            targetM2.destroyBuffers();
            targetM4.destroyBuffers();
            targetM8.destroyBuffers();
        });
        Client.CLOSER.add(cloud);
        Client.CLOSER.add(cloudBuffer);

        VertexBuffers.tryInit();
        RenderTargets.tryInit();
    }

    private static void onAfterParticles(final RenderLevelStageEvent event) {
        final RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        final AbstractTexture noise = Minecraft.getInstance().getTextureManager().getTexture(NOISE);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        GlStateManager._depthMask(true);

        VertexBuffer.unbind();

        RayMarchingInstance raymarching = Shaders.RAY_MARCHING_VOX.getInstance();

        final Camera camera = event.getCamera();
        final Vec3 cameraPosition = camera.getPosition();
        final Vector3f position = cameraPosition.toVector3f();

        raymarching.uCameraPosition.set(position);

        final Matrix4f projectionMatrix = new Matrix4f(event.getProjectionMatrix());
        final Quaternionf quaternionf = camera.rotation().conjugate(new Quaternionf());
        final Matrix4f modelViewMatrix = new Matrix4f().rotation(quaternionf);

        modelViewMatrix.translate(
                -position.x,
                -position.y,
                -position.z
        );

        raymarching.setProjectionMatrix(projectionMatrix);
        raymarching.setInverseProjectionMatrix(projectionMatrix.invert());

        raymarching.setViewMatrix(modelViewMatrix);
        raymarching.setInverseViewMatrix(modelViewMatrix.invert());

        raymarching.setOffset(
                Client.uOffsetX.getFloat(),
                Client.uOffsetY.getFloat(),
                Client.uOffsetZ.getFloat()
        );

        raymarching.setChunkAmount(
                Client.uChunkAmountX.getInt(),
                Client.uChunkAmountY.getInt(),
                Client.uChunkAmountZ.getInt()
        );

//        raymarching.setSampler("uDepthSampler", target.getDepthTextureId());

        raymarching.apply();

        raymarching.setVoxelSampler(cloudBuffer.getVoxelTexture());
        raymarching.setOccupationSampler(cloudBuffer.getOccupationTexture());

        /*targetM8.clear(true);
        targetM8.bindWrite(true);
        draw();

        targetM4.clear(true);
        targetM4.bindWrite(true);
        draw();*/

        RenderTarget tmp = targetM4;

        tmp.clear(true);
        tmp.bindWrite(true);
        draw();

        cloudBuffer.getVoxelTexture().unbind(1);
        cloudBuffer.getOccupationTexture().unbind(2);
        raymarching.clear();

        target.bindWrite(true);

        RenderSystem.enableBlend();
        ShaderInstance combine = Shaders.COMBINE.getInstance();
        combine.setSampler("uDiffuseSampler", tmp.getColorTextureId());
        combine.setSampler("uDepthSampler", tmp.getDepthTextureId());
        combine.setSampler("uNoiseSampler", noise.getId());

        combine.safeGetUniform("uDiffuseResolution").set((float) tmp.width, (float) tmp.height);
        combine.safeGetUniform("uTargetResolution").set((float) target.width, (float) target.height);
        combine.apply();
        draw();
        combine.clear();
        RenderSystem.disableBlend();

        RenderSystem.disableDepthTest();
    }

    private static void draw() {
        /*BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(bufferbuilder.buildOrThrow());*/

        VertexBuffer buffer = VertexBuffers.QUAD.getValue();
        buffer.bind();
        buffer.draw();
        VertexBuffer.unbind();
    }
}
