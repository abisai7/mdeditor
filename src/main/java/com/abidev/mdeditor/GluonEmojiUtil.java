package com.abidev.mdeditor;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.util.EmojiImageUtils;
import javafx.scene.image.ImageView;

import java.util.Optional;

/**
 * Utilidades simples para trabajar con Gluon Emoji.
 */
public class GluonEmojiUtil {

    /** Obtiene el Emoji por shortcode (:smile:) o devuelve empty. */
    public static Optional<Emoji> fromShortcode(String shortcode) {
        if (shortcode == null) return Optional.empty();
        String cleaned = shortcode.replaceAll("^:+|:+$", "");
        return EmojiData.emojiFromShortName(cleaned);
    }

    /** Devuelve ImageView listo para usar para el emoji dado (por Unicode). */
    public static Optional<ImageView> imageViewFromUnicode(String unicode, double size) {
        if (unicode == null || unicode.isEmpty()) return Optional.empty();
        return EmojiData.emojiFromUnicodeString(unicode)
                .map(e -> EmojiImageUtils.emojiView(e, size));
    }

    /** Devuelve ImageView listo para usar para shortcode tipo :smile:. */
    public static Optional<ImageView> imageViewFromShortcode(String shortcode, double size) {
        return fromShortcode(shortcode).map(e -> EmojiImageUtils.emojiView(e, size));
    }

    /** Reemplaza shortcodes :smile: presentes en el texto por el Unicode del emoji. */
    public static String replaceShortcodesWithUnicode(String text) {
        if (text == null || text.isEmpty()) return text;
        // reutilizar patrón del EmojiProcessor
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(":([a-zA-Z0-9_+-]{2,30}):");
        java.util.regex.Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = m.group(1);
            var opt = EmojiData.emojiFromShortName(code);
            if (opt.isPresent()) {
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(opt.get().getText()));
            } else {
                m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(m.group()));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
