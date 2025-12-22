package app;

import algorithm.DijkstraListener;
import algorithm.DijkstraSolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Edge;
import model.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GraphMain extends Application {

    // Componentes da Interface
    private Pane graphPane;
    private ToggleGroup modeGroup;
    private Label statusLabel;

    // Estados da Aplicação
    private enum Mode { ADD_NODE, ADD_EDGE, SELECT_SOURCE, SELECT_TARGET, NONE }
    private Mode currentMode = Mode.NONE;

    // Dados temporários para interações
    private Vertex selectedSourceForEdge = null;
    private Vertex startNode = null;
    private Vertex endNode = null;
    private int nodeCounter = 1; // Para nomear automaticamente (Nó 1, Nó 2...)

    // Mapas de visualização
    private Map<Vertex, NodeFX> nodeMap = new HashMap<>();
    private Map<Edge, EdgeFX> edgeMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // 1. ÁREA DO GRAFO (Centro)
        graphPane = new Pane();
        graphPane.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

        // Evento: Clicar no fundo vazio
        graphPane.setOnMouseClicked(event -> {
            if (currentMode == Mode.ADD_NODE) {
                createNode(event.getX(), event.getY());
            }
        });

        root.setCenter(graphPane);

        // 2. BARRA DE FERRAMENTAS (Topo)
        ToolBar toolBar = createToolBar();
        root.setTop(toolBar);

        // 3. BARRA DE STATUS (Fundo)
        statusLabel = new Label("Bem-vindo! Selecione 'Adicionar Nó' para começar.");
        statusLabel.setPadding(new Insets(5));
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Editor de Grafos com Dijkstra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ToolBar createToolBar() {
        modeGroup = new ToggleGroup();

        // Botões de Modo
        ToggleButton btnAddNode = new ToggleButton("Adicionar Nó");
        btnAddNode.setToggleGroup(modeGroup);
        btnAddNode.setOnAction(e -> setMode(Mode.ADD_NODE, "Clique na área branca para criar vértices."));

        ToggleButton btnAddEdge = new ToggleButton("Adicionar Aresta");
        btnAddEdge.setToggleGroup(modeGroup);
        btnAddEdge.setOnAction(e -> setMode(Mode.ADD_EDGE, "Clique no nó de Origem, depois no de Destino."));

        ToggleButton btnSetStart = new ToggleButton("Definir Início");
        btnSetStart.setToggleGroup(modeGroup);
        btnSetStart.setOnAction(e -> setMode(Mode.SELECT_SOURCE, "Clique no nó de partida para o algoritmo."));

        ToggleButton btnSetEnd = new ToggleButton("Definir Fim");
        btnSetEnd.setToggleGroup(modeGroup);
        btnSetEnd.setOnAction(e -> setMode(Mode.SELECT_TARGET, "Clique no nó de destino."));

        // --- A CORREÇÃO ESTÁ AQUI ---
        // Crie dois objetos diferentes em vez de reutilizar a variável "separator"
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        Button btnRun = new Button("RODAR DIJKSTRA ▶");
        btnRun.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
        btnRun.setOnAction(e -> runDijkstra());

        Button btnClear = new Button("Limpar Tudo");
        btnClear.setStyle("-fx-text-fill: red;");
        btnClear.setOnAction(e -> clearGraph());

        // Use sep1 e sep2 na lista
        return new ToolBar(btnAddNode, btnAddEdge, sep1, btnSetStart, btnSetEnd, sep2, btnRun, btnClear);
    }

    private void setMode(Mode mode, String message) {
        this.currentMode = mode;
        this.statusLabel.setText(message);
        // Reseta seleções parciais se mudar de modo
        if (selectedSourceForEdge != null) {
            nodeMap.get(selectedSourceForEdge).setSelected(false);
            selectedSourceForEdge = null;
        }
    }

    // --- LÓGICA DE CRIAÇÃO ---

    private void createNode(double x, double y) {
        String name = "No " + nodeCounter++;
        Vertex v = new Vertex(name);

        NodeFX nodeFX = new NodeFX(v, x, y);

        // Evento: Clicar numa bolinha existente
        nodeFX.setOnMouseClicked(event -> handleNodeClick(nodeFX));

        nodeMap.put(v, nodeFX);
        graphPane.getChildren().add(nodeFX);
    }

    private void handleNodeClick(NodeFX nodeFX) {
        Vertex v = nodeFX.getVertex();

        switch (currentMode) {
            case ADD_EDGE:
                if (selectedSourceForEdge == null) {
                    // Passo 1: Selecionou a origem
                    selectedSourceForEdge = v;
                    nodeFX.setSelected(true);
                    statusLabel.setText("Origem: " + v.getName() + ". Agora clique no destino.");
                } else {
                    // Passo 2: Selecionou o destino
                    if (selectedSourceForEdge != v) { // Não pode criar laço para si mesmo
                        askWeightAndCreateEdge(selectedSourceForEdge, v);
                    }
                    // Limpa seleção
                    nodeMap.get(selectedSourceForEdge).setSelected(false);
                    selectedSourceForEdge = null;
                    statusLabel.setText("Aresta criada. Selecione outra origem ou troque de modo.");
                }
                break;

            case SELECT_SOURCE:
                startNode = v;
                statusLabel.setText("Início definido: " + v.getName());
                resetColors();
                nodeFX.setColor(Color.GREEN);
                break;

            case SELECT_TARGET:
                endNode = v;
                statusLabel.setText("Destino definido: " + v.getName());
                resetColors(); // Remove cores antigas mas mantém start verde se houver
                if (startNode != null) nodeMap.get(startNode).setColor(Color.GREEN);
                nodeFX.setColor(Color.RED);
                break;
        }
    }

    private void askWeightAndCreateEdge(Vertex source, Vertex target) {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Peso da Aresta");
        dialog.setHeaderText("Conectando " + source.getName() + " -> " + target.getName());
        dialog.setContentText("Digite o custo/distância:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(weightStr -> {
            try {
                double weight = Double.parseDouble(weightStr);

                // Cria no modelo
                source.addEdge(target, weight);

                // Cria no visual (EdgeFX)
                NodeFX sourceFX = nodeMap.get(source);
                NodeFX targetFX = nodeMap.get(target);
                EdgeFX edgeFX = new EdgeFX(new Edge(target, weight), sourceFX, targetFX);

                edgeMap.put(new Edge(target, weight), edgeFX); // Atenção aqui: chave do mapa pode ser tricky se Edge não tiver equals/hashcode, mas pro visual ok
                // Adiciona edgeFX para ser desenhado
                // OBS: Como a classe Vertex cria um NEW Edge internamente, precisamos pegar aquele específico.
                // Truque: Pegar o último adicionado na lista do vértice
                Edge realEdge = source.getEdges().get(source.getEdges().size() - 1);
                edgeMap.put(realEdge, edgeFX);

                graphPane.getChildren().add(0, edgeFX); // Add no índice 0 para ficar atrás

            } catch (NumberFormatException e) {
                statusLabel.setText("Erro: Peso inválido!");
            }
        });
    }

    // --- LÓGICA DO ALGORITMO ---

    private void runDijkstra() {
        if (startNode == null || endNode == null) {
            statusLabel.setText("ERRO: Selecione um Início e um Fim antes de rodar!");
            return;
        }

        statusLabel.setText("Rodando Dijkstra de " + startNode.getName() + " para " + endNode.getName() + "...");

        // Reset visual antes de rodar
        resetColors();

        new Thread(() -> {
            DijkstraSolver solver = new DijkstraSolver();

            // Listener Visual (Copiado e adaptado da versão anterior)
            solver.setListener(new DijkstraListener() {
                @Override
                public void onVertexVisiting(Vertex v) {
                    Platform.runLater(() -> {
                        nodeMap.get(v).setColor(Color.YELLOW);
                        statusLabel.setText("Visitando: " + v.getName());
                    });
                    sleep(600);
                }

                @Override
                public void onVertexFinalized(Vertex v) {
                    Platform.runLater(() -> nodeMap.get(v).setColor(Color.LIGHTGRAY));
                    sleep(200);
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
                            fx.setStrokeWidth(1);
                        }
                    });
                    sleep(200);
                }
            });

            solver.findShortestPath(startNode, endNode);

            Platform.runLater(() -> statusLabel.setText("Algoritmo finalizado!"));

        }).start();
    }

    private void resetColors() {
        nodeMap.values().forEach(n -> n.setColor(Color.LIGHTGRAY));
        edgeMap.values().forEach(e -> {
            e.setStroke(Color.BLACK);
            e.setStrokeWidth(2);
        });
        // Restaura cores de seleção se existirem
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
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}