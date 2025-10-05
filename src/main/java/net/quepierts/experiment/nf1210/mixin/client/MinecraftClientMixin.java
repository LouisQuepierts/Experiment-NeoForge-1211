package net.quepierts.experiment.nf1210.mixin.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.experiment.nf1210.client.render.Renderer;
import net.quepierts.experiment.nf1210.client.event.ResizeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow @Final private Window window;

    @Inject(
            method = "resizeDisplay",
            at = @At("RETURN")
    )
    private void experiment$resize(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ResizeEvent(this.window));
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/Tesselator;init()V",
                    shift = At.Shift.AFTER
            )
    )
    private void experiment$init(GameConfig gameConfig, CallbackInfo ci) {
        Renderer.init();
    }

}
