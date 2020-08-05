package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.util.Optional;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static application.Constants.*;

public class Main extends Application {

    private final TextArea alertTextArea = new TextArea();
    private final Stage graphStage = new Stage();
    private MyCodeArea codeArea;
    private Button runButton;
    private CodeArea inputCodeArea, outputCodeArea;
    private MyProgressDialogue progressDialogue;
    private Alert alert, codeRunTakingLong;
    private boolean codeShouldRun;
    private TimerTask codeRunningTask;
    private ButtonType killButtonType, proceedButtonType;
    private ButtonType ldError;

    public static void main(String[] args) {
        File dir = new File(Constants.dir);
        File d = new File(System.getProperty("user.dir") + "\\data");
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!d.exists()) {
            d.mkdir();
            System.out.println(d.getPath());
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        VBox vBox = new VBox();
        Separator separator;
        codeArea = new MyCodeArea();
        vBox.getChildren().add(codeArea.getPane());

        separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        vBox.getChildren().add(separator);

        BorderPane ButtonBoxCover = new BorderPane();
        ButtonBoxCover.setPadding(new Insets(10, 180, 10, 180));
        runButton = new Button("Run");
        Button zoomIn = new Button("+");
        Button zommOut = new Button("-");
        codeArea.SetZoomButtons(zoomIn, zommOut);
        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(zoomIn, runButton, zommOut);
        buttonBox.setSpacing(5);
        ButtonBoxCover.setLeft(new Text("INPUT"));
        buttonBox.setMaxWidth(150);
        ButtonBoxCover.setCenter(buttonBox);
        ButtonBoxCover.setRight(new Text("OUTPUT"));
        vBox.getChildren().add(ButtonBoxCover);

        separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        vBox.getChildren().add(separator);

        BorderPane inputOutputCover = new BorderPane();
        inputOutputCover.setPadding(new Insets(2, 20, 5, 0));
        inputCodeArea = new CodeArea();
        inputCodeArea.setParagraphGraphicFactory(
                LineNumberFactory.get(inputCodeArea));
        outputCodeArea = new CodeArea();
        outputCodeArea.setParagraphGraphicFactory(
                LineNumberFactory.get(outputCodeArea));
        inputOutputCover.setLeft(inputCodeArea);
        inputOutputCover.setRight(outputCodeArea);
        vBox.getChildren().add(inputOutputCover);

        Scene scene = new Scene(vBox);
        ButtonBoxCover.prefWidthProperty().bind(scene.widthProperty());
        inputOutputCover.prefWidthProperty().bind(scene.widthProperty());
        codeArea.getPane().prefHeightProperty()
                .bind(scene.heightProperty().multiply(3.5 / 5));
        inputCodeArea.prefWidthProperty()
                .bind(scene.widthProperty().divide(2).add(-28));
        inputCodeArea.prefHeightProperty()
                .bind(scene.heightProperty().multiply(1 / 5.0));
        outputCodeArea.prefWidthProperty()
                .bind(scene.widthProperty().divide(2).add(-8));
        outputCodeArea.prefHeightProperty()
                .bind(scene.heightProperty().multiply(1 / 5.0));
        File lastInput = new File(System.getProperty("user.dir") + "\\data\\lastInput.txt");
        if (lastInput.exists()) {
            try {
                Scanner scanner = new Scanner(lastInput);
                StringBuilder inp = new StringBuilder();
                while (scanner.hasNextLine()) {
                    inp.append(scanner.nextLine()).append("\n");
                }
                scanner.close();
                inputCodeArea.replaceText(inp.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        primaryStage.setScene(scene);
        primaryStage.setTitle("Debugger");
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        primaryStage.setWidth(bounds.getWidth() / 2);
        primaryStage.setHeight(bounds.getHeight() - 30);
        primaryStage.setX(3);
        primaryStage.setY(3);

        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Compilation Error");
        alert.setHeaderText(" You can ignore the last parameter \"l\" from a graphVisualizer\n function. If a line number is < 0 you" +
                " are using it wrong, see\n documentation.");
        alert.initOwner(primaryStage);
        alert.initStyle(StageStyle.UTILITY);
        ldError = new ButtonType("Fix ld return 1 error");
        alert.getButtonTypes().add(ldError);
        alert.getDialogPane().setContent(new StackPane(alertTextArea));
        alertTextArea.setEditable(false);

        runButton.setOnAction(t -> {
            progressDialogue = new MyProgressDialogue(primaryStage);
            progressDialogue.setProgress(-1F);
            progressDialogue.show();
            runButton.setDisable(true);
            String code = codeArea.getText();
            String inpText = inputCodeArea.getText();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handleRun(code, inpText);
                }
            }).start();

        });
        primaryStage.getIcons().add(new Image(this.getClass().getResource("/icon.jpg").toString()));
        graphStage.getIcons().add(primaryStage.getIcons().get(0));
        primaryStage.show();
        codeRunTakingLong = new Alert(Alert.AlertType.CONFIRMATION);
        codeRunTakingLong.initStyle(StageStyle.TRANSPARENT);
        codeRunTakingLong.setHeaderText("Why the fuck is it taking so long?");
        killButtonType = new ButtonType("Kill that\n   shit");
        proceedButtonType = new ButtonType("Its a complicated program\n       you dumb");
        codeRunTakingLong.setContentText("Program is still running- \n\n" + "1. Make Sure proper input is given. If not kill this shit and run with proper" +
                " input\n\n" + "2. May be Runtime exception?\n\n"+"3. Infinite loop..");

        codeRunTakingLong.getButtonTypes().setAll(proceedButtonType, killButtonType);

    }


