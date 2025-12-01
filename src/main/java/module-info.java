module com.abidev.mdeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    requires org.kordamp.bootstrapfx.core;
    requires org.commonmark;
    requires com.gluonhq.emoji;
    requires com.gluonhq.emoji.offline;
    requires java.logging;
    requires java.net.http;
    requires java.desktop;
    requires atlantafx.base; // AtlantisFX

    opens com.abidev.mdeditor to javafx.fxml;
    exports com.abidev.mdeditor;
}