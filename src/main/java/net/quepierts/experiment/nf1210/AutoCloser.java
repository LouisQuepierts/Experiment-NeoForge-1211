package net.quepierts.experiment.nf1210;

import java.util.ArrayList;
import java.util.List;

public class AutoCloser implements AutoCloseable {
    private final List<AutoCloseable> closeables;

    public AutoCloser() {
        this.closeables = new ArrayList<>();
    }

    public void add(AutoCloseable closeable) {
        this.closeables.add(closeable);
    }

    @Override
    public void close() {
        for (AutoCloseable closeable : this.closeables) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }
}
