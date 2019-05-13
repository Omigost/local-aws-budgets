package com.omigost.localaws.budgets.aws.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShorthandParser {

    private static Map<String, String> parseMap(final String specs) {
        String specsString = specs;

        if (specsString.startsWith("{")) {
            specsString = specsString.substring(1, specsString.length() - 1);
        }

        specsString = specsString + ",";

        HashMap<String, String> m = new HashMap<>();

        final int len = specsString.length();
        int level = 0;
        int lastCutoffPos = 0;

        String keyAcc = "";

        for (int i = 0; i < len; ++i) {
            if (specsString.charAt(i) == '=' && level == 0) {
                keyAcc = specsString.substring(lastCutoffPos, i).trim();
                lastCutoffPos = i + 1;
            } else if (specsString.charAt(i) == ',' && level == 0) {
                m.put(keyAcc, specsString.substring(lastCutoffPos, i));
                lastCutoffPos = i + 1;
            } else if (specsString.charAt(i) == '{') {
                ++level;
            } else if (specsString.charAt(i) == '}') {
                --level;
            }
        }

        return m;
    }

    private static List<String> parseArray(final String specs) {
        String specsString = specs;

        if (specsString.startsWith("[")) {
            specsString = specsString.substring(1, specsString.length() - 1);
        }

        specsString = specsString + ",";

        List<String> m = new ArrayList<>();

        final int len = specsString.length();
        int level = 0;
        int lastCutoffPos = 0;

        for (int i = 0; i < len; ++i) {
            if (specsString.charAt(i) == ',' && level == 0) {
                m.add(specsString.substring(lastCutoffPos, i).trim());
                lastCutoffPos = i + 1;
            } else if (specsString.charAt(i) == '{') {
                ++level;
            } else if (specsString.charAt(i) == '}') {
                --level;
            }
        }

        return m;
    }

    public static Map<String, String> parse(final String specs) {
        if (specs.startsWith("[")) {
            final List<String> list = parseArray(specs);
            final Map<String, String> m = new HashMap<>();
            int listKey = 0;

            for(String item : list) {
                m.put(((Object) listKey).toString(), item);
                ++listKey;
            }

            return m;
        } else {
            return parseMap(specs);
        }
    }
}
