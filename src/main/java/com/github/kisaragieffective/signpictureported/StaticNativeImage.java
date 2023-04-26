package com.github.kisaragieffective.signpictureported;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class StaticNativeImage {
    public static final LazyInitialize<NativeImageBackedTexture> errorImage = new LazyInitialize<>(StaticNativeImage::getErrorImage);
    private static NativeImageBackedTexture getErrorImage() {
        final NativeImage nini = new NativeImage(128, 128, false);
        for (int xv = 0; xv < 128; xv++) {
            nini.setColor(xv, 0, 0xFF_00_00_FF);
            nini.setColor(xv, 127, 0xFF_00_00_FF);
            nini.setColor(0, xv, 0xFF_00_00_FF);
            nini.setColor(127, xv, 0xFF_00_00_FF);
        }
        return new NativeImageBackedTexture(nini);
    }

    public static final LazyInitialize<NativeImageBackedTexture> loadingImage = new LazyInitialize<>(StaticNativeImage::getLoadingImage);

    private static NativeImageBackedTexture getLoadingImage() {
        final NativeImage nini = new NativeImage(128, 128, false);
        final int gray = 0x80808080;
        for (int xv = 0; xv < 128; xv++) {
            nini.setColor(xv, 0, gray);
            nini.setColor(xv, 127, gray);
            nini.setColor(0, xv, gray);
            nini.setColor(127, xv, gray);
        }
        return new NativeImageBackedTexture(nini);
    }
}
