package com.github.kisaragieffective.signpictureported;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.JsonHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InternalSpecialUtility {
    private InternalSpecialUtility() {}

    private static List<String> lineTags(CompoundTag dest) {
        return Collections.unmodifiableList(Arrays.asList(
                dest.getString("Text1"),
                dest.getString("Text2"),
                dest.getString("Text3"),
                dest.getString("Text4")
        ));
    }

    public static <T> T dummy() {
        throw new AssertionError("This statement shouldn't reached!");
    }

    public static List<String> getPlaintextLines(SignBlockEntity sbe) {
        CompoundTag dest = new CompoundTag();
        sbe.toTag(dest);
        List<String> lines = lineTags(dest);
        return lines.stream()
                .map(JsonHelper::deserialize)
                .map(doc -> {
                    String text = doc.getAsJsonPrimitive("text").getAsString();
                    StringBuilder textAccumerator = new StringBuilder(128);
                    if (text.isEmpty()) {
                        if (doc.has("extra")) {
                            // TODO reduce-like operation
                            doc.getAsJsonArray("extra")
                                    .forEach(elem ->
                                            textAccumerator.append(elem.getAsJsonObject().getAsJsonPrimitive("text").getAsString())
                                    );
                            text = textAccumerator.toString();
                        }
                    }

                    return text;
                })
                .collect(Collectors.toList());
    }

    /**
     * Flip horizontally. This method is in-place.
     * Converts
     * <pre>
     * 1 2 3
     * 4 5 6
     * 7 8 9
     * </pre>
     * to
     * <pre>
     * 3 2 1
     * 6 5 4
     * 9 8 7
     * </pre>
     * where 1 to 9 are its pixels.
     * @param ni
     * @return the flipped image
     */
    public static NativeImage mirrorHorizontally(NativeImage ni) {
        for (int y = 0; y < ni.getHeight(); y++) {
            for (int x = 0; x < ni.getWidth() / 2; x++) {
                int color1 = ni.getPixelColor(x, y);
                final int destX1 = ni.getWidth() - 1 - x;
                int color2 = ni.getPixelColor(destX1, y);
                ni.setPixelColor(destX1, y, color1);
                ni.setPixelColor(x, y, color2);
            }
        }
        return ni;
    }
}
