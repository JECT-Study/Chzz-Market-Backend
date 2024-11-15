package org.chzz.market.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringCaseConverterTest {

    @Test
    void 대문자로변환_하이픈을언더스코어로변환() {
        // given
        String input = "example-string";
        String expectedOutput = "EXAMPLE_STRING";

        // when
        String result = StringCaseConverter.toUpperCaseWithUnderscores(input);

        // then
        assertThat(result).isEqualTo(expectedOutput);
    }

    @Test
    void 소문자로변환_언더스코어를하이픈으로변환() {
        // given
        String input = "EXAMPLE_STRING";
        String expectedOutput = "example-string";

        // when
        String result = StringCaseConverter.toLowerCaseWithHyphens(input);

        // then
        assertThat(result).isEqualTo(expectedOutput);
    }
}
