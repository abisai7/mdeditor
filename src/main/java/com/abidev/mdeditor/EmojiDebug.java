package com.abidev.mdeditor;

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;

public class EmojiDebug {
    public static void main(String[] args) {
        String[] samples = {"smile","grinning","wave","thumbsup"};
        for (String s : samples) {
            var opt = EmojiData.emojiFromShortName(s);
            System.out.println("ShortName=" + s + " present=" + opt.isPresent());
            if (opt.isPresent()) {
                Emoji e = opt.get();
                System.out.println("  getShortName=" + e.getShortName());
                System.out.println("  getText=" + e.getText());
                System.out.println("  shortNamesSet contains? " + EmojiData.shortNamesSet().contains(s));
                var unicodeOpt = EmojiData.emojiForText(s);
                System.out.println("  emojiForText(" + s + ")=" + (unicodeOpt.isPresent()? unicodeOpt.get() : "<empty>"));
                System.out.println("  textList size=" + e.getTextList().size());
                e.getTextList().forEach(t -> System.out.println("    textList item=" + t));
            }
        }
    }
}

