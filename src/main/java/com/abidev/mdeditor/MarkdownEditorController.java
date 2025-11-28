package com.abidev.mdeditor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.ResourceBundle;

public class MarkdownEditorController {

    @FXML private TextArea markdownEditor;
    @FXML private WebView markdownPreview;
    @FXML private ChoiceBox<String> headingChoice;

    private File currentFile;
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();
    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();
    private boolean suppressHistory = false;
    private boolean isDirty = false;

    @FXML private ResourceBundle resources;

    @FXML
    public void initialize() {
        // Actualizar la vista previa cuando el texto cambia
        markdownEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!suppressHistory) {
                undoStack.push(oldValue);
                // Limpiar redo cuando hay nueva edición
                redoStack.clear();
            }
            // marcar sucio y actualizar título
            markDirty();
            updatePreview(newValue);
        });
        // Inicializar encabezado choice
        if (headingChoice != null) {
            headingChoice.getItems().addAll("Normal", "H1", "H2", "H3", "H4", "H5", "H6");
            headingChoice.setValue("Normal");
            headingChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyHeading(newVal));
        }

        // Mostrar un mensaje de bienvenida inicial
        String welcomeMessage = "# Editor de Markdown\n\n" +
                "¡Bienvenido al editor de Markdown!\n\n" +
                "## Características\n\n" +
                "- Edición en tiempo real\n" +
                "- Vista previa en vivo\n" +
                "- Abrir y guardar archivos .md\n\n" +
                "## Cómo usar\n\n" +
                "1. **Nuevo**: Crea un nuevo documento\n" +
                "2. **Abrir**: Abre un archivo .md existente\n" +
                "3. **Guardar**: Guarda el documento actual\n" +
                "4. **Guardar Como**: Guarda el documento con un nuevo nombre\n\n" +
                "### Sintaxis Markdown básica\n\n" +
                "- `# Título` - Encabezado nivel 1\n" +
                "- `## Subtítulo` - Encabezado nivel 2\n" +
                "- `**negrita**` - **Texto en negrita**\n" +
                "- `*cursiva*` - *Texto en cursiva*\n" +
                "- `[enlace](url)` - Crear un enlace\n" +
                "- `- item` - Lista con viñetas\n" +
                "- `1. item` - Lista numerada\n" +
                "- `` `código` `` - Código en línea\n\n" +
                "¡Comienza a escribir!";

        suppressHistory = true; // evitar registrar mensaje inicial en historial
        markdownEditor.setText(welcomeMessage);
        suppressHistory = false;

        // Marcar como limpio después del mensaje inicial
        isDirty = false;
    }

    private void updatePreview(String markdown) {
        try {
            var document = parser.parse(markdown);
            String html = renderer.render(document);
            String styledHtml = """
                <html>
                <head>
                    <style>
                        body {font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif; line-height: 1.6; padding: 20px; max-width: 800px; margin: 0 auto; color: #333;}
                        h1, h2, h3, h4, h5, h6 {margin-top: 24px; margin-bottom: 16px; font-weight: 600; line-height: 1.25;}
                        h1 { font-size: 2em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
                        h2 { font-size: 1.5em; border-bottom: 1px solid #eaecef; padding-bottom: 0.3em; }
                        h3 { font-size: 1.25em; }
                        code { background-color: #f6f8fa; border-radius: 3px; padding: 0.2em 0.4em; font-family: 'Courier New', monospace; }
                        pre { background-color: #f6f8fa; border-radius: 3px; padding: 16px; overflow: auto; }
                        blockquote { border-left: 4px solid #dfe2e5; padding-left: 16px; color: #6a737d; }
                        ul, ol { padding-left: 2em; }
                        a { color: #0366d6; text-decoration: none; }
                        a:hover { text-decoration: underline; }
                        em { font-style: italic; }
                        strong { font-weight: bold; }
                        del { color: #555; }
                        u { text-decoration: underline; }
                    </style>
                </head>
                <body>
                """ + html + """
                </body>
                </html>
                """;
            markdownPreview.getEngine().loadContent(styledHtml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Métodos de formato
    private void applyHeading(String heading) {
        String beforeText = markdownEditor.getText();
        if (heading == null) return;
        int caret = markdownEditor.getCaretPosition();
        int lineStart = getLineStart(caret);
        String text = markdownEditor.getText();
        int lineEnd = text.indexOf('\n', lineStart);
        if (lineEnd == -1) lineEnd = text.length();
        String line = text.substring(lineStart, lineEnd).replaceFirst("^#+\\s*", "").trim();
        String prefix = switch (heading) {
            case "H1" -> "# ";
            case "H2" -> "## ";
            case "H3" -> "### ";
            case "H4" -> "#### ";
            case "H5" -> "##### ";
            case "H6" -> "###### ";
            default -> "";
        };
        String newLine = prefix + line;
        StringBuilder sb = new StringBuilder(text);
        sb.replace(lineStart, lineEnd, newLine);
        markdownEditor.setText(sb.toString());
        markdownEditor.positionCaret(Math.min(lineStart + newLine.length(), sb.length()));
        undoStack.push(beforeText);
        redoStack.clear();
    }

    private int getLineStart(int pos) {
        String txt = markdownEditor.getText();
        int p = txt.lastIndexOf('\n', Math.max(0, pos - 1));
        return p == -1 ? 0 : p + 1;
    }

    private void wrapSelection(String before, String after) {
        int start = markdownEditor.getSelection().getStart();
        int end = markdownEditor.getSelection().getEnd();
        if (start == end) {
            // No selección: insertar delimitadores y posicionar cursor
            markdownEditor.insertText(start, before + after);
            markdownEditor.positionCaret(start + before.length());
            return;
        }
        String selected = markdownEditor.getSelectedText();
        String replacement = before + selected + after;
        markdownEditor.replaceText(start, end, replacement);
        markdownEditor.selectRange(start + before.length(), start + before.length() + selected.length());
    }

    private void prefixSelectedLines(String prefix) {
        var selection = markdownEditor.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();
        String text = markdownEditor.getText();
        // Expandir a inicio y fin de línea
        int lineStart = getLineStart(start);
        int lineEnd = end;
        if (lineEnd < text.length()) {
            lineEnd = text.indexOf('\n', lineEnd);
            if (lineEnd == -1) lineEnd = text.length();
        }
        String block = text.substring(lineStart, lineEnd);
        String[] lines = block.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                lines[i] = prefix + lines[i];
            }
        }
        String replaced = String.join("\n", lines);
        StringBuilder sb = new StringBuilder(text);
        sb.replace(lineStart, lineEnd, replaced);
        markdownEditor.setText(sb.toString());
        markdownEditor.selectRange(lineStart, lineStart + replaced.length());
    }

    @FXML private void handleBold() { wrapSelectionWithHistory("**", "**"); }
    @FXML private void handleItalic() { wrapSelectionWithHistory("*", "*"); }
    @FXML private void handleStrikethrough() { wrapSelectionWithHistory("~~", "~~"); }
    @FXML private void handleUnderline() { wrapSelectionWithHistory("<u>", "</u>"); }
    @FXML private void handleInlineCode() { wrapSelectionWithHistory("`", "`"); }
    @FXML private void handleBlockquote() { prefixSelectedLinesWithHistory("> "); }
    @FXML private void handleUnorderedList() { prefixSelectedLinesWithHistory("- "); }
    @FXML private void handleOrderedList() { numberSelectedLinesWithHistory(); }
    @FXML private void handleCodeBlock() { wrapSelectionWithHistory("\n```\n", "\n```\n"); }
    @FXML private void handleLink() { wrapSelectionWithHistory("[", "](https://)"); }
    @FXML private void handleImage() { wrapSelectionWithHistory("![", "](https://ruta/imagen.png)"); }
    @FXML private void handleHorizontalRule() { wrapSelectionWithHistory("\n---\n", ""); }

    private void wrapSelectionWithHistory(String before, String after) {
        String beforeText = markdownEditor.getText();
        wrapSelection(before, after);
        undoStack.push(beforeText);
        redoStack.clear();
    }

    private void prefixSelectedLinesWithHistory(String prefix) {
        String beforeText = markdownEditor.getText();
        prefixSelectedLines(prefix);
        undoStack.push(beforeText);
        redoStack.clear();
    }

    private void numberSelectedLinesWithHistory() {
        String beforeText = markdownEditor.getText();
        numberSelectedLines();
        undoStack.push(beforeText);
        redoStack.clear();
    }
    private void numberSelectedLines() {
        var selection = markdownEditor.getSelection();
        int start = selection.getStart();
        int end = selection.getEnd();
        String text = markdownEditor.getText();
        if (start == end) { // solo cursor: numerar línea actual si tiene contenido
            int lineStart = getLineStart(start);
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd == -1) lineEnd = text.length();
            String line = text.substring(lineStart, lineEnd);
            String cleaned = line.replaceFirst("^\\d+\\.\\s+", "").trim();
            if (cleaned.isEmpty()) return; // no numerar línea vacía
            String newLine = "1. " + cleaned;
            StringBuilder sb = new StringBuilder(text);
            sb.replace(lineStart, lineEnd, newLine);
            markdownEditor.setText(sb.toString());
            markdownEditor.positionCaret(Math.min(lineStart + newLine.length(), sb.length()));
            return;
        }
        // Expandir selección a inicio y fin de línea
        int lineStart = getLineStart(start);
        int lineEnd = end;
        if (lineEnd < text.length()) {
            int nl = text.indexOf('\n', lineEnd);
            lineEnd = nl == -1 ? text.length() : nl;
        }
        String block = text.substring(lineStart, lineEnd);
        String[] lines = block.split("\n", -1);
        int counter = 1;
        for (int i = 0; i < lines.length; i++) {
            String original = lines[i];
            String cleaned = original.replaceFirst("^\\d+\\.\\s+", "").trim();
            if (cleaned.isEmpty()) {
                // conservar líneas vacías sin numeración
                lines[i] = original.isEmpty() ? "" : original;
                continue;
            }
            lines[i] = counter + ". " + cleaned;
            counter++;
        }
        String replaced = String.join("\n", lines);
        StringBuilder sb = new StringBuilder(text);
        sb.replace(lineStart, lineEnd, replaced);
        markdownEditor.setText(sb.toString());
        markdownEditor.selectRange(lineStart, lineStart + replaced.length());
    }

    @FXML
    private void handleUndo() {
        if (undoStack.isEmpty()) return;
        String current = markdownEditor.getText();
        String prev = undoStack.pop();
        suppressHistory = true;
        markdownEditor.setText(prev);
        suppressHistory = false;
        redoStack.push(current);
    }

    @FXML
    private void handleRedo() {
        if (redoStack.isEmpty()) return;
        String current = markdownEditor.getText();
        String next = redoStack.pop();
        suppressHistory = true;
        markdownEditor.setText(next);
        suppressHistory = false;
        undoStack.push(current);
    }

    private void markDirty() {
        if (!isDirty) {
            isDirty = true;
            updateWindowTitle();
        }
    }

    private void markClean() {
        if (isDirty) {
            isDirty = false;
            updateWindowTitle();
        }
    }

    private void updateWindowTitle() {
        try {
            Stage stage = getStage();
            if (stage == null) return;
            String name = (currentFile != null) ? currentFile.getName() : "Sin título";
            if (isDirty) {
                stage.setTitle("* " + name + " - Editor de Markdown");
            } else {
                stage.setTitle(name + " - Editor de Markdown");
            }
        } catch (NullPointerException e) {
            // La ventana aún no está disponible durante la inicialización
        }
    }

    // Cambio de idioma: ES
    @FXML private void handleLanguageEs() {
        switchLanguage(new Locale("es"));
    }
    // Cambio de idioma: EN
    @FXML private void handleLanguageEn() {
        switchLanguage(Locale.ENGLISH);
    }

    private void switchLanguage(Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("com.abidev.mdeditor.messages", locale);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/abidev/mdeditor/markdown-editor-view.fxml"), bundle);
            Parent root = loader.load();
            Stage stage = getStage();
            // Preservar contenido actual del editor
            MarkdownEditorController newController = loader.getController();
            newController.markdownEditor.setText(this.markdownEditor.getText());
            Scene newScene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(newScene);
            // Actualizar título según estado
            stage.setTitle(buildWindowTitle(bundle));
        } catch (Exception e) {
            showError("I18N", e.getMessage());
        }
    }

    private String buildWindowTitle(ResourceBundle bundle) {
        String baseTitle = bundle.getString("app.title.base");
        String name = (currentFile != null) ? currentFile.getName() : bundle.getString("app.title.untitled");
        String dirtyPrefix = isDirty ? bundle.getString("app.title.dirtyPrefix") : "";
        return dirtyPrefix + name + " - " + baseTitle;
    }

    // Confirmar descartar cambios si hay modificaciones
    private boolean confirmDiscardIfDirty(String action) {
        if (!isDirty) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("alert.unsaved.title"));
        alert.setHeaderText(resources.getString("alert.unsaved.header"));
        alert.setContentText(java.text.MessageFormat.format(resources.getString("alert.unsaved.contentPrefix"), action));
        ButtonType guardar = new ButtonType(resources.getString("alert.button.save"), ButtonBar.ButtonData.OK_DONE);
        ButtonType descartar = new ButtonType(resources.getString("alert.button.discard"), ButtonBar.ButtonData.NO);
        ButtonType cancelar = new ButtonType(resources.getString("alert.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(guardar, descartar, cancelar);
        var result = alert.showAndWait();
        if (result.isEmpty()) return false;
        if (result.get() == guardar) {
            handleSave();
            // tras guardar, continúa si se guardó
            return !isDirty;
        } else if (result.get() == descartar) {
            return true;
        }
        return false; // cancelar
    }

    // Método público para la ventana principal al cerrar
    public boolean confirmClose() {
        return confirmDiscardIfDirty("salir");
    }

    @FXML
    private void handleNew() {
        if (!confirmDiscardIfDirty("crear un nuevo documento")) return;
        // registrar estado anterior para deshacer
        if (!markdownEditor.getText().isEmpty()) {
            undoStack.push(markdownEditor.getText());
            redoStack.clear();
        }
        currentFile = null;
        markdownEditor.clear();
        markClean();
        updateWindowTitle();
        showInfo("Nuevo documento creado");
    }

    @FXML
    private void handleOpen() {
        if (!confirmDiscardIfDirty(resources.getString("file.open"))) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("fileChooser.open.title"));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Markdown", "*.md", "*.markdown", "*.txt")
        );
        java.io.File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            try {
                String content = java.nio.file.Files.readString(file.toPath());
                suppressHistory = true;
                markdownEditor.setText(content);
                suppressHistory = false;
                currentFile = file;
                markClean();
                updateWindowTitle();
                showInfo(java.text.MessageFormat.format(resources.getString("info.opened"), file.getName()));
            } catch (IOException e) {
                showError(resources.getString("error.open.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentFile != null) {
            saveToFile(currentFile);
        } else {
            handleSaveAs();
        }
    }

    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("fileChooser.save.title"));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos Markdown", "*.md")
        );
        if (currentFile == null) {
            fileChooser.setInitialFileName("document.md");
        } else {
            fileChooser.setInitialFileName(currentFile.getName());
        }
        java.io.File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            if (!file.getName().endsWith(".md")) {
                file = new java.io.File(file.getAbsolutePath() + ".md");
            }
            currentFile = file;
            saveToFile(file);
            markClean();
            updateWindowTitle();
        }
    }

    private void saveToFile(java.io.File file) {
        try {
            java.nio.file.Files.writeString(file.toPath(), markdownEditor.getText(),
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            markClean();
            updateWindowTitle();
            showInfo(java.text.MessageFormat.format(resources.getString("info.saved"), file.getName()));
        } catch (IOException e) {
            showError(resources.getString("error.save.title"), e.getMessage());
        }
    }

    @FXML
    private void handleExportHTML() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("fileChooser.export.title"));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos HTML", "*.html")
        );
        if (currentFile != null) {
            String baseName = currentFile.getName().replaceFirst("[.][^.]+$", "");
            fileChooser.setInitialFileName(baseName + ".html");
        } else {
            fileChooser.setInitialFileName("document.html");
        }
        java.io.File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                var document = parser.parse(markdownEditor.getText());
                String html = renderer.render(document);
                String fullHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset=\"UTF-8\">
                        <title>Documento Markdown</title>
                        <style>/* estilos */</style>
                    </head>
                    <body>
                    """ + html + """
                    </body>
                    </html>
                    """;
                java.nio.file.Files.writeString(file.toPath(), fullHtml,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                showInfo(java.text.MessageFormat.format(resources.getString("info.exported"), file.getName()));
            } catch (IOException e) {
                showError(resources.getString("error.export.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void handleExit() {
        if (!confirmDiscardIfDirty("salir")) return;
        System.exit(0);
    }

    private Stage getStage() {
        return (Stage) markdownEditor.getScene().getWindow();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources != null ? resources.getString("app.title.base") : "Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

