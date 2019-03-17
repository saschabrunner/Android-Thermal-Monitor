package com.gitlab.saschabrunner.thermalmonitor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean matchesAnyPattern(List<Pattern> patterns, String string) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(string).matches()) {
                return true;
            }
        }
        return false;
    }

    public static List<Pattern> createRegexPatterns(String[] strings) {
        List<Pattern> patterns = new ArrayList<>(strings.length);
        for (String string : strings) {
            patterns.add(Pattern.compile(string));
        }
        return patterns;
    }
}
