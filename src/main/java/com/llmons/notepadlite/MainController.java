package com.llmons.notepadlite;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.HexFormat;
import java.util.Optional;

public class MainController {
    public static MainController mainController;

    // ********** 字体 **********
    public String family = "System";
    public FontWeight style = FontWeight.NORMAL;    // 字体样式直接使用内置 FontWeight
    public Integer size = 15;
    public Color color = Color.BLACK;

    // ********** 文本区 **********
    @FXML
    public TextArea textArea;

    // ********** 文件保存 **********
    BooleanProperty savedP = new SimpleBooleanProperty(this, "saved", true);
    String filePath;    // 保存文件路径

    // ********** 状态栏 **********
    @FXML
    private Text RowColumn;
    @FXML
    private Text numOfCh;
    @FXML
    private Text chEncoding;


    public void initialize() {
        mainController = this;

        // 监听savedP，用于Title更改
        savedP.addListener((observable, oldValue, newValue) -> {
            String oldT = NotePadLiteApplication.primaryStage.getTitle();

            int len = oldT.length();
            if (newValue && oldT.charAt(len - 1) == '*')
                NotePadLiteApplication.primaryStage.setTitle(oldT.substring(0, len - 1));
            else if (oldValue && oldT.charAt(len - 1) != '*')
                NotePadLiteApplication.primaryStage.setTitle(oldT + '*');
        });


        // 光标位置变化监听
        textArea.caretPositionProperty().addListener(((observable, oldValue, newValue) -> {
            int caretPos = (int) newValue;
            String textBeforeCaretPos = textArea.getText().substring(0, caretPos);
            int row = 1, col = 1;

            if (!textBeforeCaretPos.isEmpty()) {     // 当caretPos为0时特判，不需要做操作
                row = textBeforeCaretPos.split("\n").length;
                // 如果最后一个字符是'\n'，说明caret在新的一行开始处且没有字符，此时row实际需要加一
                if (textBeforeCaretPos.charAt(caretPos - 1) == '\n') ++row;
                col = caretPos - textBeforeCaretPos.lastIndexOf("\n");
            }

            RowColumn.setText("行" + row + "，列" + col);
        }));

        // TextArea内容变化监听
        textArea.textProperty().addListener((observableValue, oldValue, newValue) -> {
            savedP.setValue(false);
            numOfCh.setText("字符数：" + newValue.length());
        });

        chEncoding.setText("UTF-8");

    }

    // ******************** 文件菜单事件 ********************
    @FXML
    void createEvent() throws IOException {
        // 未保存时的操作
        if (!savedP.get()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("文件未保存，是否保存？");

            ButtonType save = new ButtonType("保存");
            ButtonType unSave = new ButtonType("不保存");
            ButtonType cancel = new ButtonType("取消");
            alert.getButtonTypes().setAll(save, unSave, cancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == save) {
                if (filePath == null) saveAsEvent();  //如果没路径，则应调用‘另存为’
                else saveEvent();   //否则直接保存
            } else if (result.isPresent() && result.get() == cancel) return;  // 点击取消，停留在当前页面
        }

        // 关闭当前窗口
        Stage currStage = NotePadLiteApplication.primaryStage;
        currStage.close();

        // 创建新窗口
        FXMLLoader fxmlLoader = new FXMLLoader(NotePadLiteApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        Stage newStage = new Stage();
        newStage.setTitle("无标题*");
        newStage.setScene(scene);
        NotePadLiteApplication.primaryStage = newStage;
        newStage.show();
    }

    @FXML
    void openEvent() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text File", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File file = fileChooser.showOpenDialog(NotePadLiteApplication.primaryStage);
        if (file == null) return;

        filePath = file.getAbsolutePath();
        // 字符流读取
        try (Reader reader = new FileReader(file)) {
            StringBuilder stringBuilder = new StringBuilder();
            while (reader.ready()) stringBuilder.append((char) reader.read());
            textArea.setText(stringBuilder.toString());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("文件打开错误");
        }
        NotePadLiteApplication.primaryStage.setTitle(file.getName());
        savedP.setValue(true);
    }

    @FXML
    void saveEvent() {
        if (filePath == null) return; // saveEvent依靠的是当前路径，需要保证不空

        File file = new File(filePath);
        try (Writer writer = new FileWriter(file)) {
            String s = textArea.getText();
            writer.write(s);
            writer.flush();
            savedP.set(true);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("文件写入错误");
        }
    }

