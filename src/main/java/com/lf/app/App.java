package com.lf.app;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 录屏应用
 *
 * @author auler
 * @date 2024/2/26
 */
public class App extends Application {

    /**
     * 应用启动
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 录屏+音频
        Recorder recorder = new Recorder();
        // 图像窗口
        ImageView imageVideo = new ImageView();
        imageVideo.setFitWidth(800);
        imageVideo.setFitHeight(600);
        // 状态图标
        Text text = new Text(" 未录制 ");
        text.setTextAlignment(TextAlignment.CENTER);
        text.setX(10);
        text.setY(10);
        // 时间
        Label timeLabal = new Label(" 录制时间：");
        Text timeText = new Text("00:00:00");
        timeText.setTextAlignment(TextAlignment.CENTER);
        GridPane gridPane = new GridPane();
        gridPane.setPrefWidth(160);
        gridPane.setPrefHeight(50);
        gridPane.add(text, 0, 0);
        gridPane.add(timeLabal, 1, 0);
        gridPane.add(timeText, 2, 0);
        gridPane.setAlignment(Pos.CENTER);
        // 开始按钮
        Button startButton = new Button("开始录制");
        startButton.setOnAction(event -> {
            System.out.println("开始录制");
            text.setText("录制中");
            text.setFill(Color.GREEN);
            recorder.setStopped(false);
            try {
                recorder.startRecorder(imageVideo, timeText);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 停止按钮
        Button stopButton = new Button("停止录制");
        stopButton.setOnAction(event -> {
            System.out.println("停止录制");
            text.setText("已停止");
            text.setFill(Color.RED);
            recorder.setStopped(true);
        });

        // 水平盒子
        HBox hBox = new HBox();
        hBox.getChildren().addAll(gridPane, startButton, stopButton);
        hBox.setSpacing(5);//子节点之间的空格
        // 垂直盒子
        VBox vbox = new VBox(hBox);
        vbox.setSpacing(5);//子节点之间的空格
        vbox.getChildren().addAll(imageVideo);//添加子模块
        // 场景
        Scene scene = new Scene(vbox);
        //设置应用大小并show
        primaryStage.setTitle("桌面录屏");//标题
        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
        //关闭请求
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("关闭请求");
        });
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
