package application;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private final List<Stroke> model = new ArrayList<>(); // модель — список штрихов
    private final UndoManager undoManager = new UndoManager(100);
    private Stroke currentStroke = null;

    private Canvas canvas;
    private GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        redraw();

        // Панель управления
        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        undoBtn.setOnAction(e -> {
            undoManager.undo();
            redraw();
            updateButtons(undoBtn, redoBtn);
        });
        redoBtn.setOnAction(e -> {
            undoManager.redo();
            redraw();
            updateButtons(undoBtn, redoBtn);
        });
        updateButtons(undoBtn, redoBtn);

        HBox controls = new HBox(5, undoBtn, redoBtn);

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(canvas);

        // Mouse handlers
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            currentStroke = new Stroke(Color.BLACK, 2.0);
            currentStroke.addPoint(e.getX(), e.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (currentStroke != null) {
                currentStroke.addPoint(e.getX(), e.getY());
                redraw(); // перерисовываем всё модель + текущий черновик
                drawStroke(currentStroke);
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (currentStroke != null) {
                currentStroke.addPoint(e.getX(), e.getY());
                // Создаём команду и выполняем её через UndoManager
                DrawStrokeCommand cmd = new DrawStrokeCommand(model, currentStroke);
                undoManager.doCommand(cmd);
                currentStroke = null;
                redraw();
                updateButtons(undoBtn, redoBtn);
            }
        });

        Scene scene = new Scene(root);
        // Горячие клавиши Ctrl+Z / Ctrl+Y
        scene.setOnKeyPressed(ev -> {
            if (ev.isControlDown() && ev.getCode() == KeyCode.Z) {
                undoManager.undo();
                redraw();
                updateButtons(undoBtn, redoBtn);
            } else if (ev.isControlDown() && (ev.getCode() == KeyCode.Y || ev.getCode() == KeyCode.Z && ev.isShiftDown())) {
                // Ctrl+Y или Ctrl+Shift+Z
                undoManager.redo();
                redraw();
                updateButtons(undoBtn, redoBtn);
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple Draw with Undo/Redo");
        primaryStage.show();
    }

    private void updateButtons(Button undoBtn, Button redoBtn) {
        undoBtn.setDisable(!undoManager.canUndo());
        redoBtn.setDisable(!undoManager.canRedo());
    }

    private void redraw() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Stroke s : model) {
            drawStroke(s);
        }
    }

    private void drawStroke(Stroke s) {
        List<Double> pts = s.getPoints();
        if (pts.size() < 4) return;
        gc.setStroke(s.getColor());
        gc.setLineWidth(s.getWidth());
        gc.beginPath();
        gc.moveTo(pts.get(0), pts.get(1));
        for (int i = 2; i < pts.size(); i += 2) {
            gc.lineTo(pts.get(i), pts.get(i + 1));
        }
        gc.stroke();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
