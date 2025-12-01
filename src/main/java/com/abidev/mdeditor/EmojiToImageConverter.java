package com.abidev.mdeditor;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.util.EmojiImageUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;

/**
 * Convierte emojis Unicode en el HTML a elementos img con data URI usando EmojiImageUtils para capturar sprite y SwingFXUtils para PNG.
 */
public class EmojiToImageConverter {

    /**
     * Reemplaza caracteres emoji Unicode con tags img que contengan la imagen en un data URI.
     */
    public static String convertEmojisToImages(String html) {
        StringBuilder result = new StringBuilder();
        int[] codePoints = html.codePoints().toArray();

        for (int i = 0; i < codePoints.length; i++) {
            int cp = codePoints[i];
            // Detectar si es un emoji (rango simplificado)
            if (isEmojiCodepoint(cp)) {
                // Intentar construir secuencia completa (puede haber múltiples codepoints)
                StringBuilder emojiSeq = new StringBuilder();
                emojiSeq.appendCodePoint(cp);

                // Mirar adelante para ZWJ sequences, skin tones, etc.
                while (i + 1 < codePoints.length && isEmojiRelated(codePoints[i + 1])) {
                    i++;
                    emojiSeq.appendCodePoint(codePoints[i]);
                }

                String emojiStr = emojiSeq.toString();
                Optional<Emoji> emojiOpt = EmojiData.emojiFromUnicodeString(emojiStr);

                if (emojiOpt.isPresent()) {
                    String imgTag = createImgTag(emojiOpt.get());
                    result.append(imgTag);
                } else {
                    // No se encontró, dejar el caracter original
                    result.append(emojiStr);
                }
            } else {
                result.appendCodePoint(cp);
            }
        }

        return result.toString();
    }

    private static boolean isEmojiCodepoint(int cp) {
        return (cp >= 0x1F300 && cp <= 0x1FAFF) || // Misc Symbols and Pictographs, Emoticons, etc.
               (cp >= 0x2600 && cp <= 0x27BF) ||   // Misc symbols
               (cp >= 0x1F600 && cp <= 0x1F64F) || // Emoticons
               (cp >= 0x1F680 && cp <= 0x1F6FF) || // Transport and Map
               (cp >= 0x2700 && cp <= 0x27BF) ||   // Dingbats
               (cp == 0x200D) ||                    // ZWJ
               (cp >= 0xFE00 && cp <= 0xFE0F);      // Variation selectors
    }

    private static boolean isEmojiRelated(int cp) {
        return isEmojiCodepoint(cp) ||
               (cp >= 0x1F3FB && cp <= 0x1F3FF); // Skin tone modifiers
    }

    private static String createImgTag(Emoji emoji) {
        var view = EmojiImageUtils.emojiView(emoji, 20);
        // Renderizar ImageView a imagen y codificar PNG en data URI
        WritableImage snapshot = view.snapshot(new SnapshotParameters(), null);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", out);
            String b64 = Base64.getEncoder().encodeToString(out.toByteArray());
            String alt = emoji.getShortName();
            return "<img alt='" + escapeHtml(alt) + "' style='vertical-align:middle' width='20' height='20' src='data:image/png;base64," + b64 + "'/>";
        } catch (Exception ex) {
            // fallback a texto unicode si hay error
            return emoji.getText();
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * Procesa el HTML de salida y reemplaza emojis solo en nodos de texto (fuera de etiquetas),
     * evitando corromper el marcado.
     */
    public static String convertEmojisToImagesInHtml(String html) {
        StringBuilder out = new StringBuilder(html.length() + 256);
        StringBuilder textBuf = new StringBuilder();
        boolean inTag = false;
        boolean inQuotes = false;
        char quoteChar = '\0';

        for (int i = 0; i < html.length(); i++) {
            char ch = html.charAt(i);
            if (!inTag) {
                if (ch == '<') {
                    // volcar texto procesado
                    if (textBuf.length() > 0) {
                        out.append(convertEmojisToImages(textBuf.toString()));
                        textBuf.setLength(0);
                    }
                    inTag = true;
                    out.append(ch);
                } else {
                    textBuf.append(ch);
                }
            } else {
                // dentro de etiqueta: manejar comillas para no cerrar antes de tiempo
                if (inQuotes) {
                    if (ch == quoteChar) {
                        inQuotes = false;
                        quoteChar = '\0';
                    }
                } else {
                    if (ch == '\'' || ch == '"') {
                        inQuotes = true;
                        quoteChar = ch;
                    } else if (ch == '>') {
                        inTag = false;
                    }
                }
                out.append(ch);
            }
        }
        // volcar resto de texto
        if (textBuf.length() > 0) {
            out.append(convertEmojisToImages(textBuf.toString()));
        }
        return out.toString();
    }
}
