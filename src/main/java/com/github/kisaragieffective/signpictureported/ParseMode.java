package com.github.kisaragieffective.signpictureported;

import com.github.kisaragieffective.signpictureported.api.DisplayConfigurationParseResult;

public enum ParseMode {
    URL(DisplayConfigurationParseResult.class),
    TEXTURE(TextureParseResult.class),;

    ParseMode(Class<DisplayConfigurationParseResult> parseResultClass) {

    }
}
