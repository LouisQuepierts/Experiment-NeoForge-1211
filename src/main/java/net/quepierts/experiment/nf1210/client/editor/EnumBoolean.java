package net.quepierts.experiment.nf1210.client.editor;

import net.minecraft.network.chat.Component;

public enum EnumBoolean implements DisplayableType {
    FALSE(Component.literal("false")),
    TRUE(Component.literal("true"));

    static final EnumBoolean[] VALUES = values();
    static final Component TYPE_NAME = Component.literal("boolean");
    final Component name;

    EnumBoolean(Component name) {
        this.name = name;
    }

    @Override
    public Component getTypeDisplayName() {
        return TYPE_NAME;
    }

    @Override
    public Component getDisplayName() {
        return this.name;
    }

    public static EnumBoolean[] getValues() {
        return VALUES;
    }
}
