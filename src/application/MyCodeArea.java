package application;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import static application.Constants.PATTERN;
import static application.Constants.sampleCode;

public class MyCodeArea extends CodeArea {

    private final CodeArea codeArea;
    private final ExecutorService executor;
    private ZoomableScrollPane virtualizedScrollPane;

    public MyCodeArea() {

        super();
        executor = Executors.newSingleThreadExecutor();
        codeArea = this;
        codeArea.setPadding(new Insets(8, 0, 0, 0));
        codeArea.setLineHighlighterOn(false);
        codeArea.getStylesheets().add(Main.class
                .getResource("/highlighterStyle.css").toExternalForm());
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenDone = codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.multiPlainChanges()).filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                }).subscribe(this::applyHighlighting);
        // call when no longer need it: `cleanupWhenFinished.unsubscribe();`
        File lastCode = new File(System.getProperty("user.dir") + "\\data\\lastCode.txt");
        if (lastCode.exists()) {
            try {
                FileReader fileReader = new FileReader(lastCode);
                Scanner scanner = new Scanner(fileReader);
                StringBuilder code = new StringBuilder();
                while (scanner.hasNextLine()) code.append(scanner.nextLine()).append("\n");
                codeArea.replaceText(code.toString());

                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else codeArea.replaceText(0, 0, sampleCode);


    }

    private static StyleSpans<Collection<String>> computeHighlighting(
            String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null
                    ? "keyword"
                    : matcher.group("PAREN") != null
                    ? "paren"
                    : matcher.group("BRACE") != null
                    ? "brace"
                    : matcher.group("BRACKET") != null
                    ? "bracket"
                    : matcher.group("SEMICOLON") != null
                    ? "semicolon"
                    : matcher.group(
                    "STRING") != null
                    ? "string"
                    : matcher
                    .group("COMMENT") != null
                    ? "comment"
                    : matcher
                    .group("PREC") != null
                    ? "prec"
                    : matcher
                    .group("MYCOM") != null
                    ? "mycom"
                    : null;

            /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(),
                    matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass),
                    matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private void applyHighlighting(
            StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    public void stop() {
        executor.shutdown();
    }

    public ZoomableScrollPane getPane() {
        if (virtualizedScrollPane == null) {
            StackPane codeAreaStack = new StackPane();
            Rectangle rectangle = new Rectangle();
            rectangle.widthProperty().bind(codeArea.prefWidthProperty());
            rectangle.heightProperty().bind(codeArea.prefHeightProperty());
            rectangle.xProperty().bind(codeArea.layoutXProperty());
            rectangle.yProperty().bind(codeArea.layoutYProperty());
            rectangle.setStyle("-fx-fill: " + Constants.BACK_COLOR + ";");
            codeAreaStack.getChildren().addAll(rectangle, codeArea);

            virtualizedScrollPane = new ZoomableScrollPane(codeAreaStack);
            codeArea.prefWidthProperty()
                    .bind(virtualizedScrollPane.widthProperty());
            codeArea.prefHeightProperty()
                    .bind(virtualizedScrollPane.heightProperty());
        }
        return virtualizedScrollPane;
    }

    public void SetZoomButtons(Button pos, Button neg) {
        if (virtualizedScrollPane == null)
            getPane();

        pos.setOnAction((t) -> {
            virtualizedScrollPane.onScroll(1.0,
                    new Point2D(codeArea.getLayoutX(), codeArea.getLayoutY()));
        });

        neg.setOnAction((t) -> {
            virtualizedScrollPane.onScroll(-1.0,
                    new Point2D(codeArea.getLayoutX(), codeArea.getLayoutY()));
        });

    }

}
