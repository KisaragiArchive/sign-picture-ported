package net.fabricmc.example;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InternalSpecialUtility {
    private InternalSpecialUtility() {}

    private static List<Tag> lineTags(CompoundTag dest) {
        return Collections.unmodifiableList(Arrays.asList(
                dest.get("Text1"),
                dest.get("Text2"),
                dest.get("Text3"),
                dest.get("Text4")
        ));
    }

    public static List<Text> getTextLines(SignBlockEntity sbe) {
        CompoundTag dest = new CompoundTag();
        sbe.toTag(dest);
        List<Tag> lines = lineTags(dest);
        return lines.stream()
                .map(x -> (StringTag) x)
                .map(x -> Text.Serializer.fromJson(x.asString()))
                .collect(Collectors.toList());
    }

    public static List<String> getPlaintextLines(SignBlockEntity sbe) {
        CompoundTag dest = new CompoundTag();
        sbe.toTag(dest);
        List<Tag> lines = lineTags(dest);
        return lines.stream()
                .map(x -> (StringTag) x)
                .map(x -> Text.Serializer.fromJson(x.asString()))
                .map(x -> x.asString())
                .collect(Collectors.toList());
    }
}
