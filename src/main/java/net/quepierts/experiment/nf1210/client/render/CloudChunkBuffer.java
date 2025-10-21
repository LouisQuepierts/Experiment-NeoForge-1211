package net.quepierts.experiment.nf1210.client.render;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.quepierts.experiment.nf1210.Closeable;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

public class CloudChunkBuffer implements Closeable {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int CHUNK_AMOUNT = 16;
    public static final int TOTAL_CHUNK_AMOUNT = CHUNK_AMOUNT * CHUNK_AMOUNT * CHUNK_AMOUNT;
    public static final int BUFFER_SIZE = CloudChunk.CHUNK_SIZE * CHUNK_AMOUNT;

    @Getter private final Texture3D voxelTexture;
    @Getter private final Texture3D occupationTexture;

    private final ByteBuffer mappingBuffer;

    private final Int2ObjectMap<CloudChunk> pos2chunk;
    private final Object2IntMap<CloudChunk> chunk2pos;

    private final LinkedHashMap<CloudChunk, Integer> lru;
    private final IntArrayList freeList;

    public CloudChunkBuffer() {
        this.voxelTexture = new Texture3D(
                BUFFER_SIZE,
                BUFFER_SIZE,
                BUFFER_SIZE,
                GL32.GL_R8UI,
                GL32.GL_RED_INTEGER,
                GL32.GL_UNSIGNED_BYTE,
                GL32.GL_NEAREST,
                GL32.GL_REPEAT
        );

        this.occupationTexture = new Texture3D(
                CHUNK_AMOUNT,
                CHUNK_AMOUNT,
                CHUNK_AMOUNT,
                GL32.GL_RGBA8UI,
                GL32.GL_RGBA_INTEGER,
                GL32.GL_UNSIGNED_BYTE,
                GL32.GL_NEAREST,
                GL32.GL_REPEAT
        );

        this.mappingBuffer = MemoryUtil.memAlloc(CHUNK_AMOUNT * CHUNK_AMOUNT * CHUNK_AMOUNT * 4);
        MemoryUtil.memSet(this.mappingBuffer, 0);

        LOGGER.info("Created: [{}x{}x{}] cloud voxel atlas", BUFFER_SIZE, BUFFER_SIZE, BUFFER_SIZE);
        LOGGER.info("Created: [{}x{}x{}x4] cloud mapping texture", CHUNK_AMOUNT, CHUNK_AMOUNT, CHUNK_AMOUNT);

        this.pos2chunk = new Int2ObjectOpenHashMap<>(TOTAL_CHUNK_AMOUNT);
        this.chunk2pos = new Object2IntOpenHashMap<>(TOTAL_CHUNK_AMOUNT);
        this.lru = new LinkedHashMap<>(TOTAL_CHUNK_AMOUNT, 0.75f, true);
        this.freeList = new IntArrayList(TOTAL_CHUNK_AMOUNT);
        for (int i = 0; i < TOTAL_CHUNK_AMOUNT; i++) {
            this.freeList.add(i);
        }
    }

    public void link(int cx, int cy, int cz, CloudChunk chunk) {
        if (cx < 0 || cx >= CHUNK_AMOUNT
                || cy < 0 || cy >= CHUNK_AMOUNT
                || cz < 0 || cz >= CHUNK_AMOUNT) {
            throw new IllegalArgumentException("Invalid chunk position [" + cx + "," + cy + "," + cz + "]");
        }

        if (chunk.isEmpty()) {
            LOGGER.info("Linked: [{},{},{}] cloud chunk to empty", cx, cy, cz);
            this.linkEmpty(cx, cy, cz);
        } else if (chunk.isFull()) {
            LOGGER.info("Linked: [{},{},{}] cloud chunk to full", cx, cy, cz);
            this.linkFull(cx, cy, cz);
        } else {
            int[] position = new int[3];
            int packed = this.uploadChunk(chunk);
            this.decode(packed, position);

            LOGGER.info("Linked: [{},{},{}] cloud chunk to [{},{},{}]", cx, cy, cz, position[0], position[1], position[2]);
            this.link(cx, cy, cz, position[0], position[1], position[2]);
        }
    }

