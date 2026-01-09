package application;

import java.util.List;

public class DrawStrokeCommand implements Command {
    private final List<Stroke> model;
    private final Stroke stroke;

    public DrawStrokeCommand(List<Stroke> model, Stroke stroke) {
        // ВАЖНО: клонировать данные штриха, чтобы последующие изменения не ломали историю
        this.model = model;
        this.stroke = stroke.copy();
    }

    @Override
    public void execute() {
        model.add(stroke);
    }

    @Override
    public void undo() {
        model.remove(stroke);
    }
}
