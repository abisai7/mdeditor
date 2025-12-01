package com.abidev.mdeditor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reemplaza shortcodes :smile: por caracteres emoji reales usando un mapeo.
 */
public class EmojiProcessor {
    private static final Pattern SHORTCODE_PATTERN = Pattern.compile(":([a-zA-Z0-9_+-]{2,30}):");
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();

    static {
        try (InputStream in = EmojiProcessor.class.getResourceAsStream("/com/abidev/mdeditor/emoji-mapping.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                for (String k : props.stringPropertyNames()) {
                    EMOJI_MAP.put(k, props.getProperty(k));
                }
            }
        } catch (IOException ignored) {}
    }

    public static String replaceShortcodes(String markdown) {
        if (markdown == null || markdown.isEmpty()) return markdown;
        Matcher m = SHORTCODE_PATTERN.matcher(markdown);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = m.group(1);
            String emoji = EMOJI_MAP.get(code);
            if (emoji != null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(emoji));
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static Map<String, String> getEmojiMap() {
        return EMOJI_MAP;
    }
}

