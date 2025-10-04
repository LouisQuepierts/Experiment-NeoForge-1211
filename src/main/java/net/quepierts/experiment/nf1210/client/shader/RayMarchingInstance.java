package net.quepierts.experiment.nf1210.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RayMarchingInstance extends ShaderInstance {

    public final Uniform INVERSE_VIEW_MATRIX;
    public final Uniform INVERSE_PROJECTION_MATRIX;
    public final Uniform CAMERA_POSITION;

    public RayMarchingInstance(
            @NotNull ResourceProvider provider,
            @NotNull ResourceLocation location,
            @NotNull VertexFormat format
    ) throws IOException {
        super(provider, location, format);

        INVERSE_VIEW_MATRIX = this.getUniform("InverseViewMatrix");
        INVERSE_PROJECTION_MATRIX = this.getUniform("InverseProjectionMatrix");
        CAMERA_POSITION = this.getUniform("CameraPosition");
    }

}
