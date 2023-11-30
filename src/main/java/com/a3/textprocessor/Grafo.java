package com.a3.textprocessor;


import com.a3.textprocessor.sdk.Aresta;
import com.a3.textprocessor.sdk.Vertice;

import java.util.List;
import java.util.Map;

public class Grafo {
    private List<Aresta> listaDeAdjacencia;
    private Map<String, Vertice> vertices;

    public Grafo() {
    }

    public Grafo(List<Aresta> listaDeAdjacencia, Map<String, Vertice> vertices) {
        this.listaDeAdjacencia = listaDeAdjacencia;
        this.vertices = vertices;
    }

    public List<Aresta> getListaDeAdjacencia() {
        return listaDeAdjacencia;
    }

    public void setListaDeAdjacencia(List<Aresta> listaDeAdjacencia) {
        this.listaDeAdjacencia = listaDeAdjacencia;
    }

    public Map<String, Vertice> getVertices() {
        return vertices;
    }

    public void setVertices(Map<String, Vertice> vertices) {
        this.vertices = vertices;
    }
}
