package com.github.kisaragieffective.signpictureported.api;

import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.kisaragieffective.signpictureported.Functions.compose;
import static com.github.kisaragieffective.signpictureported.Functions.switchBasedNullish;

public final class DisplayConfigurationParseResult {
    public final double offsetRight;
    public final double offsetUp;
    public final double offsetDepth;
    public final double rotateX;
    public final double rotateY;
    public final double rotateZ;
    public final double scaleX;
    public final double scaleY;

    public DisplayConfigurationParseResult(OptionalDouble offsetRight, OptionalDouble offsetUp, OptionalDouble offsetDepth,
                                           OptionalDouble rotateX, OptionalDouble rotateY, OptionalDouble rotateZ,
                                           OptionalDouble scaleX, OptionalDouble scaleY) {
        this(
                offsetRight.orElse(0.0),
                offsetUp.orElse(0.0),
                offsetDepth.orElse(0.0),
                rotateX.orElse(0.0),
                rotateY.orElse(0.0),
                rotateZ.orElse(0.0),
                scaleX.orElse(1.0),
                scaleY.orElse(1.0)
        );
    }

    public DisplayConfigurationParseResult(double offsetRight, double offsetUp, double offsetDepth,
                                           double rotateX, double rotateY, double rotateZ,
                                           double scaleX, double scaleY) {
        this.offsetRight = offsetRight;
        this.offsetUp = offsetUp;
        this.offsetDepth = offsetDepth;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public DisplayConfigurationParseResult(OptionalDouble offsetRight, OptionalDouble offsetUp, OptionalDouble offsetDepth,
                                           OptionalDouble rotateX, OptionalDouble rotateY, OptionalDouble rotateZ,
                                           OptionalDouble scale) {
        this(offsetRight, offsetUp, offsetDepth, rotateX, rotateY, rotateZ, scale, scale);
    }

    /**
     * ParseResult which has all fallback parameters
     */
    public static final DisplayConfigurationParseResult DEFAULT = new DisplayConfigurationParseResult(
            OptionalDouble.empty(),
            OptionalDouble.empty(),
            OptionalDouble.empty(),
            OptionalDouble.empty(),
            OptionalDouble.empty(),
            OptionalDouble.empty(),
            OptionalDouble.empty(),
            OptionalDouble.empty()
    );

    public static final String DOUBLE = "[-+]?(?:\\d+|\\d+\\.\\d+)";
    public static final String REGEX = "(?:(?<scale2>(?<sx>" + DOUBLE + ")x(?<sy>" + DOUBLE + "))" +
            "|(?<scale1>x(?<s>" + DOUBLE + ")))?" +
            "(?<x>X" + DOUBLE + ")?(?<y>Y" + DOUBLE + ")?(?<z>Z" + DOUBLE + ")?" +
            "(?<r>R" + DOUBLE + ")?(?<u>U" + DOUBLE + ")?(?<d>D" + DOUBLE + ")?";
    public static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Parse and construct. If argument doesn't contain any fragment, it will
     * default to default one.
     * @param from string to parse
     * @return parse result
     */
    public static DisplayConfigurationParseResult parse(String from) {
        Matcher m = PATTERN
                .matcher(from);
        if (m.matches()) {
            Function<String, OptionalDouble> extractor = groupName -> switchBasedNullish(
                    m.group(groupName),
                    compose(Double::parseDouble, OptionalDouble::of),
                    OptionalDouble::empty
            );

            final OptionalDouble scaleX;
            final OptionalDouble scaleY;
            if (m.group("scale2") != null) {
                scaleX = extractor.apply("sx");
                scaleY = extractor.apply("sy");
            } else if (m.group("scale1") != null) {
                scaleX = scaleY = extractor.apply("s");
            } else {
                scaleX = scaleY = OptionalDouble.empty();
            }

            final OptionalDouble rotateX = extractor.apply("x");
            final OptionalDouble rotateY = extractor.apply("y");
            final OptionalDouble rotateZ = extractor.apply("z");
            final OptionalDouble offsetRight = extractor.apply("r");
            final OptionalDouble offsetUp = extractor.apply("u");
            final OptionalDouble offsetDepth = extractor.apply("d");
            return new DisplayConfigurationParseResult(offsetRight, offsetUp, offsetDepth, rotateX, rotateY, rotateZ, scaleX, scaleY);
        } else {
            return DisplayConfigurationParseResult.DEFAULT;
        }
    }
}
