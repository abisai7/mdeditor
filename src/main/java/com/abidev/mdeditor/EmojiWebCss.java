package com.abidev.mdeditor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Provee CSS con @font-face incrustando la fuente de emojis desde recursos para WebView.
 */
public final class EmojiWebCss {
    private static String cachedCss;
    private EmojiWebCss() {}

    public static String getEmbeddedEmojiFontCss() {
        if (cachedCss != null) return cachedCss;
        String path = "/com/abidev/mdeditor/fonts/seguiemj.ttf"; // fuente emoji de Windows
        try (InputStream in = EmojiWebCss.class.getResourceAsStream(path)) {
            if (in == null) {
                // fallback silencioso
                cachedCss = "";
                return cachedCss;
            }
            byte[] bytes = readAllBytes(in);
            String b64 = Base64.getEncoder().encodeToString(bytes);
            cachedCss = "@font-face { font-family: 'AppEmoji'; src: url(data:font/ttf;base64," + b64 + ") format('truetype'); font-weight: normal; font-style: normal; }";
            return cachedCss;
        } catch (IOException e) {
            cachedCss = "";
            return cachedCss;
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.max(8192, in.available()));
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            bos.write(buf, 0, r);
        }
        return bos.toByteArray();
    }
}

