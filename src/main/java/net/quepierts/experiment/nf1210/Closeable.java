package net.quepierts.experiment.nf1210;

public interface Closeable extends CloseFunction<Closeable> {
    void close();

    default void close(Closeable value) {
        this.close();
    }
}
