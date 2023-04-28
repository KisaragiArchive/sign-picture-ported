package com.github.kisaragieffective.signpictureported.internal;

import com.github.kisaragieffective.signpictureported.SignPicturePorted;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PreciseCacheRepository {
    record Data(BlockPos pos, int dimensionId, Identifier id, URL url) {

    }

    record UniquePosition(DimensionType dimension, BlockPos pos) {}

    record UniqueKeyed<T>(UUID randomAllocated, T data) {
        static <T> UniqueKeyed<T> allocate(T data) {
            return new UniqueKeyed<>(UUID.randomUUID(), data);
        }
    }

    private final HashMap<URL, UniqueKeyed<NativeImageBackedTexture>> cacheByUrl = new HashMap<>(128);
    private final HashMap<Identifier, UniqueKeyed<NativeImageBackedTexture>> cacheByIdentifier = new HashMap<>(128);
    private final HashMap<UniquePosition, UniqueKeyed<NativeImageBackedTexture>> cacheByPosition = new HashMap<>(128);

    public void register(BlockPos pos, DimensionType dimension, URL source, NativeImageBackedTexture loaded) {
        final var texture = UniqueKeyed.allocate(loaded);
        Identifier newIdentifier = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("sgpc_reloaded", loaded);
        SignPicturePorted.LOGGER.debug("new dynamic identifier was allocated: {}", newIdentifier);
        cacheByUrl.put(source, texture);
        cacheByIdentifier.put(newIdentifier, texture);
        cacheByPosition.put(new UniquePosition(dimension, pos), texture);
    }

    public Optional<NativeImageBackedTexture> find(URL url) {
        return cacheByUrl.entrySet().stream()
                .filter((x) -> x.getKey().equals(url))
                .map((x) -> x.getValue().data())
                .findFirst();
    }

    public Optional<NativeImageBackedTexture> find(Identifier id) {
        return cacheByIdentifier.entrySet().stream()
                .filter((x) -> x.getKey().equals(id))
                .map((x) -> x.getValue().data())
                .findFirst();
    }

    public Optional<NativeImageBackedTexture> find(DimensionType dimension, BlockPos pos) {
        final var up = new UniquePosition(dimension, pos);

        return cacheByPosition.entrySet().stream()
                .filter((x) -> x.getKey().equals(up))
                .map((x) -> x.getValue().data())
                .findFirst();
    }

    public Identifier findTextureIdentifier(DimensionType dimension, BlockPos pos) {
        final var up = new UniquePosition(dimension, pos);

        return cacheByPosition.entrySet().stream()
                .filter(x -> x.getKey().equals(up))
                .map(x -> x.getValue().data())
                .findFirst()
                .flatMap(x -> cacheByIdentifier.entrySet().stream().filter(y -> y.getValue().data.equals(x)).findFirst())
                .orElseThrow(() -> new IllegalStateException("Identifier must exist (pos=" + pos + ", dim=" + dimension + ")"))
                .getKey();
    }

    public void releaseByDimensionId(DimensionType dim) {
        cacheByPosition.entrySet().stream()
                .filter(x -> x.getKey().dimension.equals(dim))
                .map(x -> x.getValue().data)
                .forEach(this::release);
    }

    public void releaseByChunk(DimensionType dim, int chunkX, int chunkZ) {
        final var toBeRemoved = cacheByPosition.entrySet().stream()
                .filter(x -> {
                    final var k = x.getKey();

                    return k.dimension == dim &&
                            chunkX * 16 <= k.pos.getX() && k.pos.getX() < (chunkX + 1) * 16 &&
                            chunkZ * 16 <= k.pos.getZ() && k.pos.getZ() < (chunkZ + 1) * 16;
                })
                .map(x -> x.getValue().data)
                .toList();

        toBeRemoved.forEach(this::release);
    }

    public void releaseByBlockPos(DimensionType dim, BlockPos pos) {
        final var toBeRemoved = cacheByPosition.entrySet().stream()
                .filter(x -> {
                    final var k = x.getKey();

                    return k.dimension.equals(dim) && k.pos.equals(pos);
                })
                .map(x -> x.getValue().data)
                .toList();

        toBeRemoved.forEach(this::release);
    }

    private void release(NativeImageBackedTexture nibt) {
        cacheByPosition.entrySet().stream().filter(x -> x.getValue().data.equals(nibt)).forEach(x -> x.getValue().data.close());
        cacheByIdentifier.entrySet().stream().filter(x -> x.getValue().data.equals(nibt)).forEach(x -> x.getValue().data.close());
        cacheByUrl.entrySet().stream().filter(x -> x.getValue().data.equals(nibt)).forEach(x -> x.getValue().data.close());

        cacheByPosition.entrySet().retainAll(cacheByPosition.entrySet().stream().filter(y -> y.getValue().data == nibt).toList());
        cacheByIdentifier.entrySet().retainAll(cacheByIdentifier.entrySet().stream().filter(y -> y.getValue().data == nibt).toList());
        cacheByUrl.entrySet().retainAll(cacheByUrl.entrySet().stream().filter(y -> y.getValue().data == nibt).toList());
    }
}
