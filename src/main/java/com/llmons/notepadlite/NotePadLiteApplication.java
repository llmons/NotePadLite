package com.llmons.notepadlite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static com.llmons.notepadlite.MainController.mainController;

public class NotePadLiteApplication extends Application {
    public static Stage primaryStage;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NotePadLiteApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setScene(scene);
        stage.setTitle("无标题");
        primaryStage = stage;

        // 关闭窗口监听
        NotePadLiteApplication.primaryStage.setOnCloseRequest(windowEvent -> {
            if (mainController.savedP.get()) return;

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
                if (mainController.filePath == null) mainController.saveAsEvent();  //如果没路径，则应调用‘另存为’
                else mainController.saveEvent();   //否则直接保存
            } else if (result.isPresent() && result.get() == cancel) windowEvent.consume();  // 点击取消，停留在当前页面

        });
        
        stage.show();
    }
}