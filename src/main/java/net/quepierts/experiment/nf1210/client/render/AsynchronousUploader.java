package net.quepierts.experiment.nf1210.client.render;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import lombok.Getter;
import net.quepierts.experiment.nf1210.Closeable;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.Comparator;

public class AsynchronousUploader implements Closeable {

    private final Int2ObjectMap<Task> map;
    private final ObjectHeapPriorityQueue<Task> queue;
    private final Pool pool;

    private final int[] pboIds;
    private final int pboAmount;
    private final int pboSize;

    private int frameIndex;

    public AsynchronousUploader(int amount, int size) {
        this.pboIds = new int[amount];
        this.pboAmount = amount;
        this.pboSize = size;

        for (int i = 0; i < amount; i++) {
            this.pboIds[i] = GL30.glGenBuffers();
            GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, this.pboIds[i]);
            GL30.glBufferData(GL30.GL_PIXEL_UNPACK_BUFFER, size, GL30.GL_STREAM_DRAW);
        }

        GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);

        this.map = new Int2ObjectOpenHashMap<>();
        this.queue = new ObjectHeapPriorityQueue<>(Comparator.comparingInt(Task::getPriority));
        this.pool = new Pool(32);
    }

    public void schedule(
            Texture3D texture,
            ByteBuffer data,
            int id,
            int priority,
            int x, int y, int z,
            int w, int h, int d
    ) {
        if (data.remaining() > this.pboSize) {
            throw new IllegalArgumentException("Data size is too large");
        }

        Task task = this.map.get(id);
        if (task == null) {
            task = this.pool.obtain();
            task.set(texture, data, priority, x, y, z, w, h, d);
            this.map.put(id, task);
            this.queue.enqueue(task);
        } else {
            task.set(texture, data, priority, x, y, z, w, h, d);
            this.queue.changed();
        }
    }

    public void flush() {
        int i = 0;
        while (!this.queue.isEmpty()) {
            if (i >= this.pboAmount) {
                break;
            }

            int pboIndex = this.frameIndex;
            this.frameIndex = (this.frameIndex + 1) % this.pboAmount;
            Task task = this.queue.dequeue();
            GL30.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, this.pboIds[pboIndex]);

            ByteBuffer mapped = GL30.glMapBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, GL30.GL_WRITE_ONLY, task.data.remaining(), null);
            if (mapped == null) {
                throw new RuntimeException("glMapBuffer failed");
            }

            mapped.put(task.data);
            mapped.flip();
            GL30.glUnmapBuffer(GL30.GL_PIXEL_UNPACK_BUFFER);

            GLTexture texture = task.texture;
            texture.bind();

            if (texture.getGlType() == GL30.GL_TEXTURE_2D) {
                GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, task.x, task.y, task.w, task.h, texture.getGlPixelFormat(), texture.getGlPixelType(), 0);
            } else if (texture.getGlType() == GL30.GL_TEXTURE_3D) {
                GL30.glTexSubImage3D(GL30.GL_TEXTURE_3D, 0, task.x, task.y, task.z, task.w, task.h, task.d, texture.getGlPixelFormat(), texture.getGlPixelType(), 0);
            }

            texture.unbind();

            pool.release(task);
        }
    }

    @Override
    public void close() {
        for (int pbo : this.pboIds) {
            if (pbo != 0) {
                GL30.glDeleteBuffers(pbo);
            }
        }
    }

    final static class Task implements Comparator<Task> {
        GLTexture texture;
        ByteBuffer data;

        @Getter
        int priority;
        int x, y, z;
        int w, h, d;

        boolean dirty;

        void set(
                GLTexture texture,
                ByteBuffer data,
                int priority,
                int x, int y, int z,
                int w, int h, int d
        ) {
            this.texture = texture;
            this.data = data;

            this.priority = priority;

            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.d = d;

            this.dirty = true;
        }

        void reset() {
            this.texture = null;
            this.data = null;

            this.dirty = false;
        }

        @Override
        public int compare(Task me, Task ot) {
            return me.priority - ot.priority;
        }
    }

    final static class Pool {
        private final Stack<Task> pool;

        public Pool(int initialCapacity) {
            this.pool = new ObjectArrayList<>(initialCapacity);
            for (int i = 0; i < initialCapacity; i++) {
                this.pool.push(new Task());
            }
        }

        Task obtain() {
            return this.pool.isEmpty() ? new Task() : this.pool.pop();
        }

        void release(Task task) {
            task.reset();
            this.pool.push(task);
        }
    }
}
