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

    //private final List<Stroke> model = new ArrayList<>(); // модель — список штрихов
    private final List<Drawable> model = new ArrayList<>();
    private final UndoManager undoManager = new UndoManager(100);
    private Stroke currentStroke = null;
    private Line currentLine = null;
    private Erase currentErase = null;

    private Canvas canvas;
    private GraphicsContext gc;
    
    private enum Tool { Stroke, Line, Erase};
    private Tool currentTool = Tool.Stroke;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        redraw();
        

        // Панель управления
        Button strokeBtn = new Button("Stroke");
        Button lineBtn = new Button("Line");
        Button eraseBtn = new Button("Eraser");
        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        undoBtn.setOnAction(e -> {
           
        });
        strokeBtn.setOnAction(e -> {
            currentTool = Tool.Stroke;
        });
        lineBtn.setOnAction(e -> {
        	currentTool = Tool.Line;
        });
        eraseBtn.setOnAction(e -> {
        	currentTool = Tool.Erase;
        });
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

        HBox controls = new HBox(5, undoBtn, redoBtn, strokeBtn,lineBtn, eraseBtn);

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(canvas);

        // Mouse handlers
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
        	if (currentTool == Tool.Stroke) {
        		currentStroke = new Stroke(Color.BLACK, 2.0);
                currentStroke.addPoint(e.getX(), e.getY());
        	}
        	else if (currentTool == Tool.Line) {
        		currentLine = new Line(Color.BLACK, 2.0);
        		currentLine.addPoint(e.getX(), e.getY());
        	}
        	else if (currentTool == Tool.Erase) {
        		currentErase = new Erase();
        		currentErase.addPoint(e.getX(), e.getY());
        		//eraseAt(e.getX(), e.getY());
        	}
            
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
        	if (currentTool == Tool.Stroke) {
	            if (currentStroke != null) {
	                currentStroke.addPoint(e.getX(), e.getY());
	                redraw(); // перерисовываем всё модель + текущий черновик
	                drawStroke(currentStroke);
	            }
        	}
			else if (currentTool == Tool.Line) {
				if (currentLine != null) {
					currentLine.addPoint(e.getX(), e.getY());
					redraw();
					drawLine(currentLine);
				}
			}
			else if (currentTool == Tool.Erase) {
				if (currentErase != null) {
					currentErase.addPoint(e.getX(), e.getY());
					redraw();
					drawErase(currentErase);
				//eraseAt(e.getX(), e.getY());  		
				}
			}
        });
        

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	if (currentTool == Tool.Stroke) {
	            if (currentStroke != null) {
	                currentStroke.addPoint(e.getX(), e.getY());
	                // Создаём команду и выполняем её через UndoManager
	                DrawStrokeCommand cmd = new DrawStrokeCommand(model, currentStroke);
	                undoManager.doCommand(cmd);
	                currentStroke = null;
	                redraw();
	                updateButtons(undoBtn, redoBtn);
	            }
        	}
        	else if (currentTool == Tool.Line) {
        		if (currentLine != null) {
					currentLine.addPoint(e.getX(), e.getY());
					// Создаём команду и выполняем её через UndoManager
					DrawLineCommand cmd = new DrawLineCommand(model, currentLine);
	                undoManager.doCommand(cmd);
	                currentLine = null;
					redraw();
					updateButtons(undoBtn, redoBtn);
					//drawLine(currentLine);
				}
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
    // добавить рисовку линий
    private void drawLine(Line l) {
    	if (l == null) return;
    	List<Double> ptl = l.getPoints();
    	int size = ptl.size();
        if (ptl.size() < 4) return;
        gc.setStroke(l.getColor());
        gc.setLineWidth(l.getWidth());
        gc.beginPath();
        gc.moveTo(ptl.get(0), ptl.get(1));
//        for (int i = 2; i < ptl.size(); i += 2) {
//            gc.lineTo(ptl.get(i), ptl.get(i + 1));
//        }
        gc.lineTo(ptl.get(size-2), ptl.get(size-1));
        gc.stroke();
    }
    // рисовать всю модель из штрихов
    // сделать чтобы рисовалась модель из штрихов и прямых линий
    private void redraw() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Drawable d : model) {
            if (d instanceof Stroke) {
                Stroke stroke = (Stroke) d;
                drawStroke(stroke);
            }
            else if (d instanceof Line) {
                Line line = (Line) d;
                drawLine(line);
            }
            else if (d instanceof Erase) {
                Erase eraser = (Erase) d;
                drawErase(eraser);
            }
        }

    }
    // рисовать штрихи
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
    // стирание
    private void eraseAt(double x, double y) {
    	final double ERASER_SIZE = 10;
    	gc.clearRect(x - ERASER_SIZE / 2, y - ERASER_SIZE / 2, ERASER_SIZE, ERASER_SIZE);
    }
    private void drawErase(Erase e) {
        List<Double> pts = e.getPoints();
        if (pts.size() < 4) return;
        gc.beginPath();
        gc.moveTo(pts.get(0), pts.get(1));
        for (int i = 2; i < pts.size(); i += 2) {
            eraseAt(pts.get(i), pts.get(i + 1));
        }
        gc.stroke();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
