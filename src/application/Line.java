package application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class Line {
	private final Color color;
	private final double width;
    public Line(Color color, double width) {
		super();
		this.color = color;
		this.width = width;
	}
    public void addPoint(double x, double y) {
        points.add(x);
        points.add(y);
    }
    private final List<Double> points = new ArrayList<>();
    
    public Color getColor() {
        return color;
    }

    public double getWidth() {
        return width;
    }
    
}