    private void handleRun(String code, String inpText) {
        progressDialogue.setProgress(.1);
        progressDialogue.setLabel("cleaning Directory...");
        File dir = new File(Constants.dir);
        if (dir.exists()) {
            for (String childs : dir.list()) {
                File tmpFile = new File(dir, childs);
                tmpFile.delete();
            }
        }

        progressDialogue.setProgress(.2);
        progressDialogue.setLabel("Creating files...");

        File inpFile = new File(dir + "\\" + Constants.inp_file);
        File graphFile = new File(dir + "\\" + Constants.graph_file);
        File commandFile = new File(dir + "\\" + Constants.command_file);
        File finalCodeFile = new File(dir + "\\" + "finalCode.cpp");
        File compilerOutputFile = new File(dir + "\\" + Constants.compiler_output_file);
        File outputFile = new File(dir + "\\" + output_file);


        try {
            inpFile.createNewFile();
            finalCodeFile.createNewFile();
            compilerOutputFile.createNewFile();
            outputFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        progressDialogue.setProgress(.3);
        progressDialogue.setLabel("Processing final Code...");
        code = modifyCode(code);
        try {

            FileWriter fileWriter = new FileWriter(inpFile);
            fileWriter.write(inpText);
            fileWriter.close();
            fileWriter = new FileWriter(finalCodeFile);
            fileWriter.write(Constants.cppCode + "\n" + code);
            fileWriter.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c",
                "g++ -w -std=c++14 finalCode.cpp")
                .directory(new File(dir + "\\"));
        processBuilder.redirectError(compilerOutputFile);
        try {
            progressDialogue.setProgress(.4);
            progressDialogue.setLabel("Compiling Code...");

            Process process = processBuilder.start();
            while (process.isAlive()) {
                Thread.sleep(100);
            }

            BufferedReader reader = new BufferedReader(new FileReader(compilerOutputFile));

            String line = null, msg = "";
            while ((line = reader.readLine()) != null) {
                msg += line + "\n";
            }
            reader.close();
            if (!msg.equals("")) {

                String finalMsg = modifyCompilerError(msg);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressDialogue.setResult("s");
                        runButton.setDisable(false);
                        alertTextArea.setText(finalMsg);
                        Optional<ButtonType> optionalButtonType = alert.showAndWait();
                        if (optionalButtonType.get().equals(ldError)) {
                            try {

                                new File(dir + "//" + "a.exe").renameTo(new File(dir + "//" + "b.exe"));
                                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "taskkill /IM \"a.exe\" /F ");
                                builder.redirectOutput();
                                builder.redirectError();
                                builder.start();
                                runButton.fire();
                            } catch (Exception e) {

                            }
                        }

                    }
                });

                return;
            }


            progressDialogue.setProgress(.6);
            progressDialogue.setLabel("Running Code...");

            processBuilder = new ProcessBuilder("cmd.exe", "/c", "a.exe")
                    .directory(new File(dir + "\\"));
            processBuilder.redirectOutput(outputFile).redirectInput(inpFile);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (codeRunTakingLong.isShowing()) return;
                            Optional<ButtonType> buttonType = codeRunTakingLong.showAndWait();
                            System.out.println(buttonType.toString());
                            if (buttonType.get().equals(killButtonType)) codeShouldRun = false;

                        }
                    });
                }
            }, 3000, 5000);
            codeShouldRun = true;
            process = processBuilder.start();
            while (process.isAlive()) {
                Thread.sleep(300);
                if (!codeShouldRun) {
                    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "taskkill /IM \"a.exe\" /F ");
                    builder.redirectOutput();
                    builder.redirectError();
                    builder.start();
                }
            }
            timer.cancel();

            if (!codeShouldRun) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressDialogue.setResult("s");
                        runButton.setDisable(false);
                    }
                });
                System.out.println("here");
                return;
            } else if (codeRunTakingLong.isShowing()) codeRunTakingLong.setResult(proceedButtonType);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        progressDialogue.setProgress(.75);
        progressDialogue.setLabel("setting output...");


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                outputCodeArea.clear();
                Scanner reader = null;
                try {
                    reader = new Scanner(new FileInputStream(dir + "\\" + output_file));
                    while (reader.hasNext()) outputCodeArea.appendText(reader.nextLine() + "\n");
                    reader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });


        progressDialogue.setProgress(.8);
        progressDialogue.setLabel("Launching GraphVisualizer...");


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (graphStage.isShowing()) graphStage.hide();

                if (graphFile.exists() || commandFile.exists()) {
                    System.out.println("here");
                    GraphWindow graphWindow = new GraphWindow(codeArea);
                    graphWindow.start(graphStage);

                }

                progressDialogue.setResult("s");
                runButton.setDisable(false);
            }
        });

    }

    @Override
    public void stop() {
        File lastCode = new File(System.getProperty("user.dir") + "\\data\\lastCode.txt");
        File lastInput = new File(System.getProperty("user.dir") + "\\data\\lastInput.txt");

        try {
            if (lastCode.exists()) lastCode.delete();
            if (lastInput.exists()) lastInput.delete();

            lastCode.createNewFile();
            lastInput.createNewFile();
            String code = codeArea.getText();
            String inp = inputCodeArea.getText();

            FileWriter fileWriter = new FileWriter(lastCode);
            fileWriter.write(code);
            fileWriter.close();
            fileWriter = new FileWriter(lastInput);
            fileWriter.write(inp);
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        codeArea.stop();

    }

}