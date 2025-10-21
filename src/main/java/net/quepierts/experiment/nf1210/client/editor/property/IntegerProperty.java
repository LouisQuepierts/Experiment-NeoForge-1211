package net.quepierts.experiment.nf1210.client.editor.property;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IntegerProperty extends Property<Integer> {
    static IntegerProperty of(IntegerSupplier getter, IntegerConsumer setter) {
        return new Wrapped(getter, setter);
    }

    static IntegerProperty of(int value) {
        return new Value(value);
    }

    static IntegerProperty of() {
        return new Value(0);
    }

    @Override
    default Integer getObject() {
        return this.getInt();
    }

    @Override
    default void setObject(Integer value) {
        this.setInt(value);
    }

    @Override
    default double getNumber() {
        return this.getInt();
    }

    @Override
    default void setNumber(double value) {
        this.setInt((int) value);
    }

    int getInt();

    void setInt(int value);

    @NoArgsConstructor
    @AllArgsConstructor
    class Value implements IntegerProperty {
        private int value;

        @Override
        public int getInt() {
            return this.value;
        }

        @Override
        public void setInt(int value) {
            this.value = value;
        }
    }

    @AllArgsConstructor
    class Wrapped implements IntegerProperty {
        @NotNull
        private final IntegerSupplier getter;

        @NotNull
        private final IntegerConsumer setter;

        @Override
        public int getInt() {
            return this.getter.get();
        }

        @Override
        public void setInt(int value) {
            this.setter.accept(value);
        }
    }

    class Bound extends Value implements Bindable<Integer> {
        @Nullable
        private Property<Integer> boundary;

        @Override
        public int getInt() {
            if (this.boundary != null) {
                return (int) this.boundary.getNumber();
            } else {
                return super.getInt();
            }
        }

        @Override
        public void bind(Property<Integer> other) {
            this.boundary = other;
        }

        @Override
        public void unbind() {
            this.boundary = null;
        }
    }

    @FunctionalInterface
    interface IntegerConsumer {
        void accept(int value);
    }

    @FunctionalInterface
    interface IntegerSupplier {
        int get();
    }
}
