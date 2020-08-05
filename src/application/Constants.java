package application;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

public class Constants {

    public static final String inp_file = "inp_file.txt",
            graph_file = "graph_file.txt", command_file = "command_file.txt",
            output_file = "output_file.txt",
            compiler_output_file = "compiler_output.txt",
            dir = System.getProperty("user.dir") + "\\graphVisualizer";
    public static final String CREATE_GRAPH = "#CREATE_GRAPH#";
    public static final Color nodeColor = Color.BLACK;
    public static final Color activeNodeColor = Color.RED;
    public static final String UPDATE = "#UPDATE#";
    public static final double OFFSET = 25;
    public static final String LOG = "#LOGGER#";
    public static final String BACK_COLOR = "#e1e4eb";
    private static final String[] SEARCHNREPLACE = new String[]{
            "update_node\\s*\\(",
            "logger\\s*\\(", "alert\\s*\\(", "show_edge\\s*\\("
    };
    private static final String[] KEYWORDS = new String[]{"abstract", "assert",
            "bool", "break", "struct", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum",
            "and", "final", "const", "float", "for", "goto", "if", "or", "xor",
            "short", "int", "register", "long", "inline", "new", "package",
            "private", "protected", "public", "return", "sizeof", "static",
            "signed", "unsigned", "switch", "union", "this", "throw",
            "template", "try", "void", "volatile", "while", "virtual", "not",
            "enum", "delete", "auto", "extern", "namespace"};
    private static final String[] PREPROCESSOR = new String[]{"include",
            "\\#if", "ifdef", "define", "\\#elif", "endif", "undef", "error",
            "pragma", "line", "\\#else", "typedef", "using", "\\d"};
    private static final String[] MYCOMMANDS = new String[]{"createGraph",
            "add_edge", "logger", "build_graph", "update_node", "alert", "show_edge"};
    private static final String KEYWORD_PATTERN = "\\b("
            + String.join("|", KEYWORDS) + ")\\b";
    private static final String SEARCHNREPLACE_PATTERN = String.join("|", SEARCHNREPLACE);
    public static final Pattern SEARCH_PATTERN = Pattern.compile(SEARCHNREPLACE_PATTERN);
    private static final String MYCOMMANDS_PATTERN = "\\b("
            + String.join("|", MYCOMMANDS) + ")\\b";
    private static final String PREC_PATTERN = "("
            + String.join("|", PREPROCESSOR) + ")";
    private static final String PAREN_PATTERN = "\\(|\\)|\\%[^\\s]*";
    private static final String BRACE_PATTERN = "\\{|\\}|\\<|\\>";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|"
            + "/\\*(.|\\R)*?\\*/";
    public static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>"
            + KEYWORD_PATTERN + ")" + "|(?<PREC>" + PREC_PATTERN + ")"
            + "|(?<MYCOM>" + MYCOMMANDS_PATTERN + ")" + "|(?<PAREN>"
            + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>"
            + SEMICOLON_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")");
    public static String sampleCode = "";
    public static String cppCodeHeader = "#include<bits/stdc++.h>\n"
            + "using namespace std;\n" + "string dir=\"";
    public static String cppCode = "";
    public static int cppCodeLineCount = 5;
    public static ArrayList<Integer> endIndex = new ArrayList<>();

    static {
        String dirString = "";
        for (char x : dir.toCharArray()) {
            if (x == '\\')
                dirString += "\\\\";
            else
                dirString += x;
        }
        dirString += "\\\\\"";
        cppCodeHeader += dirString + ";\n";
        cppCode += cppCodeHeader;

        InputStream inputStream = Main.class
                .getResourceAsStream("/cppCodeToAdd.txt");
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            cppCode += scanner.nextLine() + "\n";
            cppCodeLineCount++;
        }
        scanner.close();

        inputStream = Main.class.getResourceAsStream("/sampleCppCodeToShow.txt");
        scanner = new Scanner(inputStream);

        while (scanner.hasNext())
            sampleCode += scanner.nextLine() + "\n";
        scanner.close();

    }

    public static String modifyCode(String s) {

        endIndex.clear();

        Matcher matcher = SEARCH_PATTERN.matcher(s);
        while (matcher.find()) {
            endIndex.add(matcher.end() - 1);
        }


        StringBuilder builder = new StringBuilder();
        int indexOfendIndex = 0;
        int sz = s.length();
        int line = 1;
        for (int i = 0; i < sz; i++) {

            builder.append(s.charAt(i));
            if (s.charAt(i) == '\n') line++;
            if (indexOfendIndex < endIndex.size() && i == endIndex.get(indexOfendIndex)) {
                int paren = 1;
                i++;
                while (paren != 0 && i < sz) {
                    if (s.charAt(i) == '\n') line++;

                    if (s.charAt(i) == '(') paren++;
                    else if (s.charAt(i) == ')') paren--;

                    if (paren == 0) {
                        builder.append("," + line + ")");
                    } else builder.append(s.charAt(i));
                    i++;
                }
                i--;
                indexOfendIndex++;
            }
        }

        return builder.toString();
    }

    public static Point2D lineCircleInterSection(Line line, Circle circle) {
        double EPS = 1e-6;
        double endx = line.getEndX() - circle.getCenterX();
        double endy = line.getEndY() - circle.getCenterY();
        double startx = line.getStartX() - circle.getCenterX();
        double starty = line.getStartY() - circle.getCenterY();
        double m = (starty - endy) / (startx - endx);

        double r = circle.getRadius(), a = 0, b = 0, c = 0; // given as input
        if (abs(startx - endx) <= EPS) {
            a = 1;
            b = 0;
            c = -startx;
        } else {
            a = -m;
            b = 1;
            c = 0;
        }

        double x0 = -a * c / (a * a + b * b), y0 = -b * c / (a * a + b * b);
        Point2D rt = null;

        double d = r * r - c * c / (a * a + b * b);
        double mult = Math.sqrt(d / (a * a + b * b));
        double ax, ay, bx, by;
        ax = x0 + b * mult;
        bx = x0 - b * mult;
        ay = y0 - a * mult;
        by = y0 + a * mult;
        Point2D pa = new Point2D(ax + circle.getCenterX(), ay + circle.getCenterY());
        Point2D pb = new Point2D(bx + circle.getCenterX(), by + circle.getCenterY());

        if (line.getBoundsInLocal().contains(pa)) rt = pa;
        else rt = pb;


        return rt;
    }

    public static String modifyCompilerError(String error) {
        Pattern pattern = Pattern.compile("finalCode.cpp:\\d*:");
        Matcher matcher = pattern.matcher(error);
        ArrayList<Integer> startIndex = new ArrayList<>();

        while (matcher.find()) {
            startIndex.add(matcher.start());
        }

        int sz = error.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0, index = 0; i < sz; i++) {
            if (index < startIndex.size() && startIndex.get(index) == i) {
                i += 14;
                builder.append("finalCode.cpp:");
                if (error.charAt(i + 1) == ':') {
                    int tm = Integer.parseInt(error.substring(i, i + 1)) - cppCodeLineCount;
                    builder.append(tm).append(":");
                    i += 1;
                } else if (error.charAt(i + 2) == ':') {
                    int tm = Integer.parseInt(error.substring(i, i + 2)) - cppCodeLineCount;
                    builder.append(tm).append(":");
                    i += 2;
                } else if (error.charAt(i + 3) == ':') {
                    int tm = Integer.parseInt(error.substring(i, i + 3)) - cppCodeLineCount;
                    builder.append(tm).append(":");
                    i += 3;
                } else if (error.charAt(i + 4) == ':') {
                    int tm = Integer.parseInt(error.substring(i, i + 4)) - cppCodeLineCount;
                    builder.append(tm).append(":");
                    i += 4;
                }


            } else builder.append(error.charAt(i));

        }


        return builder.toString();
    }


}
