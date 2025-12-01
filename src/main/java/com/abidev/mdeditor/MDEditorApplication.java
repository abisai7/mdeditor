package com.abidev.mdeditor;

import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.EmojiSpriteLoader;
import com.gluonhq.emoji.offline.LocalEmojiSpriteLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MDEditorApplication extends Application {
    private void loadEmojiFontFromResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                Font f = Font.loadFont(in, 12);
                // opcional: verificar nombre f.getName()
            }
            System.out.println("Loaded font from " + path);
        } catch (Exception ignored) {
            System.out.println("Error loading font from " + path);
        }
    }

    private void loadEmojiFonts() {
        // Intentar cargar desde recursos locales
        loadEmojiFontFromResource("/com/abidev/mdeditor/fonts/OpenSansEmoji.ttf");
        loadEmojiFontFromResource("/com/abidev/mdeditor/fonts/seguiemj.ttf");
    }

    private void initEmojiLoader() {
        try {
            // Forzar carga de datos
            EmojiData.shortNamesSet();
            // Inicializar loader de sprites si no está
            // La clase EmojiImageUtils depende de EmojiSpriteLoader inicializado.
            java.lang.reflect.Field f = EmojiSpriteLoader.class.getDeclaredField("initialized");
            f.setAccessible(true);
            boolean init = f.getBoolean(null);
            if (!init) {
                // Instanciar LocalEmojiSpriteLoader para offline
                LocalEmojiSpriteLoader loader = new LocalEmojiSpriteLoader();
                // Reflejar campo loader si existe
                try {
                    java.lang.reflect.Field lf = EmojiSpriteLoader.class.getDeclaredField("loader");
                    lf.setAccessible(true);
                    lf.set(null, loader);
                    f.setBoolean(null, true);
                    System.out.println("EmojiSpriteLoader inicializado manualmente con LocalEmojiSpriteLoader");
                } catch (NoSuchFieldException e) {
                    System.out.println("Campo loader no encontrado: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Fallo inicializando emoji loader: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        initEmojiLoader();
        //loadEmojiFonts();

        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("com.abidev.mdeditor.messages", locale);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/abidev/mdeditor/markdown-editor-view.fxml"), bundle);
        Parent root = loader.load();
        Scene scene = new Scene(root, 1200, 700);
        // Añadir Noto Color Emoji via Google Fonts y nuestro CSS local con fallback de fuentes
        List<String> stylesheets = List.of(
                "https://fonts.googleapis.com/css2?family=Noto+Color+Emoji&display=swap",
                getClass().getResource("/com/abidev/mdeditor/styles.css").toExternalForm()
        );
        scene.getStylesheets().addAll(stylesheets);
        stage.setScene(scene);
        String titleBase = bundle.getString("app.title.base");
        stage.setTitle(titleBase);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
