package app;

import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Vertex;

import java.util.function.Consumer;

public class NodeFX extends StackPane {

    private final Vertex vertex;
    private final Circle circle;
    private final Text textLabel;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private boolean isDragging = false;
    private boolean isDraggable = false;

    private Consumer<NodeFX> clickHandler;

    // CORES DO TEMA
    private static final Color COLOR_NORMAL = Color.web("#00d2ff"); // Azul Neon
    private static final Color COLOR_SELECTED = Color.web("#ff007f"); // Rosa Cyberpunk
    private static final Color COLOR_VISITING = Color.web("#ffd700"); // Dourado
    private static final Color COLOR_FINISHED = Color.web("#39ff14"); // Verde Neon
    private static final Color COLOR_TEXT = Color.WHITE;

    public NodeFX(Vertex vertex, double x, double y) {
        this.vertex = vertex;

        setLayoutX(x);
        setLayoutY(y);

        // 1. A BOLINHA (Com Gradiente 3D)
        this.circle = new Circle(22);
        updateColor(COLOR_NORMAL);

        // Efeito de sombra/brilho externo
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setRadius(10);
        shadow.setSpread(0.2);
        this.circle.setEffect(shadow);

        // 2. O TEXTO
        this.textLabel = new Text(vertex.getName());
        this.textLabel.setFill(COLOR_TEXT);
        this.textLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        // Sombra leve no texto para leitura
        DropShadow textShadow = new DropShadow(2, Color.BLACK);
        this.textLabel.setEffect(textShadow);

        getChildren().addAll(circle, textLabel);

        initMouseEvents();

        // Animação de entrada (Pop-up)
        setScaleX(0); setScaleY(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), this);
        st.setToX(1); st.setToY(1);
        st.play();
    }

    private void updateColor(Color baseColor) {
        // Cria um gradiente radial para parecer uma esfera
        RadialGradient gradient = new RadialGradient(
                0, 0,
                0.3, 0.3, // Ponto de luz deslocado (topo esquerda)
                0.7,      // Raio
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, baseColor.deriveColor(0, 0.5, 1.5, 1)), // Centro mais claro
                new Stop(1, baseColor.deriveColor(0, 1, 0.8, 1))    // Borda mais escura
        );
        this.circle.setFill(gradient);
        this.circle.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.5)); // Borda fina translúcida
        this.circle.setStrokeWidth(1.5);
    }

    public void setDraggable(boolean draggable) {
        this.isDraggable = draggable;
        if (draggable) {
            setCursor(Cursor.OPEN_HAND);
        } else {
            setCursor(Cursor.DEFAULT);
        }
    }

    public void setOnNodeClickListener(Consumer<NodeFX> handler) {
        this.clickHandler = handler;
    }

    private void initMouseEvents() {
        setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown()) return;
            if (isDraggable) {
                mouseAnchorX = event.getSceneX() - getLayoutX();
                mouseAnchorY = event.getSceneY() - getLayoutY();
                setCursor(Cursor.CLOSED_HAND);
                toFront(); // Traz para frente ao clicar
            }
            isDragging = false;
            event.consume();
        });

        setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) return;
            if (!isDraggable) return;

            double newX = event.getSceneX() - mouseAnchorX;
            double newY = event.getSceneY() - mouseAnchorY;

            setLayoutX(newX);
            setLayoutY(newY);

            isDragging = true;
            event.consume();
        });

        setOnMouseReleased(event -> {
            if (isDraggable) setCursor(Cursor.OPEN_HAND);
            if (!isDragging && clickHandler != null) {
                clickHandler.accept(this);
            }
            isDragging = false;
            event.consume();
        });

        // Efeito ao passar o mouse (Hover)
        setOnMouseEntered(e -> {
            if (!e.isPrimaryButtonDown()) {
                setCursor(isDraggable ? Cursor.OPEN_HAND : Cursor.HAND);
                // Leve aumento
                ScaleTransition st = new ScaleTransition(Duration.millis(100), this);
                st.setToX(1.1); st.setToY(1.1);
                st.play();
            }
        });

        setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), this);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });
    }

    public Vertex getVertex() { return vertex; }

    public void setColor(Color color) {
        // Mapeia cores simples para nossos gradientes complexos
        if (color.equals(Color.YELLOW)) updateColor(COLOR_VISITING);
        else if (color.equals(Color.LIGHTGREEN) || color.equals(Color.GREEN)) updateColor(COLOR_FINISHED);
        else if (color.equals(Color.RED)) updateColor(Color.RED); // Erro/Destino
        else if (color.equals(Color.LIGHTGRAY)) updateColor(COLOR_NORMAL);
        else updateColor(color); // Cor customizada (ex: seleção verde)
    }

    public void setSelected(boolean selected) {
        if (selected) {
            updateColor(COLOR_SELECTED);
            // Efeito de brilho pulsante poderia ser adicionado aqui
        } else {
            updateColor(COLOR_NORMAL);
        }
    }
}