package app;

import algorithm.DijkstraListener;
import algorithm.DijkstraSolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Edge;
import model.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GraphMain extends Application {

    private Pane graphPane;
    private ToggleGroup modeGroup;
    private Label statusLabel;

    private enum Mode { MOVE, ADD_NODE, ADD_EDGE, SELECT_SOURCE, SELECT_TARGET, REMOVE, NONE }
    private Mode currentMode = Mode.NONE;

    private Vertex selectedSourceForEdge = null;
    private Vertex startNode = null;
    private Vertex endNode = null;
    private int nodeCounter = 1;

    private Map<Vertex, NodeFX> nodeMap = new HashMap<>();
    private Map<Edge, EdgeFX> edgeMap = new HashMap<>();

    // CORES GLOBAIS
    private final String BG_COLOR = "#1e1e1e";
    private final String TOOLBAR_COLOR = "#2d2d2d";
    private final String TEXT_COLOR = "#e0e0e0";
    private final String ACCENT_COLOR = "#00d2ff";

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // 1. ÁREA DO GRAFO
        graphPane = new Pane();
        Canvas gridCanvas = new Canvas(1200, 800);
        drawGrid(gridCanvas);
        graphPane.getChildren().add(gridCanvas);
        gridCanvas.setMouseTransparent(true);

        graphPane.setOnMouseClicked(event -> {
            if (currentMode == Mode.ADD_NODE) {
                createNode(event.getX(), event.getY());
            }
        });

        root.setCenter(graphPane);
        root.setTop(createToolBar());

        statusLabel = new Label("Bem-vindo ao Dijkstra Visualizer! Selecione uma ferramenta.");
        statusLabel.setPadding(new Insets(10));
        statusLabel.setTextFill(Color.web(TEXT_COLOR));
        statusLabel.setFont(Font.font("Consolas", 14));
        statusLabel.setStyle("-fx-background-color: " + TOOLBAR_COLOR + "; -fx-border-color: #444; -fx-border-width: 1 0 0 0;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1000, 700);

        // CONFIGURAÇÃO DO ÍCONE E TÍTULO
        primaryStage.setTitle("Dijkstra Visualizer Pro");
        primaryStage.getIcons().add(createAppIcon()); // Gera o ícone via código

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- GERAÇÃO DE ÍCONE PROCEDURAL ---
    private Image createAppIcon() {
        // Cria um Canvas pequeno para desenhar o ícone
        int size = 64;
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fundo transparente (não desenhamos rect cheio)

        // Desenha arestas (linhas)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeLine(16, 48, 32, 16); // Nó Esq -> Topo
        gc.strokeLine(32, 16, 48, 48); // Topo -> Dir
        gc.strokeLine(16, 48, 48, 48); // Esq -> Dir

        // Desenha 3 nós (bolinhas)
        gc.setFill(Color.web(ACCENT_COLOR)); // Azul Neon
        gc.fillOval(8, 40, 16, 16); // Nó Esq

        gc.setFill(Color.web("#ff007f")); // Rosa Neon
        gc.fillOval(24, 8, 16, 16); // Nó Topo

        gc.setFill(Color.web("#39ff14")); // Verde Neon
        gc.fillOval(40, 40, 16, 16); // Nó Dir

        // Converte o desenho para uma Imagem
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }

    private void drawGrid(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web(BG_COLOR));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(1);

        for (double i = 0; i < canvas.getWidth(); i += 40) {
            gc.strokeLine(i, 0, i, canvas.getHeight());
        }
        for (double j = 0; j < canvas.getHeight(); j += 40) {
            gc.strokeLine(0, j, canvas.getWidth(), j);
        }
    }

    private ToolBar createToolBar() {
        modeGroup = new ToggleGroup();

        String btnStyle = "-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;";
        String btnSelectedStyle = "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5;";

        ToggleButton btnMove = createStyledToggle("✋ Mover", Mode.MOVE, btnStyle, btnSelectedStyle);
        ToggleButton btnAddNode = createStyledToggle("➕ Nó", Mode.ADD_NODE, btnStyle, btnSelectedStyle);
        ToggleButton btnAddEdge = createStyledToggle("🔗 Aresta", Mode.ADD_EDGE, btnStyle, btnSelectedStyle);
        ToggleButton btnRemove = createStyledToggle("🗑 Remover", Mode.REMOVE, btnStyle, btnSelectedStyle);

        ToggleButton btnSetStart = createStyledToggle("🚩 Início", Mode.SELECT_SOURCE, btnStyle, btnSelectedStyle);
        ToggleButton btnSetEnd = createStyledToggle("🏁 Fim", Mode.SELECT_TARGET, btnStyle, btnSelectedStyle);

        Separator sep1 = new Separator(); sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        Separator sep2 = new Separator(); sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        // Separador novo para agrupar os botões de ação
        Separator sep3 = new Separator(); sep3.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Button btnRun = new Button("RODAR DIJKSTRA ▶");
        btnRun.setStyle("-fx-background-color: #39ff14; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(57, 255, 20, 0.4), 10, 0, 0, 0);");
        btnRun.setOnAction(e -> runDijkstra());

        Button btnClear = new Button("Limpar Tudo");
        btnClear.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnClear.setOnAction(e -> clearGraph());

        Button btnRandom = new Button("🎲 Gerar Grafo");
        btnRandom.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnRandom.setOnAction(e -> generateRandomGraph());

        // --- NOVO BOTÃO RESETAR ---
        Button btnReset = new Button("🔄 Resetar");
        btnReset.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        btnReset.setOnAction(e -> resetAlgorithm());

        // Adicionei o sep3 e o btnReset na barra
        ToolBar tb = new ToolBar(btnMove, btnAddNode, btnAddEdge, sep1, btnRemove, sep2, btnSetStart, btnSetEnd, sep3, btnRandom, btnReset, btnRun, btnClear);
        tb.setStyle("-fx-background-color: " + TOOLBAR_COLOR + "; -fx-padding: 10px; -fx-spacing: 10px;");
        return tb;
    }

    private ToggleButton createStyledToggle(String text, Mode mode, String normalStyle, String selectedStyle) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(modeGroup);
        btn.setStyle(normalStyle);
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> btn.setStyle(newVal ? selectedStyle : normalStyle));
        btn.setOnAction(e -> setMode(mode, "Modo: " + text));
        return btn;
    }

    private void setMode(Mode mode, String message) {
        this.currentMode = mode;
        this.statusLabel.setText(message);

        if (selectedSourceForEdge != null) {
            nodeMap.get(selectedSourceForEdge).setSelected(false);
            selectedSourceForEdge = null;
        }

        boolean canMove = (mode == Mode.MOVE);
        for (NodeFX node : nodeMap.values()) {
            node.setDraggable(canMove);
            node.setOpacity(canMove ? 1.0 : 0.9);
        }
    }

    private void createNode(double x, double y) {
        String name = "No " + nodeCounter++;
        Vertex v = new Vertex(name);
        NodeFX nodeFX = new NodeFX(v, x, y);
        nodeFX.setDraggable(currentMode == Mode.MOVE);
        nodeFX.setOnNodeClickListener(this::handleNodeClick);
        nodeMap.put(v, nodeFX);
        graphPane.getChildren().add(nodeFX);
    }

    private void handleNodeClick(NodeFX nodeFX) {
        if (currentMode == Mode.MOVE) return;

        Vertex v = nodeFX.getVertex();

        switch (currentMode) {
            case ADD_EDGE:
                if (selectedSourceForEdge == null) {
                    selectedSourceForEdge = v;
                    nodeFX.setSelected(true);
                    statusLabel.setText("Origem: " + v.getName() + " selecionada. Clique no destino.");
                } else {
                    if (selectedSourceForEdge != v) {
                        askWeightAndCreateEdge(selectedSourceForEdge, v);
                    }
                    nodeMap.get(selectedSourceForEdge).setSelected(false);
                    selectedSourceForEdge = null;
                    statusLabel.setText("Aresta criada!");
                }
                break;
            case REMOVE:
                removeNode(v);
                statusLabel.setText("Nó removido.");
                break;
            case SELECT_SOURCE:
                startNode = v;
                statusLabel.setText("🚩 Partida: " + v.getName());
                resetColors();
                nodeFX.setColor(Color.GREEN);
                break;
            case SELECT_TARGET:
                endNode = v;
                statusLabel.setText("🏁 Destino: " + v.getName());
                resetColors();
                if (startNode != null) nodeMap.get(startNode).setColor(Color.GREEN);
                nodeFX.setColor(Color.RED);
                break;
        }
    }

    private void removeNode(Vertex v) {
        List<Edge> edgesToRemove = new ArrayList<>();
        for (Vertex other : nodeMap.keySet()) {
            for (Edge e : other.getEdges()) {
                if (e.getTarget() == v || other == v) edgesToRemove.add(e);
            }
        }
        for (Edge e : edgesToRemove) removeEdge(e);
        graphPane.getChildren().remove(nodeMap.get(v));
        nodeMap.remove(v);
        if (startNode == v) startNode = null;
        if (endNode == v) endNode = null;
    }

    private void removeEdge(Edge e) {
        if (edgeMap.containsKey(e)) {
            graphPane.getChildren().remove(edgeMap.get(e));
            edgeMap.remove(e);
        }
        for (Vertex v : nodeMap.keySet()) v.getEdges().remove(e);
    }

    private void askWeightAndCreateEdge(Vertex source, Vertex target) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nova Aresta");

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #2d2d2d;");

        Label headerLabel = new Label("Conectando " + source.getName() + " -> " + target.getName());
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px;");
        pane.setHeader(headerLabel);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label msgLabel = new Label("Digite o peso (Positivo):");
        msgLabel.setStyle("-fx-text-fill: #cccccc;");

        TextField input = new TextField("10");
        input.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-size: 14px;");

        content.getChildren().addAll(msgLabel, input);
        pane.setContent(content);

        ButtonType okButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        javafx.scene.Node okButton = pane.lookupButton(okButtonType);
        okButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: black; -fx-font-weight: bold;");

        Platform.runLater(input::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) return input.getText();
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(weightStr -> {
            try {
                double weight = Double.parseDouble(weightStr);

                // --- VALIDAÇÃO DE PESO NEGATIVO ---
                if (weight < 0) {
                    statusLabel.setText("❌ Erro: Pesos negativos não são permitidos no Dijkstra!");
                    showErrorDialog("Peso Inválido",
                            "O algoritmo de Dijkstra não suporta pesos negativos (Custo: " + weight + ").\n" +
                                    "Isso quebraria a matemática do algoritmo.\n" +
                                    "Por favor, use apenas valores positivos (>= 0).");
                    return; // Cancela a criação
                }

                source.addEdge(target, weight);
                Edge realEdge = source.getEdges().get(source.getEdges().size() - 1);
                NodeFX sourceFX = nodeMap.get(source);
                NodeFX targetFX = nodeMap.get(target);
                EdgeFX edgeFX = new EdgeFX(realEdge, sourceFX, targetFX);

                edgeFX.setOnMouseClicked(ev -> {
                    if (currentMode == Mode.REMOVE) {
                        removeEdge(realEdge);
                        statusLabel.setText("Aresta removida.");
                    }
                });

                edgeMap.put(realEdge, edgeFX);
                graphPane.getChildren().add(1, edgeFX);
                statusLabel.setText("Aresta criada com sucesso!");

            } catch (NumberFormatException e) {
                statusLabel.setText("Erro: Use apenas números válidos!");
            }
        });
    }

    private void runDijkstra() {
        if (startNode == null || endNode == null) {
            statusLabel.setText("⚠️ Selecione INÍCIO e FIM antes de rodar!");
            return;
        }

        statusLabel.setText("🚀 Calculando rota...");
        resetColors();

        new Thread(() -> {
            DijkstraSolver solver = new DijkstraSolver();

            solver.setListener(new DijkstraListener() {
                @Override
                public void onVertexVisiting(Vertex v) {
                    Platform.runLater(() -> {
                        if (v != startNode && v != endNode && nodeMap.containsKey(v)) {
                            nodeMap.get(v).setColor(Color.YELLOW);
                        }
                        statusLabel.setText("Visitando: " + v.getName());
                    });
                    sleep(400);
                }
                @Override
                public void onVertexFinalized(Vertex v) {
                    Platform.runLater(() -> {
                        if (v != startNode && v != endNode && nodeMap.containsKey(v)) {
                            nodeMap.get(v).setColor(Color.LIGHTGREEN);
                        }
                    });
                    sleep(100);
                }
                @Override
                public void onEdgeRelaxed(Edge e, double d) {
                    Platform.runLater(() -> {
                        EdgeFX fx = edgeMap.get(e);
                        if(fx != null) { fx.setStroke(Color.GREEN); fx.setStrokeWidth(3); }
                    });
                    sleep(300);
                }
                @Override
                public void onEdgeRejected(Edge e, double d) {
                    Platform.runLater(() -> {
                        EdgeFX fx = edgeMap.get(e);
                        if(fx != null) { fx.setStroke(Color.RED); fx.setStrokeWidth(1); }
                    });
                    sleep(50);
                }
            });

            // Captura o resultado
            java.util.List<Vertex> path = solver.findShortestPath(startNode, endNode);

            Platform.runLater(() -> {
                // --- VERIFICAÇÃO DE CAMINHO VAZIO ---
                if (path.isEmpty()) {
                    statusLabel.setText("❌ ERRO: Destino inalcançável!");

                    if (nodeMap.containsKey(endNode)) {
                        nodeMap.get(endNode).setColor(Color.RED); // Destino fica VERMELHO
                    }

                    showErrorDialog("Rota Impossível",
                            "O algoritmo terminou mas não conseguiu chegar em " + endNode.getName() + ".\n" +
                                    "Verifique se as setas estão apontando para a direção correta.");

                } else {
                    double totalCost = calculatePathCost(path);
                    statusLabel.setText("✅ Sucesso! Custo Total: " + totalCost);
                    highlightPath(path);
                }
            });

        }).start();
    }

    private void resetColors() {
        nodeMap.values().forEach(n -> n.setColor(Color.LIGHTGRAY));
        edgeMap.values().forEach(e -> { e.setStroke(Color.BLACK); e.setStrokeWidth(2); });
        if (startNode != null && nodeMap.containsKey(startNode)) nodeMap.get(startNode).setColor(Color.GREEN);
        if (endNode != null && nodeMap.containsKey(endNode)) nodeMap.get(endNode).setColor(Color.RED);
    }

    private void clearGraph() {
        graphPane.getChildren().removeIf(node -> !(node instanceof Canvas));
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

    // Calcula a soma dos pesos do caminho final
    private double calculatePathCost(java.util.List<Vertex> path) {
        double cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Vertex u = path.get(i);
            Vertex v = path.get(i+1);
            for (Edge e : u.getEdges()) {
                if (e.getTarget() == v) {
                    cost += e.getWeight();
                    break;
                }
            }
        }
        return cost;
    }

    private void highlightPath(java.util.List<Vertex> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            Vertex u = path.get(i);
            Vertex v = path.get(i+1);

            nodeMap.get(u).setColor(Color.CYAN);
            nodeMap.get(v).setColor(Color.CYAN);

            for (Edge e : u.getEdges()) {
                if (e.getTarget() == v) {
                    EdgeFX fx = edgeMap.get(e);
                    if (fx != null) {
                        fx.setStroke(Color.CYAN);
                        fx.setStrokeWidth(4);
                        fx.setOpacity(1.0);
                    }
                }
            }
        }
    }

    // Popup de Erro Customizado (Com texto BRANCO para não sumir no fundo escuro)
    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #ff4444; -fx-border-width: 2;");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label titleLabel = new Label("🚫 " + title);
        titleLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label msgLabel = new Label(content);
        msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(350);

        vbox.getChildren().addAll(titleLabel, msgLabel);
        pane.setContent(vbox);

        pane.getButtonTypes().forEach(btnType -> {
            javafx.scene.Node btn = pane.lookupButton(btnType);
            btn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #ff4444;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #444;"));
        });

        alert.showAndWait();
    }

    private void generateRandomGraph() {
        // 1. Cria o Dialog para perguntar a quantidade
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Gerar Grafo Aleatório");

        // Estilo Dark Mode (Consistente com o resto do app)
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #2d2d2d;");

        Label headerLabel = new Label("Configuração do Grafo");
        headerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px;");
        pane.setHeader(headerLabel);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label msgLabel = new Label("Quantidade de Vértices (Recomendado: 10-30):");
        msgLabel.setStyle("-fx-text-fill: #cccccc;");

        TextField input = new TextField("15"); // Valor padrão
        input.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-size: 14px;");

        content.getChildren().addAll(msgLabel, input);
        pane.setContent(content);

        ButtonType okButtonType = new ButtonType("Gerar", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Estiliza o botão de ação
        javafx.scene.Node okButton = pane.lookupButton(okButtonType);
        okButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Platform.runLater(input::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) return input.getText();
            return null;
        });

        // 2. Processa a resposta
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int numNodes = Integer.parseInt(qtyStr);

                // Validações de segurança
                if (numNodes < 2) {
                    showErrorDialog("Poucos Vértices", "O grafo precisa de pelo menos 2 vértices para brincar.");
                    return;
                }
                if (numNodes > 100) {
                    showErrorDialog("Muitos Vértices", "Mais de 100 vértices pode deixar a tela ilegível.\nTente um número menor.");
                    return;
                }

                // 3. A Geração do Grafo (O Caos Organizado)
                clearGraph();

                java.util.Random rand = new java.util.Random();
                // Margens para não ficar colado na borda ou em cima da toolbar
                double minX = 50;
                double maxX = graphPane.getWidth() - 50;
                if (maxX <= 0) maxX = 800; // Fallback se a janela ainda não abriu totalmente

                double minY = 50;
                double maxY = graphPane.getHeight() - 50;
                if (maxY <= 0) maxY = 500;

                List<Vertex> vertices = new ArrayList<>();

                // Cria Nós
                for (int i = 0; i < numNodes; i++) {
                    double x = minX + rand.nextDouble() * (maxX - minX);
                    double y = minY + rand.nextDouble() * (maxY - minY);
                    createNode(x, y);
                }

                vertices.addAll(nodeMap.keySet());

                // Cria Arestas (Cada nó tenta se ligar a 1 ou 2 outros)
                for (Vertex u : vertices) {
                    int connections = 1 + rand.nextInt(2);

                    for (int j = 0; j < connections; j++) {
                        Vertex v = vertices.get(rand.nextInt(vertices.size()));

                        // Evita laços (u->u)
                        if (u != v) {
                            double weight = 1 + rand.nextInt(20); // Peso 1 a 20

                            u.addEdge(v, weight);
                            Edge realEdge = u.getEdges().get(u.getEdges().size() - 1);

                            NodeFX sourceFX = nodeMap.get(u);
                            NodeFX targetFX = nodeMap.get(v);

                            EdgeFX edgeFX = new EdgeFX(realEdge, sourceFX, targetFX);
                            edgeFX.setOnMouseClicked(ev -> {
                                if (currentMode == Mode.REMOVE) removeEdge(realEdge);
                            });

                            edgeMap.put(realEdge, edgeFX);
                            // Índice 1 para ficar acima do grid (0)
                            graphPane.getChildren().add(1, edgeFX);
                        }
                    }
                }
                statusLabel.setText("✨ Grafo com " + numNodes + " vértices gerado! Selecione Início e Fim.");

            } catch (NumberFormatException e) {
                showErrorDialog("Número Inválido", "Por favor, digite um número inteiro.");
            }
        });
    }

    private void resetAlgorithm() {
        resetColors(); // Remove apenas a pintura (amarelo/verde/vermelho/azul)
        statusLabel.setText("Visualização resetada. O grafo foi mantido.");
    }
}