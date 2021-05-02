package com.github.kisaragieffective.signpictureported;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class OutsideCache {
    private OutsideCache() {

    }

    public static final Map<BlockPos, ImageWrapper> cache = new ConcurrentHashMap<>();
    public static final Map<BlockPos, ImageWrapper> flippedCache = new ConcurrentHashMap<>();
    public static final Set<String> invalidURL = new HashSet<>();

    private static final Map<BlockPos, Identifier> identifierMap = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Identifier> flippedIdentifierMap = new ConcurrentHashMap<>();
    public static final Set<BlockPos> sbp = new HashSet<>();

    public static ImageWrapper putOrCached(BlockPos pos, Supplier<? extends NativeImageBackedTexture> nibt) {
        return putImage(cache, pos, nibt);
    }

    public static ImageWrapper putFlippedOrCached(BlockPos pos, Supplier<? extends NativeImageBackedTexture> flipped) {
        return putImage(flippedCache, pos, flipped);
    }

    public static void put(BlockPos pos, NativeImageBackedTexture nibt, boolean flipped) {
        putNewIdentifier(pos, nibt);
        if (flipped) {
            flippedCache.put(pos, new ImageWrapper(nibt));
        } else {
            cache.put(pos, new ImageWrapper(nibt));
        }
    }

    public static Optional<ImageWrapper> get(BlockPos pos, boolean flipped) {
        if (flipped) {
            return flippedCache.containsKey(pos) ? Optional.of(flippedCache.get(pos)) : Optional.empty();
        } else {
            return cache.containsKey(pos) ? Optional.of(cache.get(pos)) : Optional.empty();
        }
    }

    public static Set<? extends BlockPos> locations() {
        return identifierMap.keySet();
    }

    public static void drop(BlockPos pos) {
        if (flippedCache.containsKey(pos)) {
            final ImageWrapper nibt1 = flippedCache.get(pos);
            nibt1.close();
        }
        if (cache.containsKey(pos)) {
            final ImageWrapper nibt = flippedCache.get(pos);
            nibt.close();
        }
        identifierMap.remove(pos);
        flippedIdentifierMap.remove(pos);
    }

    private static ImageWrapper putImage(
            Map<BlockPos, ImageWrapper> m,
            BlockPos pos,
            Supplier<? extends NativeImageBackedTexture> v
    ) {
        return m.computeIfAbsent(pos, ig -> {
            SignPicturePorted.LOGGER.info("cache(" + pos + "): no hit, creating");
            return new ImageWrapper(v.get());
        });
    }

    public static Identifier putNewIdentifierOrCached(BlockPos pos, NativeImageBackedTexture nibt) {
        return identifierMap.computeIfAbsent(pos, p -> computeNewIdentifier(p, nibt));
    }

    public static Identifier putNewIdentifier(BlockPos pos, NativeImageBackedTexture nibt) {
        return identifierMap.put(pos, computeNewIdentifier(pos, nibt));
    }

    public static Identifier computeNewIdentifier(BlockPos pos, NativeImageBackedTexture nibt) {
        Identifier newIdentifier = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("sgpc_reloaded", nibt);
        SignPicturePorted.LOGGER.info("New identifier payed out (for " + pos + "): " + newIdentifier);
        return newIdentifier;
    }

    public static Identifier putFlippedNewIdentifierOrCached(BlockPos pos, NativeImageBackedTexture nibt) {
        return flippedIdentifierMap.computeIfAbsent(pos, p -> computeNewIdentifier(p, nibt));
    }

    public static Identifier putFlippedNewIdentifier(BlockPos pos, NativeImageBackedTexture nibt) {
        return flippedIdentifierMap.put(pos, computeNewIdentifier(pos, nibt));
    }

    public static void invalidate() {
        OutsideCache.cache.values()
                .forEach(x -> x.close());
        OutsideCache.cache.clear();
        OutsideCache.flippedCache.values()
                .forEach(x -> x.close());
        OutsideCache.identifierMap.clear();
        OutsideCache.invalidURL.clear();
    }
}
