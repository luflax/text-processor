package com.a3.textprocessor.model;

import lombok.Data;

@Data
public class Link {
    private String source;
    private String target;
    private int value;
}
