package net.quepierts.experiment.nf1210.client.editor.property;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public interface FloatProperty extends Property<Float> {
    static FloatProperty of(FloatSupplier getter, FloatConsumer setter) {
        return new Wrapped(getter, setter);
    }

    static FloatProperty of(float value) {
        return new Value(value);
    }

    static FloatProperty of() {
        return new Value(0);
    }
    
    @Override
    default Float getObject() {
        return this.getFloat();
    }

    @Override
    default void setObject(Float value) {
        this.setFloat(value);
    }

    @Override
    default double getNumber() {
        return this.getFloat();
    }

    @Override
    default void setNumber(double value) {
        this.setFloat((int) value);
    }

    float getFloat();
    
    void setFloat(float value);
    
    @NoArgsConstructor
    @AllArgsConstructor
    class Value implements FloatProperty {
        private float value;

        @Override
        public float getFloat() {
            return this.value;
        }

        @Override
        public void setFloat(float value) {
            this.value = value;
        }
    }
    
    @AllArgsConstructor
    class Wrapped implements FloatProperty {
        private final FloatSupplier getter;
        private final FloatConsumer setter;

        @Override
        public float getFloat() {
            return this.getter.get();
        }

        @Override
        public void setFloat(float value) {
            this.setter.accept(value);
        }
    }
    
    @FunctionalInterface
    interface FloatConsumer {
        void accept(float value);
    }
    
    @FunctionalInterface
    interface FloatSupplier {
        float get();
    }
}
