package org.chzz.market.common.config.aws;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BucketPrefix {
    AUCTION("auction"),
    PROFILE("profile");
    private final String name;

    public static boolean hasNameOf(String name) {
        return Arrays.stream(values())
                .anyMatch(bucketFolderName -> bucketFolderName.name.equals(name));
    }
}
