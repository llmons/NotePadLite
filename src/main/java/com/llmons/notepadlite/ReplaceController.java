package com.llmons.notepadlite;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.llmons.notepadlite.MainController.mainController;

public class ReplaceController {
    List<Pair<Integer, Integer>> indexes = new ArrayList<>();   // 存储查找字的两端索引
    private int idx;    // 当前选中的查找字
    private boolean found;  // 标记是否已经点击查找
    @FXML
    private TextField findText;
    @FXML
    private TextField replaceText;

    public void initialize() {
        findText.textProperty().addListener(observable -> found = false);
    }

    @FXML
    public void FindButtonEvent() {
        idx = 0;
        indexes.clear();

        String toBeFind = findText.getText();
        if (toBeFind == null || toBeFind.isEmpty()) return;

        int fromIdx = 0;
        String text = mainController.textArea.getText();

        while (true) {
            int start = text.indexOf(toBeFind, fromIdx);
            if (start == -1) break;
            int end = start + toBeFind.length();
            fromIdx = end;
            indexes.add(new Pair<>(start, end));
        }

        if (indexes == null || indexes.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("找不到" + '\"' + toBeFind + '\"');
            alert.showAndWait();
            return;
        }

        mainController.textArea.selectRange(indexes.getFirst().getKey(), indexes.getFirst().getValue());
        found = true;
    }

    @FXML
    public void LastFindEvent() {
        if (!found) {
            FindButtonEvent();
            return;
        }
        --idx;
        if (idx < 0) idx = indexes.size() - 1;
        mainController.textArea.selectRange(indexes.get(idx).getKey(), indexes.get(idx).getValue());
    }

    @FXML
    public void NextFindNext() {
        if (!found) {
            FindButtonEvent();
            return;
        }
        ++idx;
        if (idx >= indexes.size()) idx = 0;
        mainController.textArea.selectRange(indexes.get(idx).getKey(), indexes.get(idx).getValue());
    }

    @FXML
    public void ReplaceEvent() {
        if (!found) {
            FindButtonEvent();
            return;
        }
        if (findText.getText() == null || findText.getText().isEmpty()) return;
        if (indexes == null || indexes.isEmpty()) return;

        String toReplace = replaceText.getText();
        mainController.textArea.replaceText(indexes.get(idx).getKey(), indexes.get(idx).getValue(), toReplace);
        indexes.remove(idx);
        if (idx >= indexes.size()) {
            idx = 0;
            if (indexes.isEmpty()) {
                found = false;
                return;
            }
        }
        mainController.textArea.selectRange(indexes.get(idx).getKey(), indexes.get(idx).getValue());
    }

    @FXML
    public void ReplaceAllEvent() {
        if (!found) FindButtonEvent();
        if (findText.getText() == null || findText.getText().isEmpty()) return;
        if (indexes == null || indexes.isEmpty()) return;

        String toReplace = replaceText.getText();
        for (Pair<Integer, Integer> index : indexes) {
            mainController.textArea.replaceText(index.getKey(), index.getValue(), toReplace);
        }
        indexes.clear();
        found = false;
    }
}
