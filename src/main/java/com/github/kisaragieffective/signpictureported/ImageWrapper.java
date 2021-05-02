package com.github.kisaragieffective.signpictureported;

import net.minecraft.client.texture.NativeImageBackedTexture;

import java.io.Closeable;
import java.lang.ref.SoftReference;
import java.util.Objects;

public class ImageWrapper implements Closeable {
    public final SoftReference<NativeImageBackedTexture> nibt;

    public ImageWrapper(NativeImageBackedTexture nibt) {
        this.nibt = new SoftReference<>(nibt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageWrapper imageWrapper = (ImageWrapper) o;
        return Objects.equals(nibt, imageWrapper.nibt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nibt);
    }

    @Override
    public String toString() {
        return "ImageWrapper{" +
                "nibt=" + nibt.get() +
                '}';
    }

    @Override
    public void close() {
        NativeImageBackedTexture nibt = this.nibt.get();
        if (nibt != null) nibt.close();
        this.nibt.clear();
    }
}
