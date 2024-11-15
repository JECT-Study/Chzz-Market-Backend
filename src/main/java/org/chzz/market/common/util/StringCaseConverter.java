package org.chzz.market.common.util;

public class StringCaseConverter {

    /**
     * 문자열을 대문자로 변환하고, '-'를 '_'로 바꿈 예: "example-string" -> "EXAMPLE_STRING"
     * 요청에서 Enum값을 매칭할때 클라이언트에선 소문자와 하이푼을 쓰기 때문에 변환
     *
     * @param source 변환할 입력 문자열
     * @return 변환된 문자열
     */
    public static String toUpperCaseWithUnderscores(String source) {
        if (source == null) {
            return null;
        }
        return source.trim().toUpperCase().replace("-", "_");
    }

    /**
     * 문자열을 소문자로 변환하고, '_'를 '-'로 바꿈 예: "EXAMPLE_STRING" -> "example-string"
     * Enum값을 내보낼때, 클라이언트에선 소문자와 하이푼을 쓰기 때문에 변환
     *
     * @param source 변환할 입력 문자열
     * @return 변환된 문자열
     */
    public static String toLowerCaseWithHyphens(String source) {
        if (source == null) {
            return null;
        }
        return source.trim().toLowerCase().replace("_", "-");
    }
}
