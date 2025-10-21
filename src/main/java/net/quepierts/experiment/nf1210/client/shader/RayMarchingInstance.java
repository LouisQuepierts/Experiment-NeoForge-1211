package net.quepierts.experiment.nf1210.client.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.quepierts.experiment.nf1210.client.render.Texture3D;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.io.IOException;

public class RayMarchingInstance extends ShaderInstance {

    public final AbstractUniform uViewMatrix;
    public final AbstractUniform uProjectionMatrix;
    public final AbstractUniform uInverseViewMatrix;
    public final AbstractUniform uInverseProjectionMatrix;
    public final AbstractUniform uCameraPosition;
    public final AbstractUniform uOffset;
    public final AbstractUniform uChunkAmount;

    public final int locVoxelSampler;
    public final int locOccupationSampler;

    public RayMarchingInstance(
            @NotNull ResourceProvider provider,
            @NotNull ResourceLocation location,
            @NotNull VertexFormat format
    ) throws IOException {
        super(provider, location, format);

        uViewMatrix = this.safeGetUniform("uViewMatrix");
        uProjectionMatrix = this.safeGetUniform("uProjectionMatrix");
        uInverseViewMatrix = this.safeGetUniform("uInverseViewMatrix");
        uInverseProjectionMatrix = this.safeGetUniform("uInverseProjectionMatrix");
        uCameraPosition = this.safeGetUniform("uCameraPosition");
        uOffset = this.safeGetUniform("uOffset");
        uChunkAmount = this.safeGetUniform("uChunkAmount");

        int program = this.getId();
        locVoxelSampler = Uniform.glGetUniformLocation(program, "uVoxelSampler");
        locOccupationSampler = Uniform.glGetUniformLocation(program, "uOccupationSampler");
    }

    public void setViewMatrix(Matrix4f matrix) {
        uViewMatrix.set(matrix);
    }

    public void setProjectionMatrix(Matrix4f matrix) {
        uProjectionMatrix.set(matrix);
    }

    public void setInverseViewMatrix(Matrix4f matrix) {
        uInverseViewMatrix.set(matrix);
    }

    public void setInverseProjectionMatrix(Matrix4f matrix) {
        uInverseProjectionMatrix.set(matrix);
    }

    public void setCameraPosition(float x, float y, float z) {
        uCameraPosition.set(x, y, z);
    }

    public void setOffset(float x, float y, float z) {
        uOffset.set(x, y, z);
    }

    public void setChunkAmount(int x, int y, int z) {
        uChunkAmount.set(x, y, z);
    }

    public void setVoxelSampler(Texture3D texture) {
        texture.bind(1);
        Uniform.uploadInteger(locVoxelSampler, 1);
    }

    public void setOccupationSampler(Texture3D texture) {
        texture.bind(2);
        Uniform.uploadInteger(locOccupationSampler, 2);
    }
}
