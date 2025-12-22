# 🕸️ Dijkstra Visualizer - Pro Edition

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-2D2D30?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Completed-success)

Um visualizador interativo e moderno para o **Algoritmo de Dijkstra**, desenvolvido em JavaFX. Este projeto permite criar grafos, manipular vértices e arestas visualmente e observar o comportamento do algoritmo de caminho mínimo em tempo real.

![Screenshot do Projeto](screenshot.png)
*(Adicione um print da sua tela aqui nomeado como screenshot.png)*

## ✨ Funcionalidades

- **🎨 Interface Gráfica Moderna**: Estilo Dark Mode com efeitos Neon e grid estilo engenharia.
- **🖱️ Drag & Drop**: Arraste vértices livremente pela tela com a ferramenta de mover.
- **🛠️ Editor Completo**:
    - Adicionar Vértices e Arestas (com pesos personalizados).
    - Remover elementos (Nós ou Conexões) com apenas um clique.
    - Validação de entrada (impede pesos negativos).
- **⚡ Visualização em Tempo Real**:
    - Animação passo-a-passo do algoritmo explorando o grafo.
    - Indicação visual de nós visitados, finalizados e arestas relaxadas.
    - **Tratamento de Erros**: Detecção automática de grafos desconexos ou caminhos impossíveis.

## 🏗️ Arquitetura e Padrões de Projeto

O projeto foi construído seguindo boas práticas de Engenharia de Software:

- **Observer Pattern**: Utilizado para desacoplar a lógica do algoritmo (`DijkstraSolver`) da interface gráfica (`GraphMain`). O algoritmo "notifica" a UI sobre cada passo sem saber quem está ouvindo.
- **Multithreading**: O algoritmo roda em uma thread separada para garantir que a animação seja fluida e não congele a interface do usuário.
- **JavaFX Custom Components**: Criação de componentes visuais personalizados (`NodeFX`, `EdgeFX`) que encapsulam sua própria lógica de eventos e renderização.

## 🚀 Como Rodar

### Pré-requisitos
- JDK 21 ou superior.
- Maven (opcional, se for gerenciar dependências).

### Passos
1. Clone o repositório:
   ```bash
   git clone [https://github.com/seu-usuario/dijkstra-visualizer.git](https://github.com/seu-usuario/dijkstra-visualizer.git)