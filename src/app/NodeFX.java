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

    // Variáveis para controle de arraste
    private double mouseAnchorX;
    private double mouseAnchorY;
    private boolean isDragging = false; // O segredo está aqui

    // Interface para avisar o Main que foi um clique limpo
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

        enableDragAndClick();
    }

    // Método para o Main definir o que acontece no clique
    public void setOnNodeClickListener(Consumer<NodeFX> handler) {
        this.clickHandler = handler;
    }

    private void enableDragAndClick() {
        // 1. PRESSIONOU: Prepara o terreno
        setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown()) return;

            // Guarda onde pegamos a bolinha
            mouseAnchorX = event.getSceneX() - getLayoutX();
            mouseAnchorY = event.getSceneY() - getLayoutY();

            // Assume que NÃO é arraste até que se prove o contrário
            isDragging = false;

            setCursor(Cursor.MOVE);
            event.consume(); // Impede que o clique atravesse pro fundo
        });

        // 2. ARRASTOU: Move e marca a bandeira
        setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) return;

            double newX = event.getSceneX() - mouseAnchorX;
            double newY = event.getSceneY() - mouseAnchorY;

            // Pequena zona morta: só considera arraste se mover mais de 2 pixels
            // (Isso evita que tremedeira do mouse seja considerada arraste)
            if (Math.abs(newX - getLayoutX()) > 2 || Math.abs(newY - getLayoutY()) > 2) {
                isDragging = true;
            }

            setLayoutX(newX);
            setLayoutY(newY);

            event.consume();
        });

        // 3. SOLTOU: Decide se foi clique ou arraste
        setOnMouseReleased(event -> {
            setCursor(Cursor.HAND);

            if (!isDragging) {
                // Se NÃO estava arrastando, então foi um CLIQUE!
                if (clickHandler != null) {
                    clickHandler.accept(this);
                }
            }
            // Se estava arrastando, não fazemos nada (o nó já está no lugar novo)

            isDragging = false; // Reseta para a próxima
            event.consume();
        });

        // Efeito visual (Hover)
        setOnMouseEntered(event -> {
            if (!event.isPrimaryButtonDown()) setCursor(Cursor.HAND);
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