package application;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MyProgressDialogue extends Dialog {
    private final ProgressBar progressBar = new ProgressBar();
    private final Label label = new Label();


    MyProgressDialogue(Stage stage) {
        initOwner(stage);
        initStyle(StageStyle.TRANSPARENT);
        setHeight(200);
        setWidth(600);
        setHeaderText(null);
        setGraphic(null);

        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(5);
        VBox box = new VBox();
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(progressBar, label);
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize(300, 50);
        borderPane.setCenter(box);
        getDialogPane().setContent(borderPane);


    }

    public void setProgress(double x) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(x);
            }
        });
    }

    public void setLabel(String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                label.setText(msg);
                label.requestFocus();
            }
        });

    }

}
