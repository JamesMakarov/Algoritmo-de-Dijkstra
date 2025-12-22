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
    private final Rectangle weightBg; // Fundo do texto (Pill shape)

    private static final double NODE_RADIUS = 22.0;

    public EdgeFX(Edge edge, NodeFX source, NodeFX target) {
        this.edge = edge;

        // 1. A LINHA
        this.line = new Line();
        this.line.setStroke(Color.web("#aaaaaa")); // Cinza claro
        this.line.setStrokeWidth(2);
        this.line.setOpacity(0.6); // Levemente transparente para não poluir

        // 2. A SETA
        this.arrowHead = new Polygon();
        this.arrowHead.setFill(Color.web("#aaaaaa"));

        // 3. O TEXTO COM FUNDO
        this.weightText = new Text(String.valueOf((int) edge.getWeight()));
        this.weightText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        this.weightText.setFill(Color.WHITE);

        this.weightBg = new Rectangle();
        this.weightBg.setFill(Color.web("#2b2b2b")); // Fundo escuro igual ao app
        this.weightBg.setStroke(Color.web("#aaaaaa"));
        this.weightBg.setStrokeWidth(1);
        this.weightBg.setArcWidth(10);
        this.weightBg.setArcHeight(10);

        // Agrupar texto e fundo
        Group weightLabelGroup = new Group(weightBg, weightText);
        // Sombra para destacar o peso das linhas
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

        // Linha
        line.setStartX(sx);
        line.setStartY(sy);

        double subtractRadius = NODE_RADIUS + 5; // +5 para não colar na bolinha
        double endLineX = tx - subtractRadius * Math.cos(angle);
        double endLineY = ty - subtractRadius * Math.sin(angle);

        line.setEndX(endLineX);
        line.setEndY(endLineY);

        // Seta
        drawArrowHead(endLineX, endLineY, angle);

        // Texto (Posicionado no meio)
        double midX = (sx + tx) / 2;
        double midY = (sy + ty) / 2;

        // Ajusta tamanho do fundo baseado no texto
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
        double arrowLength = 12;
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
            line.setStroke(Color.web("#39ff14")); // Verde Neon
            arrowHead.setFill(Color.web("#39ff14"));
            line.setEffect(new DropShadow(10, Color.web("#39ff14"))); // Brilho Neon
        } else if (color.equals(Color.RED)) {
            line.setOpacity(0.3); // Fica apagadinho
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