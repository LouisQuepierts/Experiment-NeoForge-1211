package net.quepierts.experiment.nf1210;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Experiment.MODID)
public class Experiment {
    public static final String MODID = "experiment";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Experiment(IEventBus modEventBus, ModContainer modContainer) {

    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
