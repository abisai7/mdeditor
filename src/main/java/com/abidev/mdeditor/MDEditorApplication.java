package com.abidev.mdeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class MDEditorApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("com.abidev.mdeditor.messages", locale);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/abidev/mdeditor/markdown-editor-view.fxml"), bundle);
        Scene scene = new Scene(loader.load(), 1200, 700);
        stage.setScene(scene);
        String titleBase = bundle.getString("app.title.base");
        stage.setTitle(titleBase);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
