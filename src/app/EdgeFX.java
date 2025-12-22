package app;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.Edge;

public class EdgeFX extends Group {

    private final Edge edge;
    private final Line line;
    private final Polygon arrowHead;
    private final Text weightText;
    private final Rectangle weightBg;

    // --- IMPORTANTE: Raio ajustado para coincidir com o NodeFX ---
    private static final double NODE_RADIUS = 15.0;

    public EdgeFX(Edge edge, NodeFX source, NodeFX target) {
        this.edge = edge;

        this.line = new Line();
        this.line.setStroke(Color.web("#aaaaaa"));
        this.line.setStrokeWidth(2);
        this.line.setOpacity(0.6);

        this.arrowHead = new Polygon();
        this.arrowHead.setFill(Color.web("#aaaaaa"));

        this.weightText = new Text(String.valueOf((int) edge.getWeight()));
        this.weightText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        this.weightText.setFill(Color.WHITE);

        this.weightBg = new Rectangle();
        this.weightBg.setFill(Color.web("#2b2b2b"));
        this.weightBg.setStroke(Color.web("#aaaaaa"));
        this.weightBg.setStrokeWidth(1);
        this.weightBg.setArcWidth(10);
        this.weightBg.setArcHeight(10);

        Group weightLabelGroup = new Group(weightBg, weightText);
        weightLabelGroup.setEffect(new DropShadow(5, Color.BLACK));

        getChildren().addAll(line, arrowHead, weightLabelGroup);

        // Bindings de posição
        DoubleBinding sourceX = source.layoutXProperty().add(NODE_RADIUS);
        DoubleBinding sourceY = source.layoutYProperty().add(NODE_RADIUS);
        DoubleBinding targetX = target.layoutXProperty().add(NODE_RADIUS);
        DoubleBinding targetY = target.layoutYProperty().add(NODE_RADIUS);

        sourceX.addListener((o, old, v) -> update(sourceX.get(), sourceY.get(), targetX.get(), targetY.get()));
        sourceY.addListener((o, old, v) -> update(sourceX.get(), sourceY.get(), targetX.get(), targetY.get()));
        targetX.addListener((o, old, v) -> update(sourceX.get(), sourceY.get(), targetX.get(), targetY.get()));
        targetY.addListener((o, old, v) -> update(sourceX.get(), sourceY.get(), targetX.get(), targetY.get()));

        update(sourceX.get(), sourceY.get(), targetX.get(), targetY.get());
    }

    private void update(double sx, double sy, double tx, double ty) {
        double dx = tx - sx;
        double dy = ty - sy;
        double angle = Math.atan2(dy, dx);

        line.setStartX(sx);
        line.setStartY(sy);

        double subtractRadius = NODE_RADIUS + 5; // +5 de folga
        double endLineX = tx - subtractRadius * Math.cos(angle);
        double endLineY = ty - subtractRadius * Math.sin(angle);

        line.setEndX(endLineX);
        line.setEndY(endLineY);

        drawArrowHead(endLineX, endLineY, angle);

        double midX = (sx + tx) / 2;
        double midY = (sy + ty) / 2;

        double textW = weightText.getLayoutBounds().getWidth() + 10;
        double textH = weightText.getLayoutBounds().getHeight() + 4;

        weightBg.setWidth(textW);
        weightBg.setHeight(textH);
        weightBg.setX(midX - textW / 2);
        weightBg.setY(midY - textH / 2);

        weightText.setX(midX - weightText.getLayoutBounds().getWidth() / 2);
        weightText.setY(midY + weightText.getLayoutBounds().getHeight() / 4);
    }

    private void drawArrowHead(double x, double y, double angle) {
        double arrowLength = 10; // Reduzi levemente a seta também
        double x1 = x - arrowLength * Math.cos(angle - Math.PI / 6);
        double y1 = y - arrowLength * Math.sin(angle - Math.PI / 6);
        double x2 = x - arrowLength * Math.cos(angle + Math.PI / 6);
        double y2 = y - arrowLength * Math.sin(angle + Math.PI / 6);
        arrowHead.getPoints().setAll(x, y, x1, y1, x2, y2);
    }

    public void setStroke(Color color) {
        line.setStroke(color);
        arrowHead.setFill(color);
        weightBg.setStroke(color);

        if (color.equals(Color.GREEN) || color == Color.GREEN) {
            line.setOpacity(1.0);
            line.setStroke(Color.web("#39ff14"));
            arrowHead.setFill(Color.web("#39ff14"));
            line.setEffect(new DropShadow(10, Color.web("#39ff14")));
        } else if (color.equals(Color.RED)) {
            line.setOpacity(0.3);
            line.setEffect(null);
        } else {
            line.setOpacity(0.6);
            line.setEffect(null);
        }
    }

    public void setStrokeWidth(double width) {
        line.setStrokeWidth(width);
    }
}