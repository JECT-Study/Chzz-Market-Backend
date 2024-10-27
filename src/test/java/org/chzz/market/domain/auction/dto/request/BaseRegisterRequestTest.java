package org.chzz.market.domain.auction.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BaseRegisterRequestTest {

    private static final String DESCRIPTION_REGEX = "^(?:(?:[^\\n]*\\n){0,10}[^\\n]*$)"; // 개행문자 10개를 제한
    private static final Pattern pattern = Pattern.compile(DESCRIPTION_REGEX);

    @DisplayName("정규식 - 매칭이 되어야 하는 경우")
    @ParameterizedTest(name = "{index} - 문자열 매칭: {0}")
    @ValueSource(strings = {
            "", // 빈 문자열
            "          ", // 공백만 있는 경우
            "This is a simple string with no newline.", // 개행 없는 짧은 문자열
            "Hello\nWorld\nThis\nis\na\ntest\nstring.", // 개행 6번 포함
            "SingleLine", // 개행 없이 10자 미만
            "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10", // 개행 9번
            "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10\n", //개행 10번 마지막 개행
            "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10\nLine11", //개행 10번
            "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10\n ", //개행 10번 + 마지막 띄어쓰기
            "\n", //개행문자 1번
            "\n\n\n\n\n\n\n\n\n\n" //개행만 10번
    })
    void testDescriptionRegexMatches(String input) {
        // 매칭이 되어야 함
        assertThat(pattern.matcher(input).matches()).isTrue();
    }

    @DisplayName("정규식 - 매칭이 되지 않아야 하는 경우")
    @ParameterizedTest(name = "{index} - 문자열 매칭 실패 예상: {0}")
    @ValueSource(strings = {
            "Exceeding\nnewlines\nlimit\nby\nadding\nextra\nnewlines\nhere\nbeyond\nallowed\nlimit.\n.", // 개행 11번
            "Exceeding\nnewlines\nlimit\nby\nadding\nextra\nnewlines\nhere\nbeyond\nallowed\nlimit.\n", // 개행 11번
    })
    void testDescriptionRegexDoesNotMatch(String input) {
        // 매칭이 되지 않아야 함
        assertThat(pattern.matcher(input).matches()).isFalse();
    }
}

