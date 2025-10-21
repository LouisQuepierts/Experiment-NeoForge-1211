package net.quepierts.experiment.nf1210.client.cursor;

import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CursorManager implements AutoCloseable {
    public static final CursorManager INSTANCE = new CursorManager();

    private final EnumMap<CursorType, CursorInstance> standard;
    private final Map<ResourceLocation, CursorInstance> resource;

    private final Map<ResourceLocation, CursorInstance> all;

    private CursorInstance def;
    private CursorInstance using;

    public CursorManager() {
        this.standard = new EnumMap<>(CursorType.class);
        this.resource = new HashMap<>();
        this.all = new HashMap<>();
    }

    public void init() {
        if (!this.standard.isEmpty()) {
            return;
        }

        for (CursorType type : CursorType.VALUES) {
            CursorInstance instance = CursorInstance.create(type);
            this.standard.put(type, instance);
            this.all.put(type.getResourceLocation(), instance);
        }

        this.def = this.standard.get(CursorType.ARROW);
        this.using = this.def;
    }

    public boolean register(ResourceLocation location, int xhot, int yhot) {
        if (this.resource.containsKey(location)) {
            return false;
        }

        CursorInstance instance = CursorInstance.create(location, xhot, yhot);
        this.resource.put(location, instance);
        this.all.put(location, instance);
        return instance != null;
    }

    public void reset() {
        if (this.using != this.def) {
            this.using = this.def;
            this.using.use();
        }
    }

    public void use(CursorType type) {
        CursorInstance instance = this.standard.get(type);
        if (this.using != instance) {
            this.using = instance;
            this.using.use();
        }
    }

    public void use(ResourceLocation location) {
        CursorInstance instance = this.resource.get(location);
        if (this.using != instance) {
            this.using = instance;
            this.using.use();
        }
    }

    @Override
    public void close() {
        for (CursorInstance instance : this.all.values()) {
           instance.close();
        }
    }

    public CursorInstance getDefault() {
        return this.def;
    }
}
