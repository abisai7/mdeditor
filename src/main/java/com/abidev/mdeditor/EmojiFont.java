package com.abidev.mdeditor;

import javafx.scene.text.Font;

/**
 * Utilidad para gestionar fuentes de emoji en la aplicación.
 */
public class EmojiFont {
    private static Font emojiFont;

    public static void initialize() {
        // Cargar OpenSansEmoji como la fuente principal de emoji
        emojiFont = Font.loadFont(
            EmojiFont.class.getResourceAsStream("/com/abidev/mdeditor/fonts/OpenSansEmoji.ttf"),
            13
        );

        if (emojiFont != null) {
            System.out.println("EmojiFont initialized: " + emojiFont.getName());
        } else {
            System.out.println("Failed to initialize EmojiFont");
        }
    }

    public static Font getEmojiFont(double size) {
        if (emojiFont == null) {
            initialize();
        }
        return Font.font(emojiFont.getFamily(), size);
    }

    public static String getEmojiFontFamily() {
        if (emojiFont == null) {
            initialize();
        }
        return emojiFont != null ? emojiFont.getFamily() : "System";
    }
}

