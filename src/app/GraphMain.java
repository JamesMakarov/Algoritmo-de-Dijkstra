package app;

import algorithm.DijkstraListener;
import algorithm.DijkstraSolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Edge;
import model.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GraphMain extends Application {

    private Pane graphPane;
    private ToggleGroup modeGroup;
    private Label statusLabel;

    // Adicionei o modo MOVE
    private enum Mode { MOVE, ADD_NODE, ADD_EDGE, SELECT_SOURCE, SELECT_TARGET, NONE }
    private Mode currentMode = Mode.NONE;

    private Vertex selectedSourceForEdge = null;
    private Vertex startNode = null;
    private Vertex endNode = null;
    private int nodeCounter = 1;

    private Map<Vertex, NodeFX> nodeMap = new HashMap<>();
    private Map<Edge, EdgeFX> edgeMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        graphPane = new Pane();
        graphPane.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

        // Só cria nó se estiver no modo ADD_NODE
        graphPane.setOnMouseClicked(event -> {
            if (currentMode == Mode.ADD_NODE) {
                createNode(event.getX(), event.getY());
            }
        });

        root.setCenter(graphPane);
        root.setTop(createToolBar());

        statusLabel = new Label("Bem-vindo! Selecione uma ferramenta.");
        statusLabel.setPadding(new Insets(5));
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Editor de Grafos com Dijkstra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ToolBar createToolBar() {
        modeGroup = new ToggleGroup();

        // --- BOTÃO DE MOVER (NOVO) ---
        ToggleButton btnMove = new ToggleButton("✋ Mover");
        btnMove.setToggleGroup(modeGroup);
        btnMove.setOnAction(e -> setMode(Mode.MOVE, "Modo de Movimentação: Arraste os nós."));

        ToggleButton btnAddNode = new ToggleButton("➕ Nó");
        btnAddNode.setToggleGroup(modeGroup);
        btnAddNode.setOnAction(e -> setMode(Mode.ADD_NODE, "Clique na área branca para criar vértices."));

        ToggleButton btnAddEdge = new ToggleButton("🔗 Aresta");
        btnAddEdge.setToggleGroup(modeGroup);
        btnAddEdge.setOnAction(e -> setMode(Mode.ADD_EDGE, "Clique na Origem -> depois no Destino."));

        ToggleButton btnSetStart = new ToggleButton("🚩 Início");
        btnSetStart.setToggleGroup(modeGroup);
        btnSetStart.setOnAction(e -> setMode(Mode.SELECT_SOURCE, "Selecione o ponto de partida."));

        ToggleButton btnSetEnd = new ToggleButton("🏁 Fim");
        btnSetEnd.setToggleGroup(modeGroup);
        btnSetEnd.setOnAction(e -> setMode(Mode.SELECT_TARGET, "Selecione o destino."));

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        Button btnRun = new Button("RODAR ▶");
        btnRun.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
        btnRun.setOnAction(e -> runDijkstra());

        Button btnClear = new Button("Limpar");
        btnClear.setStyle("-fx-text-fill: red;");
        btnClear.setOnAction(e -> clearGraph());

        return new ToolBar(btnMove, btnAddNode, btnAddEdge, sep1, btnSetStart, btnSetEnd, sep2, btnRun, btnClear);
    }

    private void setMode(Mode mode, String message) {
        this.currentMode = mode;
        this.statusLabel.setText(message);

        // Limpa seleções parciais
        if (selectedSourceForEdge != null) {
            nodeMap.get(selectedSourceForEdge).setSelected(false);
            selectedSourceForEdge = null;
        }

        // --- LÓGICA DE TRAVAMENTO ---
        // Se for Modo MOVE, destrava todos os nós. Se não for, trava todos.
        boolean canMove = (mode == Mode.MOVE);
        for (NodeFX node : nodeMap.values()) {
            node.setDraggable(canMove);
        }
    }

    private void createNode(double x, double y) {
        String name = "No " + nodeCounter++;
        Vertex v = new Vertex(name);
        NodeFX nodeFX = new NodeFX(v, x, y);

        // Configura se nasce travado ou solto dependendo do modo atual
        nodeFX.setDraggable(currentMode == Mode.MOVE);

        nodeFX.setOnNodeClickListener(this::handleNodeClick);

        nodeMap.put(v, nodeFX);
        graphPane.getChildren().add(nodeFX);
    }

    private void handleNodeClick(NodeFX nodeFX) {
        // Se estiver movendo, ignoramos cliques lógicos (ou apenas selecionamos visualmente)
        if (currentMode == Mode.MOVE) {
            return;
        }

        Vertex v = nodeFX.getVertex();

        switch (currentMode) {
            case ADD_EDGE:
                if (selectedSourceForEdge == null) {
                    selectedSourceForEdge = v;
                    nodeFX.setSelected(true);
                    statusLabel.setText("Origem: " + v.getName() + ". Selecione o destino.");
                } else {
                    if (selectedSourceForEdge != v) {
                        askWeightAndCreateEdge(selectedSourceForEdge, v);
                    }
                    nodeMap.get(selectedSourceForEdge).setSelected(false);
                    selectedSourceForEdge = null;
                    statusLabel.setText("Aresta criada.");
                }
                break;

            case SELECT_SOURCE:
                startNode = v;
                statusLabel.setText("Início: " + v.getName());
                resetColors();
                nodeFX.setColor(Color.GREEN);
                break;

            case SELECT_TARGET:
                endNode = v;
                statusLabel.setText("Destino: " + v.getName());
                resetColors();
                if (startNode != null) nodeMap.get(startNode).setColor(Color.GREEN);
                nodeFX.setColor(Color.RED);
                break;
        }
    }

    private void askWeightAndCreateEdge(Vertex source, Vertex target) {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Nova Aresta");
        dialog.setHeaderText("Conectando " + source.getName() + " -> " + target.getName());
        dialog.setContentText("Peso:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(weightStr -> {
            try {
                double weight = Double.parseDouble(weightStr);
                source.addEdge(target, weight);

                NodeFX sourceFX = nodeMap.get(source);
                NodeFX targetFX = nodeMap.get(target);
                EdgeFX edgeFX = new EdgeFX(new Edge(target, weight), sourceFX, targetFX);

                Edge realEdge = source.getEdges().get(source.getEdges().size() - 1);
                edgeMap.put(realEdge, edgeFX);
                graphPane.getChildren().add(0, edgeFX);

            } catch (NumberFormatException e) {
                statusLabel.setText("Erro: Peso inválido!");
            }
        });
    }

    private void runDijkstra() {
        if (startNode == null || endNode == null) {
            statusLabel.setText("Selecione INÍCIO e FIM primeiro.");
            return;
        }

        statusLabel.setText("Calculando...");
        resetColors();

        new Thread(() -> {
            DijkstraSolver solver = new DijkstraSolver();
            solver.setListener(new DijkstraListener() {
                @Override
                public void onVertexVisiting(Vertex v) {
                    Platform.runLater(() -> nodeMap.get(v).setColor(Color.YELLOW));
                    sleep(500);
                }
                @Override
                public void onVertexFinalized(Vertex v) {
                    Platform.runLater(() -> nodeMap.get(v).setColor(Color.LIGHTGRAY));
                    sleep(200);
                }
                @Override
                public void onEdgeRelaxed(Edge e, double d) {
                    Platform.runLater(() -> {
                        EdgeFX fx = edgeMap.get(e);
                        if(fx != null) { fx.setStroke(Color.GREEN); fx.setStrokeWidth(4); }
                    });
                    sleep(300);
                }
                @Override
                public void onEdgeRejected(Edge e, double d) {
                    Platform.runLater(() -> {
                        EdgeFX fx = edgeMap.get(e);
                        if(fx != null) { fx.setStroke(Color.RED); fx.setStrokeWidth(1); }
                    });
                    sleep(100);
                }
            });
            solver.findShortestPath(startNode, endNode);
            Platform.runLater(() -> statusLabel.setText("Concluído!"));
        }).start();
    }

    private void resetColors() {
        nodeMap.values().forEach(n -> n.setColor(Color.LIGHTGRAY));
        edgeMap.values().forEach(e -> { e.setStroke(Color.BLACK); e.setStrokeWidth(2); });
        if (startNode != null) nodeMap.get(startNode).setColor(Color.GREEN);
        if (endNode != null) nodeMap.get(endNode).setColor(Color.RED);
    }

    private void clearGraph() {
        graphPane.getChildren().clear();
        nodeMap.clear();
        edgeMap.clear();
        startNode = null;
        endNode = null;
        nodeCounter = 1;
        statusLabel.setText("Grafo limpo.");
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (Exception e) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}