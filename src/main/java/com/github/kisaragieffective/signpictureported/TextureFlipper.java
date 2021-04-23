package com.github.kisaragieffective.signpictureported;

import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TextureFlipper {
    private TextureFlipper() {
        // no need to instantiate
    }

    /**
     * テクスチャを左右反転する。このメソッドはout-placeである。
     */
    @Contract("_->new")
    public static NativeImage flipHorizontal(@NotNull NativeImage ni) {
        final int w = ni.getWidth();
        final int h = ni.getHeight();
        final NativeImage buffer = new NativeImage(ni.getFormat(), w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                buffer.setPixelColor(w - 1 - x, y, ni.getPixelColor(x, y));
            }
        }
        return buffer;
    }
}