    @FXML
    void saveAsEvent() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text File", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File file = fileChooser.showSaveDialog(NotePadLiteApplication.primaryStage);
        if (file == null) return;

        // 由于‘另存为’操作结束后，窗口仍需停留，filePath应当先暂存，调用saveEvent()后再还原
        String currPath = filePath;   // 暂存当前路径
        filePath = file.getAbsolutePath();
        saveEvent();
        filePath = currPath;  // 还原
    }

    // ******************** 编辑菜单事件 ********************
    @FXML
    void undoEvent() {
        textArea.undo();
    }

    @FXML
    void redoEvent() {
        textArea.redo();
    }

    @FXML
    void findEvent() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NotePadLiteApplication.class.getResource("find-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320 * 2, 240);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("查找");

        stage.show();
    }

    @FXML
    void replaceEvent() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NotePadLiteApplication.class.getResource("replace-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320 * 2, 240);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("替换");

        stage.show();
    }

    // ******************** 选项菜单事件 ********************
    @FXML
    void fontEvent() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NotePadLiteApplication.class.getResource("font-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 480);
        Stage fontStage = new Stage();
        fontStage.setScene(scene);
        fontStage.setTitle("字体");

        // 监听窗口关闭
        fontStage.setOnCloseRequest(windowEvent -> {
            textArea.setFont(Font.font(family, style, size));

            // 字体颜色修改使用css
            String colorS = color.toString();
            int r = HexFormat.fromHexDigits(colorS, 2, 4);
            int g = HexFormat.fromHexDigits(colorS, 4, 6);
            int b = HexFormat.fromHexDigits(colorS, 6, 8);
            int a = HexFormat.fromHexDigits(colorS, 8, 10);
            textArea.setStyle(String.format("-fx-text-fill:rgba(%d,%d,%d,%d)", r, g, b, a));
        });

        fontStage.show();
    }

    @FXML
    public void wrapText() {
        textArea.setWrapText(!textArea.isWrapText());   // 设置自动换行，取反即可实现切换
    }

    // ******************** 帮助菜单事件 ********************
    @FXML
    void aboutEvent() {
        String aboutText = """
                NotePad Lite 1.0
                by llmons
                2024
                """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText(null);
        alert.setContentText(aboutText);

        alert.showAndWait();
    }

    // ******************** 右键菜单事件 ********************
    @FXML
    void cutEvent() {
        // 获取选中的文字
        String selectText = textArea.getSelectedText();
        if (selectText == null || selectText.isEmpty()) return;

        // 放入剪切板
        ClipboardContent content = new ClipboardContent();
        content.putString(selectText);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);

        // 获取光标位置和选取文本的锚位置，二者的位置正好包括了整段选中的文字
        int caretPosition = textArea.getCaretPosition();
        int anchor = textArea.getAnchor();
        int start = Math.min(caretPosition, anchor);
        int end = Math.max(caretPosition, anchor);
        textArea.deleteText(start, end);
    }

    @FXML
    void copyEvent() {
        // 获取选中的文字
        String selectText = textArea.getSelectedText();
        if (selectText == null || selectText.isEmpty()) return;

        // 放入剪切板
        ClipboardContent content = new ClipboardContent();
        content.putString(selectText);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    @FXML
    void pasteEvent() {
        // 获取剪切板内容
        Clipboard clipboard = Clipboard.getSystemClipboard();
        String clipboardText = clipboard.getString();
        if (clipboardText == null || clipboardText.isEmpty()) return;

        // 获取光标位置并插入
        int caretPosition = textArea.getCaretPosition();
        textArea.insertText(caretPosition, clipboardText);
    }

    @FXML
    void deleteEvent() {
        // 获取选中的文字
        String selectText = textArea.getSelectedText();
        if (selectText == null || selectText.isEmpty()) return;

        // 获取光标位置和选取文本的锚位置，二者的位置正好包括了整段选中的文字
        int caretPosition = textArea.getCaretPosition();
        int anchor = textArea.getAnchor();
        int start = Math.min(caretPosition, anchor);
        int end = Math.max(caretPosition, anchor);
        textArea.deleteText(start, end);
    }

    @FXML
    void selectAllEvent() {
        textArea.selectAll();
    }

}