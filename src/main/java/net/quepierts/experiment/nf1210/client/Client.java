package net.quepierts.experiment.nf1210.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.quepierts.experiment.nf1210.AutoCloser;
import net.quepierts.experiment.nf1210.Experiment;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Client {
    public static final AutoCloser CLOSER = new AutoCloser();

    @SubscribeEvent
    public static void onShutdown(final GameShuttingDownEvent event) {
        CLOSER.close();
    }
}
