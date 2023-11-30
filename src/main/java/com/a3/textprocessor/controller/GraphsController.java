package com.a3.textprocessor.controller;

import com.a3.textprocessor.model.GrafoResponse;
import com.a3.textprocessor.service.GrafoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs")
public class GraphsController {

    private final GrafoService grafoService;

    @Autowired
    public GraphsController(GrafoService grafoService) {
        this.grafoService = grafoService;
    }

    @GetMapping("")
    public GrafoResponse graph(String fileName) {
        return this.grafoService.generateGraph(fileName);
    }

    @GetMapping("topWords")
    public GrafoResponse topWords(String fileName, int numberOfWords) {
        return this.grafoService.generateTopWordsGraph(fileName, numberOfWords);
    }

    @GetMapping("coautoria")
    public GrafoResponse coautoria() {
        return this.grafoService.generateCoautoriaGraph();
    }
}