    public void link(int cx, int cy, int cz, int ax, int ay, int az) {
        if (cx < 0 || cx >= CHUNK_AMOUNT
                || cy < 0 || cy >= CHUNK_AMOUNT
                || cz < 0 || cz >= CHUNK_AMOUNT) {
            throw new IllegalArgumentException("Invalid chunk position [" + cx + "," + cy + "," + cz + "]");
        }

        if (ax < 0 || ax >= CloudChunk.CHUNK_SIZE
                || ay < 0 || ay >= CloudChunk.CHUNK_SIZE
                || az < 0 || az >= CloudChunk.CHUNK_SIZE) {
            throw new IllegalArgumentException("Invalid atlas position [" + ax + "," + ay + "," + az + "]");
        }

        final int begin = (cx * CHUNK_AMOUNT * CHUNK_AMOUNT + cy * CHUNK_AMOUNT + cz) << 2;
        final int packed = (ax & 0xFF) | ((ay & 0xFF) << 8) | ((az & 0xFF) << 16) | (1 << 24);
        this.mappingBuffer.putInt(begin, packed);
//        this.mapping[begin] = (byte) ax;
//        this.mapping[begin + 1] = (byte) ay;
//        this.mapping[begin + 2] = (byte) az;
//        this.mapping[begin + 3] = (byte) 1;
    }

    public void linkEmpty(int cx, int cy, int cz) {
        if (cx < 0 || cx >= CHUNK_AMOUNT
                || cy < 0 || cy >= CHUNK_AMOUNT
                || cz < 0 || cz >= CHUNK_AMOUNT) {
            throw new IllegalArgumentException("Invalid chunk position [" + cx + "," + cy + "," + cz + "]");
        }

        final int begin = (cx * CHUNK_AMOUNT * CHUNK_AMOUNT + cy * CHUNK_AMOUNT + cz) << 2;
        final int packed = 0;
        this.mappingBuffer.putInt(begin, packed);
    }

    public void linkFull(int cx, int cy, int cz) {
        if (cx < 0 || cx >= CHUNK_AMOUNT
                || cy < 0 || cy >= CHUNK_AMOUNT
                || cz < 0 || cz >= CHUNK_AMOUNT) {
            throw new IllegalArgumentException("Invalid chunk position [" + cx + "," + cy + "," + cz + "]");
        }

        final int begin = (cx * CHUNK_AMOUNT * CHUNK_AMOUNT + cy * CHUNK_AMOUNT + cz) << 2;
        final int packed = 0xFFFFFFFF;
        this.mappingBuffer.putInt(begin, packed);
//        this.mapping[begin] = (byte) 255;
//        this.mapping[begin + 1] = (byte) 255;
//        this.mapping[begin + 2] = (byte) 255;
//        this.mapping[begin + 3] = (byte) 255;
    }

    public int uploadChunk(CloudChunk chunk) {
        int pos = this.chunk2pos.getOrDefault(chunk, -1);
        int[] position = new int[3];
        if (pos != -1) {
            this.lru.get(chunk);
            this.decode(pos, position);
            // exist
            LOGGER.info("Get voxels at [{},{},{}]", position[0], position[1], position[2]);
            return pos;
        }

        if (this.freeList.isEmpty()) {
            Map.Entry<CloudChunk, Integer> first = this.lru.entrySet().iterator().next();
            CloudChunk old = first.getKey();
            pos = first.getValue();

            this.lru.remove(old);
            this.pos2chunk.remove(pos);
            this.chunk2pos.removeInt(old);
        } else {
            pos = this.freeList.popInt();
        }

        this.lru.put(chunk, pos);
        this.chunk2pos.put(chunk, pos);
        this.pos2chunk.put(pos, chunk);

        this.decode(pos, position);

        LOGGER.info("Upload voxels at [{},{},{}]", position[0], position[1], position[2]);

        this.voxelTexture.uploadImage(
                position[0] * CloudChunk.CHUNK_SIZE,
                position[1] * CloudChunk.CHUNK_SIZE,
                position[2] * CloudChunk.CHUNK_SIZE,
                CloudChunk.CHUNK_SIZE,
                CloudChunk.CHUNK_SIZE,
                CloudChunk.CHUNK_SIZE,
                chunk.getBuffer().rewind()
        );

        return pos;
    }

    public void uploadMapping() {
        this.occupationTexture.upload(this.mappingBuffer);
    }

    @Override
    public void close() {
        this.voxelTexture.close();
        this.occupationTexture.close();

        MemoryUtil.memFree(this.mappingBuffer);
    }

    private int encode(int x, int y, int z) {
        return (x << 8) | (y << 4) | z;
    }

    private void decode(int pos, int[] out) {
        out[0] = (pos >> 8) & 0xF;
        out[1] = (pos >> 4) & 0xF;
        out[2] = pos & 0xF;
    }

    private int packRGBA(int r, int g, int b, int a) {
        return (r << 24) | (g << 16) | (b << 8) | a;
    }

    private static final class Chunk {
        
    }
}
