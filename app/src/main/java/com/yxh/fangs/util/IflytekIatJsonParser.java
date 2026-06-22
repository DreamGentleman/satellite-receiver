package com.yxh.fangs.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class IflytekIatJsonParser {

    private IflytekIatJsonParser() {
    }

    public static String parse(String resultJson) {
        StringBuilder builder = new StringBuilder();
        try {
            JSONObject root = new JSONObject(resultJson);
            JSONArray words = root.optJSONArray("ws");
            if (words == null) {
                return "";
            }

            for (int i = 0; i < words.length(); i++) {
                JSONObject word = words.optJSONObject(i);
                if (word == null) {
                    continue;
                }
                JSONArray candidates = word.optJSONArray("cw");
                if (candidates == null || candidates.length() == 0) {
                    continue;
                }
                JSONObject candidate = candidates.optJSONObject(0);
                if (candidate != null) {
                    builder.append(candidate.optString("w"));
                }
            }
        } catch (JSONException ignored) {
            return "";
        }
        return builder.toString();
    }
}
