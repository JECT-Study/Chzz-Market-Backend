package org.chzz.market.common.config.aws;

import java.util.Arrays;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BucketPrefix {
    AUCTION("auction"),
    PROFILE("profile");
    private final String name;

    public static boolean hasNameOf(String name) {
        return Arrays.stream(values())
                .anyMatch(bucketFolderName -> bucketFolderName.name.equals(name));
    }

    public String createPath(final String fileName) {
        String fileId = UUID.randomUUID().toString();
        return String.format("%s/%s/%s", this.name, fileId, fileName);
    }
}
