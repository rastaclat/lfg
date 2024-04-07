package me.bramar.task.controller;

import cn.hutool.core.io.FileUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class MainController {
    @FXML
    private TabPane tabPane;

    @FXML
    private Tab homeTab;

    @Autowired
    private HomeTableController homeTableController;

    @FXML
    private Tab taskTable;

   /* @Autowired
    private TaskTableController taskTableController;*/

    public void handleImportCvv(ActionEvent actionEvent) {
        // 文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择配置文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt")
        );
        File selectedFile = fileChooser.showOpenDialog(homeTab.getTabPane().getScene().getWindow());
        if (selectedFile != null) {
            // 使用 Hutool 读取文件内容
            String content = FileUtil.readUtf8String(selectedFile);
            homeTableController.parseAndLoadData(content);
        }
    }

   /* public void handleImportTask(ActionEvent actionEvent) {
        // 文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择配置文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt")
        );
        File selectedFile = fileChooser.showOpenDialog(taskTable.getTabPane().getScene().getWindow());
        if (selectedFile != null) {
            // 使用 Hutool 读取文件内容
            String content = FileUtil.readUtf8String(selectedFile);
            taskTableController.parseAndLoadData(content);
        }
    }*/
}
