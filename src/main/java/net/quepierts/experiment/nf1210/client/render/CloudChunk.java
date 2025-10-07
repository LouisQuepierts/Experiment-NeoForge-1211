package net.quepierts.experiment.nf1210.client.render;

import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

@Getter
public class CloudChunk implements AutoCloseable {

    public static final int CHUNK_SIZE = 16;
    public static final int VOXEL_SIZE = 1;

    public static final int BUFFER_SIZE = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * VOXEL_SIZE;

    private final ByteBuffer buffer;

    private boolean dirty;
    private int occupancy;

    public CloudChunk() {
        this.buffer = MemoryUtil.memAlloc(BUFFER_SIZE);
        this.dirty = true;
        this.occupancy = 0;
    }

    public void copy(CloudChunk other) {
        if (this == other) return;

        other.buffer.rewind();
        this.buffer.rewind();
        this.buffer.put(other.buffer);
        this.buffer.rewind();
        this.occupancy = other.occupancy;

        this.dirty = true;
    }

    public void put(byte[] bytes) {
        if (bytes.length != CloudChunk.BUFFER_SIZE) {
            throw new IllegalArgumentException("Invalid buffer size");
        }

        this.buffer.rewind();
        this.occupancy = 0;
        for (int i = 0; i < bytes.length; i++) {
            byte vox = bytes[i];
            this.buffer.put(vox);
            if (vox != 0) {
                this.occupancy++;
            }
        }
        this.buffer.rewind();
        this.dirty = true;
    }

    public void put(byte[][][] voxels) {
        if (voxels.length != CHUNK_SIZE
                || voxels[0].length != CHUNK_SIZE
                || voxels[0][0].length != CHUNK_SIZE) {
            throw new IllegalArgumentException("Invalid voxel array");
        }

        this.occupancy = 0;
        this.buffer.rewind();
        for (int i = 0; i < CHUNK_SIZE; i++) {
            for (int j = 0; j < CHUNK_SIZE; j++) {
                for (int k = 0; k < CHUNK_SIZE; k++) {
                    byte value = voxels[i][j][k];
                    this.buffer.put(value);

                    if (value != 0) {
                        this.occupancy++;
                    }
                }
            }
        }
        this.buffer.rewind();
        this.dirty = true;
    }

    public void put(int x, int y, int z, byte value) {
        int index = (x * CHUNK_SIZE + y) * CHUNK_SIZE + z;
        byte last = this.buffer.get(index);
        if (last == value) {
            return;
        }

        this.dirty = true;
        this.buffer.put(index, value);

        boolean last0 = last == 0;
        boolean value0 = value == 0;

        if (last0 != value0) {
            if (value0) {
                this.occupancy --;
            } else {
                this.occupancy ++;
            }
        }
    }

    public boolean isEmpty() {
        return this.occupancy == 0;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(this.buffer);
    }
}
