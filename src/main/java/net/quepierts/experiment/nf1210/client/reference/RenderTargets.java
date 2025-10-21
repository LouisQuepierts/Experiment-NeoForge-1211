package net.quepierts.experiment.nf1210.client.reference;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import lombok.experimental.UtilityClass;
import net.quepierts.experiment.nf1210.AutoCloser;
import net.quepierts.experiment.nf1210.client.Client;

@UtilityClass
public class RenderTargets {

    public static final AutoCloser.Holder<RenderTarget> SUB_MAIN = AutoCloser.Holder.<RenderTarget>of(
            () -> new TextureTarget(1920, 1080, true, true),
            RenderTarget::destroyBuffers,
            Client.CLOSER
    );

    public static void tryInit() {
        SUB_MAIN.getValue();
    }
}
