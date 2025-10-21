package net.quepierts.experiment.nf1210.client;

import com.mojang.blaze3d.vertex.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.quepierts.experiment.nf1210.AutoCloser;
import net.quepierts.experiment.nf1210.Experiment;
import net.quepierts.experiment.nf1210.client.editor.EnumBoolean;
import net.quepierts.experiment.nf1210.client.editor.property.EnumProperty;
import net.quepierts.experiment.nf1210.client.editor.property.FloatProperty;
import net.quepierts.experiment.nf1210.client.editor.property.IntegerProperty;

@EventBusSubscriber(value = Dist.CLIENT, modid = Experiment.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Client {
    public static final AutoCloser CLOSER = new AutoCloser();

    public static final EnumProperty<EnumBoolean> uEnableDebug = EnumProperty.of(EnumBoolean.TRUE);

    public static final FloatProperty uOffsetX = FloatProperty.of(0);
    public static final FloatProperty uOffsetY = FloatProperty.of(0);
    public static final FloatProperty uOffsetZ = FloatProperty.of(0);

    public static final IntegerProperty uChunkAmountX = IntegerProperty.of(16);
    public static final IntegerProperty uChunkAmountY = IntegerProperty.of(16);
    public static final IntegerProperty uChunkAmountZ = IntegerProperty.of(16);

    @SubscribeEvent
    public static void onShutdown(final GameShuttingDownEvent event) {
        CLOSER.close();
    }
}
