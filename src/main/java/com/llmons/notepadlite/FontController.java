package com.llmons.notepadlite;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HexFormat;

import static com.llmons.notepadlite.MainController.mainController;

public class FontController {
    @FXML
    private ComboBox<String> fontFamily;
    @FXML
    private ComboBox<FontWeight> fontStyle;
    @FXML
    private ComboBox<Integer> fontSize;
    @FXML
    private ColorPicker fontColor;
    @FXML
    private Text simpleText;    // 示例文本

    public void initialize() {
        fontFamily.getItems().addAll(Font.getFamilies());
        fontStyle.getItems().addAll(FontWeight.values());
        for (int i = 1; i <= 96; ++i) fontSize.getItems().add(i);   // 字体大小范围：1px~96px

        // 设置默认值
        fontFamily.setPromptText(mainController.family);
        fontStyle.setPromptText(mainController.style.toString());
        fontSize.setPromptText(mainController.size.toString());
        fontColor.setValue(Color.BLACK);
        simpleText.setFont(Font.font(mainController.family, mainController.style, mainController.size));

        // 设置监听
        fontFamily.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            simpleText.setFont(Font.font(newValue, mainController.style, mainController.size));
            mainController.family = newValue;
        });
        fontStyle.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            simpleText.setFont(Font.font(mainController.family, newValue, mainController.size));
            mainController.style = newValue;
        });
        fontSize.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            simpleText.setFont(Font.font(mainController.family, mainController.style, newValue));
            mainController.size = newValue;
        });
        fontColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            String colorS = newValue.toString();
            int r = HexFormat.fromHexDigits(colorS, 2, 4);
            int g = HexFormat.fromHexDigits(colorS, 4, 6);
            int b = HexFormat.fromHexDigits(colorS, 6, 8);
            int a = HexFormat.fromHexDigits(colorS, 8, 10);
            simpleText.setStyle(String.format("-fx-fill:rgba(%d,%d,%d,%d)", r, g, b, a));

            mainController.color = newValue;
        });
    }
}