package com.github.kisaragieffective.signpictureported.internal;

import net.minecraft.block.entity.SignBlockEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InternalSpecialUtility {
    private InternalSpecialUtility() {}

    public static <T> T never() {
        throw new AssertionError("This statement shouldn't reached!");
    }

    public static List<String> getPlaintextLines(SignBlockEntity sbe) {
        return Collections.unmodifiableList(Arrays.asList(
                sbe.getTextOnRow(0, false).getString(),
                sbe.getTextOnRow(1, false).getString(),
                sbe.getTextOnRow(2, false).getString(),
                sbe.getTextOnRow(3, false).getString()
        ));
    }
}
