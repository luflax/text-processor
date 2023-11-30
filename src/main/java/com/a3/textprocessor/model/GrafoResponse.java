package com.a3.textprocessor.model;

import lombok.Data;

import java.util.List;

@Data
public class GrafoResponse {
    private List<Node> nodes;
    private List<Link> links;

    public GrafoResponse(List<Node> nodes, List<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }
}
