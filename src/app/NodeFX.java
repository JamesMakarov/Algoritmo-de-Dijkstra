package app;

import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.Vertex;

import java.util.function.Consumer;

public class NodeFX extends StackPane {

    private final Vertex vertex;
    private final Circle circle;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private boolean isDragging = false;

    // TRAVA DE SEGURANÇA: Só permite mover se o Main deixar
    private boolean isDraggable = false;

    private Consumer<NodeFX> clickHandler;

    public NodeFX(Vertex vertex, double x, double y) {
        this.vertex = vertex;

        setLayoutX(x);
        setLayoutY(y);

        this.circle = new Circle(20, Color.LIGHTGRAY);
        this.circle.setStroke(Color.BLACK);
        this.circle.setStrokeWidth(2);

        Text text = new Text(vertex.getName());
        getChildren().addAll(circle, text);

        initMouseEvents();
    }

    public void setDraggable(boolean draggable) {
        this.isDraggable = draggable;
        // Muda o cursor visualmente para o usuário saber
        if (draggable) {
            setCursor(Cursor.OPEN_HAND);
        } else {
            setCursor(Cursor.DEFAULT); // Ou HAND se for clicável
        }
    }

    public void setOnNodeClickListener(Consumer<NodeFX> handler) {
        this.clickHandler = handler;
    }

    private void initMouseEvents() {
        setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown()) return;

            // Se não puder mover, a gente nem começa a calcular posição
            if (isDraggable) {
                mouseAnchorX = event.getSceneX() - getLayoutX();
                mouseAnchorY = event.getSceneY() - getLayoutY();
                setCursor(Cursor.CLOSED_HAND);
            }

            isDragging = false;
            event.consume();
        });

        setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) return;

            // O BLOQUEIO MÁGICO: Se não for draggable, ignora o movimento
            if (!isDraggable) return;

            double newX = event.getSceneX() - mouseAnchorX;
            double newY = event.getSceneY() - mouseAnchorY;

            setLayoutX(newX);
            setLayoutY(newY);

            isDragging = true; // Marca que houve movimento
            event.consume();
        });

        setOnMouseReleased(event -> {
            if (isDraggable) setCursor(Cursor.OPEN_HAND);

            // Se NÃO houve arraste (foi só um clique parado)
            if (!isDragging) {
                if (clickHandler != null) {
                    clickHandler.accept(this);
                }
            }

            isDragging = false;
            event.consume();
        });

        // Efeito visual simples ao passar o mouse
        setOnMouseEntered(e -> {
            if (!e.isPrimaryButtonDown()) {
                setCursor(isDraggable ? Cursor.OPEN_HAND : Cursor.HAND);
            }
        });
    }

    public Vertex getVertex() { return vertex; }

    public void setColor(Color color) {
        this.circle.setFill(color);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            this.circle.setStroke(Color.BLUE);
            this.circle.setStrokeWidth(4);
        } else {
            this.circle.setStroke(Color.BLACK);
            this.circle.setStrokeWidth(2);
        }
    }
}