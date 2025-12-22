package algorithm;

import model.Edge;
import model.Vertex;

public interface DijkstraListener {
    // Quando o algoritmo tira um nó da fila para analisar (Pinta de AMARELO/Laranja)
    void onVertexVisiting(Vertex v);

    // Quando o nó foi totalmente processado e fechado (Pinta de VERDE FINAL ou CINZA)
    void onVertexFinalized(Vertex v);

    // Quando uma aresta é analisada e descobre-se um caminho MELHOR (Pinta aresta de VERDE)
    void onEdgeRelaxed(Edge e, double newDistance);

    // Quando uma aresta é analisada mas o caminho é PIOR ou IGUAL (Pinta aresta de VERMELHO)
    void onEdgeRejected(Edge e, double distanceSent);
}