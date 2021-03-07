package com.github.kisaragieffective.signpictureported;

import net.minecraft.block.entity.SignBlockEntity;
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
}
