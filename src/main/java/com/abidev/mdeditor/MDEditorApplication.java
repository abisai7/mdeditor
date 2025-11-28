package com.abidev.mdeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MDEditorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MDEditorApplication.class.getResource("markdown-editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        // Obtener el controlador para manejar el cierre
        MarkdownEditorController controller = fxmlLoader.getController();

        // Establecer título inicial
        stage.setTitle("Sin título - Editor de Markdown");
        stage.setScene(scene);

        // Interceptar el evento de cierre de ventana
        stage.setOnCloseRequest(event -> {
            if (!controller.confirmClose()) {
                event.consume(); // Cancelar el cierre si el usuario cancela
            }
        });

        stage.show();
    }
}
