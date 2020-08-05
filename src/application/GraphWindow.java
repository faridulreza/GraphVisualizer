package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static application.Constants.*;

public class GraphWindow extends Application {

    private final double radius = 40;
    private final MyCodeArea codeArea;
    public Group root;
    public double stageHeight, stageWidth;
    public Button button;
    double orgSceneX, orgSceneY;
    long interval = 4000;
    private boolean graphNotCreated = true;
    private HashMap<Integer, ArrayList<Integer>> levelMap;
    private HashMap<Integer, Circle> circleMap;
    private HashMap<Circle, Label> textOfcircle;
    private HashMap<Circle, Circle> backgroundCircle;
    private Line[][] lineMap;
    private volatile String msgString;
    private volatile int msgNode;
    private volatile int msgPreviousNode;
    private Thread myThread;
    private boolean threadRunning = false;
    private TextArea loggerText;
    private volatile boolean stopThread = false;
    private Alert nodeNotFoundAlert, alert;
    private Arrow arrow;
    private Circle previousCircle = null;

    GraphWindow(MyCodeArea codeArea) {
        this.codeArea = codeArea;
    }

    public void log(String s) {
        // System.out.println(s);
    }

    private void highlightLine(int line) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                codeArea.moveTo(line - 1, 0);
                codeArea.requestFollowCaret();
                codeArea.requestFocus();


            }
        });
    }

    private void toggleLineHighlighter() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                codeArea.setLineHighlighterOn(!codeArea.isLineHighlighterOn());
            }
        });
    }

    public void new_thread() {
        if (graphNotCreated) {
            loggerText.appendText("No graph Created\n");
            threadRunning = false;
            button.setStyle("-fx-background-color: green;");
            button.setText("Play");
            return;
        }
        for (java.util.Map.Entry<Integer, Circle> x : circleMap.entrySet()) {
            backgroundCircle.get(x.getValue()).setFill(Color.BLACK);
            textOfcircle.get(x.getValue()).setText("   " + x.getKey());
            x.getValue().toFront();
        }
        int ssz = lineMap[0].length;
        for (int i = 0; i < ssz; i++) {
            for (int j = 0; j < ssz; j++) {
                if (lineMap[i][j] != null) {
                    lineMap[i][j].setStroke(Color.BLACK);
                }
            }
        }

        FileInputStream fileStream;
        try {
            fileStream = new FileInputStream(
                    Constants.dir + "\\" + Constants.command_file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            loggerText.appendText("No Commands Found\n");
            threadRunning = false;
            button.setStyle("-fx-background-color: green;");
            button.setText("Play");

            return;
        }

        Scanner scanner = new Scanner(fileStream);

        myThread = new Thread(new Runnable() {

            String Command = "";

            @Override
            public void run() {
                toggleLineHighlighter();
                while (scanner.hasNext() && !stopThread) {
                    if (!threadRunning) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    Command = scanner.next();

                    if (Command.equals(UPDATE)) {
                        int line = scanner.nextInt();
                        highlightLine(line);
                        khela_dekhao(scanner, line);
                    } else if (Command.equals(LOG)) {
                        int line = scanner.nextInt();
                        highlightLine(line);
                        logger(scanner);
                    } else if (Command.equals("#ALERT#")) {
                        int line = scanner.nextInt();
                        highlightLine(line);
                        alert(scanner);
                    } else if (Command.equals("#HIGHLIGHT#")) {
                        highlightLine(scanner.nextInt());
                        highlight(scanner);

                    }


                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }

                    while (nodeNotFoundAlert.isShowing() || alert.isShowing()) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {

                        }
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (arrow != null) {
                                arrow.stop();
                                arrow = null;
                            }
                        }
                    });

                }

                scanner.close();
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        button.setText("Replay");
                        button.setStyle("-fx-background-color: purple;");
                        myThread = null;

                    }
                });
                toggleLineHighlighter();
            }
        });

        myThread.start();
    }

    private void logger(Scanner scanner) {
        String com = "";

        while (!(com = scanner.nextLine()).equals(LOG)) {
            String finalCom = com;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loggerText.appendText(finalCom + "\n");
                    loggerText.requestFocus();
                }
            });

        }

    }

    private void alert(Scanner scanner) {
        String com = "", msg = "";

        while (!(com = scanner.nextLine()).equals("#ALERT#")) {
            msg += com + "\n";
        }
        final String finalCom = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                alert.setContentText(finalCom);
                alert.show();
            }
        });

    }

    private void khela_dekhao(Scanner scanner, int line) {
        scanner.nextLine();
        int node = Integer.parseInt(scanner.nextLine());
        int previousNode = Integer.parseInt(scanner.nextLine());
        int lineCnt = Integer.parseInt(scanner.nextLine());
        String string = "   " + node;


        for (int i = 0; i < lineCnt; i++) {
            String tmp = scanner.nextLine();
            string += "\n" + tmp;
        }
        String finalErrorMsg = string;
        if (circleMap.get(node) == null) {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    nodeNotFoundAlert.setHeaderText("Current Node " + node + " not found (at line: " + line + ")");
                    nodeNotFoundAlert.setContentText("Current node: " + node + "\n" +
                            "previous node: " + previousNode + "\n" + finalErrorMsg
                    );
                    nodeNotFoundAlert.show();
                }
            });

            return;
        }

        if (previousNode != -1 && circleMap.get(previousNode) == null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    nodeNotFoundAlert.setHeaderText("Previous Node " + node + " not found (at Line: " + line + ")");
                    nodeNotFoundAlert.setContentText("Current node: " + node + "\n" +
                            "previous node: " + previousNode + "\n" + finalErrorMsg
                    );
                    nodeNotFoundAlert.show();
                }
            });

            return;
        }
        msgString = string;
        msgNode = node;
        msgPreviousNode = previousNode;

        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                textOfcircle.get(circleMap.get(msgNode)).setText(msgString);
                changeCircleColor(backgroundCircle.get(circleMap.get(msgNode)));

                if (msgPreviousNode != -1) {
                    showEdgeDirection(circleMap.get(previousNode), circleMap.get(msgNode), lineMap[msgPreviousNode][msgNode], Color.RED);
                }


            }
        });

    }

    public int create_graph() {

        FileInputStream fileStream;

        try {

            fileStream = new FileInputStream(
                    Constants.dir + "\\" + Constants.graph_file);
        } catch (Exception e) {
            return 0;
        }

        Scanner scanner = new Scanner(fileStream);

        int n = scanner.nextInt();
        int m = scanner.nextInt();
        if (n == 0) return 0;

        int[] level = new int[n];
        boolean[] degree = new boolean[n];

        ArrayList<ArrayList<Integer>> adj = new ArrayList<ArrayList<Integer>>(
                n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<Integer>());
            level[i] = -1;
        }

        for (int i = 0; i < m; i++) {
            int a = scanner.nextInt();
            int b = scanner.nextInt();
            degree[a] = true;
            degree[b] = true;
            adj.get(a).add(b);
            adj.get(b).add(a);
        }

        Queue<Integer> queue = new LinkedList<>();
        levelMap = new HashMap<>();

        for (int i = 0; i < n; i++) {
            if (level[i] != -1 || !degree[i])
                continue;

            queue.add(i);
            level[i] = 0;

            while (!queue.isEmpty()) {

                int v = queue.poll();

                if (levelMap.get(level[v]) == null) {
                    levelMap.put(level[v], new ArrayList<>());
                }
                levelMap.get(level[v]).add(v);

                for (int x : adj.get(v)) {
                    if (level[x] == -1) {
                        level[x] = level[v] + 1;
                        queue.add(x);
                    }
                }
            }
        }


        ArrayList<Integer> tmpList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!degree[i]) tmpList.add(i);
        }
        levelMap.put(levelMap.size(), tmpList);

        double rectWidth = Constants.OFFSET + radius * 2 + Constants.OFFSET;
        double rectHeight = Constants.OFFSET + radius * 2 + Constants.OFFSET;
        int maxCircle = (int) (stageWidth / rectWidth);

        double startY = 4;
        circleMap = new HashMap<>();
        textOfcircle = new HashMap<>();
        backgroundCircle = new HashMap<>();

        for (int i = 0; levelMap.get(i) != null; i++) {

            int size = levelMap.get(i).size();
            int period = Math.min(size, maxCircle);
            double offSetX = 0;

            if (size < maxCircle) {
                offSetX = stageWidth - (size * rectWidth);
                offSetX /= 2;

            }
            double startX = 2, endY = 0;

            for (int x : levelMap.get(i)) {
                double endX = startX + rectWidth;
                endY = startY + rectHeight;

                Circle tmp = createCircle(offSetX + (startX + endX) / 2,
                        (startY + endY) / 2, radius, Constants.nodeColor,
                        "   " + x);
                circleMap.put(x, tmp);
                period--;
                startX = endX;
                if (period == 0) {
                    startY = endY + 4;
                    startX = 0;
                    period = Math.min(size, maxCircle);
                }

            }
            startY = endY + 4;

        }

        lineMap = new Line[n][n];

        boolean[] vis = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (vis[i])
                continue;

            queue.add(i);
            vis[i] = true;
            while (!queue.isEmpty()) {

                int v = queue.poll();

                for (int x : adj.get(v)) {
                    if (!vis[x]) {
                        vis[x] = true;
                        queue.add(x);
                    }
                    if (lineMap[v][x] == null)
                        lineMap[v][x] = lineMap[x][v] = connect(
                                circleMap.get(x), circleMap.get(v));

                }
            }
        }

        for (java.util.Map.Entry<Integer, Circle> x : circleMap.entrySet()) {
            backgroundCircle.get(x.getValue()).toFront();
            textOfcircle.get(x.getValue()).toFront();
            x.getValue().toFront();
        }
        scanner.close();
        graphNotCreated = false;
        return 1;

    }

    private void highlight(Scanner scanner) {
        int from = scanner.nextInt();
        int to = scanner.nextInt();
        String scolor = scanner.next();
        Color color = Color.GREEN;

        if (circleMap.get(from) == null || circleMap.get(to) == null || lineMap[from][to] == null) {
            String msg = circleMap.get(from) == null ? "From node =" + from + " not found.\n" : "";
            msg += circleMap.get(to) == null ? "To node =" + to + " not found.\n" : "";
            msg += lineMap[to][from] == null ? "no edge from " + from + " to " + to + "\n" : "";
            String finalMsg = msg;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    alert.setContentText(finalMsg);
                }
            });
            return;
        }

        if (scolor.equals("red")) color = Color.RED;
        else if (scolor.equals("yellow")) color = Color.YELLOW;
        else if (scolor.equals("purple")) color = Color.PURPLE;
        else if (scolor.equals("orange")) color = Color.ORANGE;
        else if (scolor.equals("blue")) color = Color.BLUE;
        else if (scolor.equals("violet")) color = Color.VIOLET;
        showEdgeDirection(circleMap.get(from), circleMap.get(to), lineMap[from][to], color);

    }

    private void showEdgeDirection(Circle from, Circle to, Line line, Color color) {
        Point2D f = lineCircleInterSection(line, from);
        Point2D t = lineCircleInterSection(line, to);
        Point2D mid = t.midpoint(f);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (arrow != null) arrow.stop();
                arrow = new Arrow(root, f, mid, color);
            }
        });

    }

    private void changeCircleColor(Circle line) {
        if (previousCircle != null) previousCircle.setFill(Color.BLACK);
        line.setFill(Color.RED);
        previousCircle = line;
    }

    public Circle createCircle(double x, double y, double r, Color color,
                               String s) {
        Circle circle = new Circle(x, y, r, color);
        Circle transparentCircle = new Circle(x, y, r, Color.TRANSPARENT);

        Label text = new Label(s);

        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextFill(Color.WHITE);

        transparentCircle.setCursor(Cursor.HAND);
        transparentCircle.setOnMousePressed((t) -> {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();

            Circle c = (Circle) (t.getSource());
            c.toFront();
            t.consume();
        });
        transparentCircle.setOnMouseDragged((t) -> {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;

            Circle c = (Circle) (t.getSource());

            c.setCenterX(c.getCenterX() + offsetX);
            c.setCenterY(c.getCenterY() + offsetY);

            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            t.consume();
        });

        circle.centerXProperty().bind(transparentCircle.centerXProperty());
        circle.centerYProperty().bind(transparentCircle.centerYProperty());
        circle.radiusProperty().bind(transparentCircle.radiusProperty());
        transparentCircle.radiusProperty().bind((text.widthProperty().add(text.heightProperty())).divide(2.0));

        text.layoutXProperty()
                .bind(circle.centerXProperty()
                        .add(circle.radiusProperty().negate())
                        .add(circle.radiusProperty().multiply(38.0 / 100)));
        text.layoutYProperty()
                .bind(circle.centerYProperty()
                        .add(circle.radiusProperty().negate())
                        .add(circle.radiusProperty().multiply(15.0 / 100)));

        root.getChildren().add(circle);
        root.getChildren().add(text);
        root.getChildren().add(transparentCircle);
        textOfcircle.put(transparentCircle, text);
        backgroundCircle.put(transparentCircle, circle);

        return transparentCircle;
    }

    public Line connect(Circle c1, Circle c2) {
        Line line = new Line();

        line.startXProperty().bind(c1.centerXProperty());
        line.startYProperty().bind(c1.centerYProperty());

        line.endXProperty().bind(c2.centerXProperty());
        line.endYProperty().bind(c2.centerYProperty());

        line.setStrokeWidth(3);
        line.setStrokeLineCap(StrokeLineCap.ROUND);

        line.getStrokeDashArray().setAll(1.0, 4.0);
        line.toBack();
        line.setBlendMode(BlendMode.SRC_ATOP);
        root.getChildren().add(line);
        return line;

    }

    @Override
    public void start(Stage primaryStage) {
        // Stage decoration
        primaryStage.setTitle("Graph Visualizer");
        Rectangle2D rectangle2d = Screen.getPrimary().getBounds();
        root = new Group();
        nodeNotFoundAlert = new Alert(Alert.AlertType.ERROR);
        //nodeNotFoundAlert.initOwner(primaryStage);
        nodeNotFoundAlert.initStyle(StageStyle.TRANSPARENT);
        nodeNotFoundAlert.setWidth(200);
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setHeaderText("Press ok to proceed");


        // preparing tool bar
        ToolBar toolBar = new ToolBar();
        button = new Button("start");
        button.setStyle("-fx-background-color: green;");
        button.setTextFill(Color.WHITE);
        button.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                if (myThread != null) {
                    if (threadRunning) {

                        threadRunning = false;
                        button.setStyle("-fx-background-color: green;");
                        button.setText("Play");

                    } else {

                        threadRunning = true;
                        button.setStyle("-fx-background-color: red;");
                        button.setText("Pause");
                    }

                } else {
                    threadRunning = true;
                    new_thread();
                    button.setStyle("-fx-background-color: red;");
                    button.setText("Pause");

                }
            }
        });

        Slider slider = new Slider(500, 20000, interval);
        Text sliderVal = new Text(interval / 1000.0 + "s");
        slider.setMajorTickUnit(100);
        slider.setOnMouseDragged((t) -> {
            Slider slr = (Slider) t.getSource();
            double x = slr.getValue();
            interval = (long) x;
            x = x / 1000;
            sliderVal.setText(String.valueOf(x).substring(0, 3) + "s");

        });
        slider.setOnMouseClicked((t) -> {
            Slider slr = (Slider) t.getSource();
            double x = slr.getValue();
            interval = (long) x;
            x = x / 1000;
            sliderVal.setText(String.valueOf(x).substring(0, 3) + "s");
        });
        Text text = new Text("Interval  ");
        Button loggerButton = new Button("open log");
        loggerButton.setVisible(false);
        HBox toolbarContainer = new HBox();
        toolbarContainer.setSpacing(14.0);
        toolbarContainer.setAlignment(Pos.CENTER);
        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        toolbarContainer.getChildren().addAll(button, separator, text, slider,
                sliderVal, loggerButton);

        BorderPane toolbarStackpane = new BorderPane();
        toolbarStackpane.setCenter(toolbarContainer);
        toolBar.getItems().add(toolbarStackpane);

        // preparing main layout
        VBox vBox = new VBox();
        vBox.getChildren().add(toolBar);

        ZoomableScrollPane scrollPane = new ZoomableScrollPane(root);

        scrollPane.setPadding(new Insets(10));
        vBox.getChildren().add(scrollPane);

        Scene scene = new Scene(vBox);
        toolbarStackpane.prefWidthProperty()
                .bind(scene.widthProperty().add(-20));
        primaryStage.setHeight(rectangle2d.getHeight() - 40);
        primaryStage.setWidth(rectangle2d.getWidth() / 2);
        primaryStage.setX(rectangle2d.getMaxX() / 2);
        primaryStage.setY(2);
        stageHeight = primaryStage.getHeight();
        stageWidth = primaryStage.getWidth() - 20;

        create_graph();
        primaryStage.setScene(scene);

        // preparing logger window
        loggerText = new TextArea();
        loggerText.setMaxWidth(300);
        loggerText.setEditable(false);
        Stage loggerStage = new Stage();
        loggerStage.setTitle("Log");
        StackPane loggerHolder = new StackPane(loggerText);
        loggerStage.setScene(new Scene(loggerHolder));
        loggerStage.initStyle(StageStyle.UTILITY);

        primaryStage.setOnCloseRequest((t) -> loggerStage.close());
        primaryStage.setOnHiding((t) -> {
            loggerStage.hide();
            stopThread = true;
        });
        primaryStage.setOnShowing((t) -> loggerStage.show());
        loggerStage.setAlwaysOnTop(true);
        loggerStage.setX(primaryStage.getX());
        loggerStage.setY(primaryStage.getHeight() - 290);

        primaryStage.show();
        loggerStage.setOnHidden((t) -> {
            if (primaryStage.isShowing()) {
                loggerButton.setVisible(true);
            }
        });

        loggerButton.setOnAction((t) -> {
            loggerStage.show();
            ((Button) t.getSource()).setVisible(false);
        });

    }

    @Override
    public void stop() throws Exception {
        super.stop();

    }
}