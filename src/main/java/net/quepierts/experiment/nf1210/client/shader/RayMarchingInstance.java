package net.quepierts.experiment.nf1210.client.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RayMarchingInstance extends ShaderInstance {

    public final AbstractUniform INVERSE_VIEW_MATRIX;
    public final AbstractUniform INVERSE_PROJECTION_MATRIX;
    public final AbstractUniform CAMERA_POSITION;

    public final int LOC_VOXEL_SAMPLER;

    public RayMarchingInstance(
            @NotNull ResourceProvider provider,
            @NotNull ResourceLocation location,
            @NotNull VertexFormat format
    ) throws IOException {
        super(provider, location, format);

        INVERSE_VIEW_MATRIX = this.safeGetUniform("uInverseViewMatrix");
        INVERSE_PROJECTION_MATRIX = this.safeGetUniform("uInverseProjectionMatrix");
        CAMERA_POSITION = this.safeGetUniform("uCameraPosition");

        LOC_VOXEL_SAMPLER = Uniform.glGetUniformLocation(this.getId(), "uVoxelSampler");
    }

}
