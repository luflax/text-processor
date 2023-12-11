# A3 - Estrutura de dados e análise de algoritmos

## Pre-requisitos
- Java SDK 17
- Node v20
- Angular CLI
- Maven 3.9.5

## Instruções
- Fazer clone do projeto
- Executar o projeto SpringBoot com o comando mvn spring-boot:run na pasta raiz do projeto
- Executar o projeto Angular frontend com o comando npm start na pasta ./frontend

## Aplicação live para teste:
Para carregar um resumo escreva o nome do arquivo .txt assim como foi disponibilizado, exemplo: arq_1.txt, arq_2.txt, arq_50.txt, etc..
https://visualizador-de-textos.onrender.com/

## Análise assintótica
Analisamos os principais algoritmos que foram usados no projeto e deixamos comentários no próprio código com os cálculos feitos, abaixo está o resumo final:

 - Leitura de um artigo
   - O(n)
   - Método: InputRead#readArtigo
 - Criação do grafo com base na frequência em que as palavras aparecem juntas em cada frase:
   -  O(n^2)
   - Método: GrafoService#criarGrafo
 - Remover arestas desnecessárias com o Algoritmo de Kruskal (árvore geradora máxima)
   - O(n * log(m))
   - Método: GrafoService#removerArestasDesnecessarias
 - Criação do grafo de top words
   - O(n)
   - Método: GrafoService#createGrafoTopWords
 - Criação do grafo de coautoria
   - O(n^3)
   - Método: GrafoService#generateCoautoriaGraph
 - Criação do grafo de similaridade de resumos
   - O(n^2)
   - Método: GrafoService#generateSimilaridadeTextosGraph
 - Criar objeto de resposta para o frontend plotar
   - O(n)
   - Método: GrafoService#createResponse