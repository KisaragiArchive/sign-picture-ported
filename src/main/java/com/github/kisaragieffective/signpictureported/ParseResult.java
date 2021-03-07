package com.github.kisaragieffective.signpictureported;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ParseResult {
    public final Optional<Double> offsetRight;
    public final Optional<Double> offsetUp;
    public final Optional<Double> offsetDepth;
    public final Optional<Double> rotateX;
    public final Optional<Double> rotateY;
    public final Optional<Double> rotateZ;
    public final Optional<Double> scaleX;
    public final Optional<Double> scaleY;
    public ParseResult(Optional<Double> offsetRight, Optional<Double> offsetUp, Optional<Double> offsetDepth,
                       Optional<Double> rotateX, Optional<Double> rotateY, Optional<Double> rotateZ,
                       Optional<Double> scaleX, Optional<Double> scaleY) {
        this.offsetRight = offsetRight;
        this.offsetUp = offsetUp;
        this.offsetDepth = offsetDepth;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public static ParseResult parse(String from) {
        MatchResult mr = Pattern.compile("(?:(\\d+)x(\\d+)|(x\\d+))?(?:(X\\d+))?(?:(Y\\d+))?(?:(Z\\d+))?(?:(R\\d+))?(?:(U\\d+))?(?:(D\\d+))")
                //                           ^~~~~~~~~~~~~ ^~~~~~~     ^~~~~~~     ^~~~~~~     ^~~~~~~     ^~~~~~~     ^~~~~~~     ^~~~~~~
                //                           Scale(2)       Scale(1)    X Deg       Y Deg       Z Deg       R ofs       U ofs       D ofs
                .matcher(from)
                .toMatchResult();
        return null;
    }
}
