module com.abidev.mdeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing; // para SwingFXUtils

    requires org.kordamp.bootstrapfx.core;
    requires org.commonmark;
    requires com.gluonhq.emoji;
    requires com.gluonhq.emoji.offline; // sprites offline
    requires java.logging; // para Google API client
    requires java.net.http; // potencial uso HTTP nativo
    requires java.desktop; // para ImageIO/BufferedImage

    opens com.abidev.mdeditor to javafx.fxml;
    exports com.abidev.mdeditor;
}