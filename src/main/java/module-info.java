module com.abidev.mdeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.kordamp.bootstrapfx.core;
    requires org.commonmark;

    opens com.abidev.mdeditor to javafx.fxml;
    exports com.abidev.mdeditor;
}