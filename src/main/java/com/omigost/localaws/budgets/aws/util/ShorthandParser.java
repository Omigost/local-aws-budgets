package com.omigost.localaws.budgets.aws.util;

import org.springframework.data.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShorthandParser {


    public static Map<String, String> parseMap(final String specs) {
        return parseAnyMap(specs).getFirst();
    }

    public static Map<String, List<String>> parseMultiMap(final String specs) {
        return parseAnyMap(specs).getSecond();
    }

    private static Pair<Map<String, String>, Map<String, List<String>>> parseAnyMap(final String specs) {
        String specsString = specs;

        if (specsString.startsWith("{")) {
            specsString = specsString.substring(1, specsString.length() - 1);
        }

        specsString = specsString + ",";

        HashMap<String, String> m = new HashMap<>();
        HashMap<String, List<String>> mMulti = new HashMap<>();

        final int len = specsString.length();
        int level = 0;
        int lastCutoffPos = 0;

        String keyAcc = "";
        ArrayList<String> valuesAcc = new ArrayList<>();

        for (int i = 0; i < len; ++i) {
            if (specsString.charAt(i) == '=' && level == 0) {
                if (keyAcc.length() > 0) {
                    mMulti.put(keyAcc, valuesAcc);
                    valuesAcc = new ArrayList<>();
                }
                keyAcc = specsString.substring(lastCutoffPos, i).trim();
                lastCutoffPos = i + 1;
            } else if (specsString.charAt(i) == ',' && level == 0) {
                final String val = specsString.substring(lastCutoffPos, i);
                m.put(keyAcc, val);
                valuesAcc.add(val);
                if (i == len-1) {
                    mMulti.put(keyAcc, valuesAcc);
                    valuesAcc = new ArrayList<>();
                }
                lastCutoffPos = i + 1;
            } else if (specsString.charAt(i) == '{') {
                ++level;
            } else if (specsString.charAt(i) == '}') {
                --level;
            }
        }

        return Pair.of(m, mMulti);
    }

    public static List<String> parseArray(final String specs) {
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
