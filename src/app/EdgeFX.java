package app;

import javafx.scene.shape.Line;
import model.Edge;

public class EdgeFX extends Line {

    private final Edge edge;

    public EdgeFX(Edge edge, NodeFX source, NodeFX target) {
        this.edge = edge;

        // Amarra a ponta da linha no centro das bolinhas
        startXProperty().bind(source.layoutXProperty().add(source.translateXProperty()).add(20)); // +20 é o raio
        startYProperty().bind(source.layoutYProperty().add(source.translateYProperty()).add(20));

        endXProperty().bind(target.layoutXProperty().add(target.translateXProperty()).add(20));
        endYProperty().bind(target.layoutYProperty().add(target.translateYProperty()).add(20));

        setStrokeWidth(2);
        setOpacity(0.3); // Começa clarinha
    }

    public Edge getEdge() { return edge; }
}