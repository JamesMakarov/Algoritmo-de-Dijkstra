package app;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.Vertex;

public class NodeFX extends StackPane {

    private final Vertex vertex;
    private final Circle circle;

    public NodeFX(Vertex vertex, double x, double y) {
        this.vertex = vertex;

        // Posição na tela
        setLayoutX(x);
        setLayoutY(y);

        // Desenho da bola
        this.circle = new Circle(20, Color.LIGHTGRAY);
        this.circle.setStroke(Color.BLACK);

        // Texto (Nome do vértice)
        Text text = new Text(vertex.getName());

        getChildren().addAll(circle, text);
    }

    public Vertex getVertex() { return vertex; }

    // Métodos para mudar a cor dinamicamente
    public void setColor(Color color) {
        this.circle.setFill(color);
    }
}