package net.quepierts.experiment.nf1210;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AutoCloser implements AutoCloseable {
    private final List<Closeable> closeables;

    public AutoCloser() {
        this.closeables = new ArrayList<>();
    }

    public void add(Closeable closeable) {
        this.closeables.add(closeable);
    }

    @Override
    public void close() {
        for (Closeable closeable : this.closeables) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }

    public static final class Holder<T> implements Closeable {

        private T value;
        private CloseFunction<T> func;
        private final Supplier<T> supplier;

        Holder(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        Holder(Supplier<T> supplier, CloseFunction<T> close) {
            this.supplier = supplier;
            this.func = close;
        }

        public static <T> Holder<T> of(
                @NotNull Supplier<T> supplier,
                @NotNull AutoCloser closer
        ) {
            final Holder<T> holder = new Holder<>(supplier);
            closer.add(holder);
            return holder;
        }

        public static <T> Holder<T> of(
                @NotNull Supplier<T> supplier,
                @NotNull CloseFunction<T> delegate,
                @NotNull AutoCloser closer
        ) {
            final Holder<T> holder = new Holder<>(supplier, delegate);
            closer.add(holder);
            return holder;
        }

        public static <T> Holder<T> of(
                @NotNull T value,
                @NotNull AutoCloser closer
        ) {
            final Holder<T> holder = new Holder<>(null);
            holder.setValue(value);
            closer.add(holder);
            return holder;
        }

        public static <T> Holder<T> of(
                @NotNull T value,
                @NotNull CloseFunction<T> delegate,
                @NotNull AutoCloser closer
        ) {
            final Holder<T> holder = new Holder<>(null, delegate);
            holder.setValue(value);
            closer.add(holder);
            return holder;
        }

        public T getValue() {
            if (this.value == null) {
                this.setValue(this.supplier.get());
            }

            return this.value;
        }

        public T value() {
            return this.value;
        }

        @Override
        public void close() {
            if (this.value != null && this.func != null) {
                this.func.close(this.value);
            }
        }

        private void setValue(T value) {
            this.value = value;
            if (value instanceof Closeable) {
                this.func = (CloseFunction<T>) value;
            } else if (value instanceof AutoCloseable) {
                this.func = (v) -> {
                    try {
                        ((AutoCloseable) v).close();
                    } catch (Exception ignored) { }
                };
            } else if (this.func == null) {
                throw new IllegalArgumentException("Value is not closeable");
            }
        }
    }
}
