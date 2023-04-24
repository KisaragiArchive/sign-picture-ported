package com.github.kisaragieffective.signpictureported;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class NativeImageFactory {
    public static final Box<NativeImageBackedTexture> errorImage = new Box<>(NativeImageFactory::getErrorImage);
    public static NativeImageBackedTexture getErrorImage() {
        final NativeImage nini = new NativeImage(128, 128, true);
        for (int xv = 0; xv < 128; xv++) {
            nini.setColor(xv, 0, 0xFF_00_00_FF);
            nini.setColor(xv, 127, 0xFF_00_00_FF);
            nini.setColor(0, xv, 0xFF_00_00_FF);
            nini.setColor(127, xv, 0xFF_00_00_FF);
        }
        return new NativeImageBackedTexture(nini);
    }

    public static final Box<NativeImageBackedTexture> loadingImage = new Box<>(NativeImageFactory::getLoadingImage);

    public static NativeImageBackedTexture getLoadingImage() {
        final NativeImage nini = new NativeImage(128, 128, true);
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
