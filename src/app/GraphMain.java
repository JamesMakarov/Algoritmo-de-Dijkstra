package app;

import algorithm.DijkstraListener;
import algorithm.DijkstraSolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Edge;
import model.Vertex;

import java.util.HashMap;
import java.util.Map;

public class GraphMain extends Application {

    // Mapas para achar o visual a partir do modelo
    private Map<Vertex, NodeFX> nodeMap = new HashMap<>();
    private Map<Edge, EdgeFX> edgeMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();

        // 1. Criar Dados (Model)
        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");
        Vertex d = new Vertex("D");
        Vertex e = new Vertex("E");

        // Definir conexões
        createConnection(a, b, 6);
        createConnection(a, d, 1);
        createConnection(d, b, 2);
        createConnection(d, e, 1);
        createConnection(b, c, 5);
        createConnection(b, e, 2);
        createConnection(e, c, 5);

        // 2. Criar Visual (View) - Posicionando manualmente por enquanto
        addNode(root, a, 100, 100);
        addNode(root, b, 300, 100);
        addNode(root, d, 100, 300);
        addNode(root, e, 300, 300);
        addNode(root, c, 500, 200);

        // Adicionar arestas na tela (precisamos percorrer os vértices)
        // Isso é meio chato manualmente, mas necessário para desenhar as linhas
        drawEdges(root, a);
        drawEdges(root, b);
        drawEdges(root, d);
        drawEdges(root, e);

        // 3. Botão para Iniciar
        Button btnRun = new Button("Rodar Dijkstra (A -> C)");
        btnRun.setLayoutX(10);
        btnRun.setLayoutY(10);

        btnRun.setOnAction(event -> {
            // Resetar cores
            nodeMap.values().forEach(n -> n.setColor(Color.LIGHTGRAY));
            edgeMap.values().forEach(line -> { line.setStroke(Color.BLACK); line.setStrokeWidth(2); });

            // Rodar em Thread separada para não travar a UI
            new Thread(() -> runAlgorithm(a, c)).start();
        });

        root.getChildren().add(btnRun);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Visualizador Dijkstra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Método auxiliar para criar conexão e guardar referência se precisar
    private void createConnection(Vertex source, Vertex target, double weight) {
        source.addEdge(target, weight);
    }

    private void addNode(Group root, Vertex v, double x, double y) {
        NodeFX nodeFX = new NodeFX(v, x, y);
        nodeMap.put(v, nodeFX);
        root.getChildren().add(nodeFX);
    }

    private void drawEdges(Group root, Vertex v) {
        NodeFX sourceFX = nodeMap.get(v);
        for (Edge edge : v.getEdges()) {
            NodeFX targetFX = nodeMap.get(edge.getTarget());
            EdgeFX edgeFX = new EdgeFX(edge, sourceFX, targetFX);
            edgeMap.put(edge, edgeFX);
            root.getChildren().add(0, edgeFX); // Adiciona no índice 0 para ficar ATRÁS das bolinhas
        }
    }

    private void runAlgorithm(Vertex start, Vertex end) {
        DijkstraSolver solver = new DijkstraSolver();

        // IMPLEMENTAÇÃO DO LISTENER VISUAL
        solver.setListener(new DijkstraListener() {
            @Override
            public void onVertexVisiting(Vertex v) {
                Platform.runLater(() -> nodeMap.get(v).setColor(Color.YELLOW));
                sleep(800); // Pausa dramática para vermos acontecendo
            }

            @Override
            public void onVertexFinalized(Vertex v) {
                Platform.runLater(() -> nodeMap.get(v).setColor(Color.LIGHTGREEN));
                sleep(400);
            }

            @Override
            public void onEdgeRelaxed(Edge e, double newDistance) {
                Platform.runLater(() -> {
                    EdgeFX fx = edgeMap.get(e);
                    if (fx != null) {
                        fx.setStroke(Color.GREEN);
                        fx.setStrokeWidth(4);
                    }
                });
                sleep(400);
            }

            @Override
            public void onEdgeRejected(Edge e, double distanceSent) {
                Platform.runLater(() -> {
                    EdgeFX fx = edgeMap.get(e);
                    if (fx != null) {
                        fx.setStroke(Color.RED);
                        fx.setStrokeWidth(1); // Fica fininho pq foi rejeitada
                    }
                });
                sleep(200);
            }
        });

        solver.findShortestPath(start, end);
    }

    // Funçãozinha pra dormir sem sujar o código com try-catch
    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}