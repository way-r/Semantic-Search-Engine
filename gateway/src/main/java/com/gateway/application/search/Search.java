package com.gateway.application.search;

import jakarta.validation.constraints.NotEmpty;

public record Search(
    @NotEmpty(message = "Cannot search with an invalid keyword")
    String keyword,
    int k
) {

    public Search(String keyword) {
        this(keyword, 10);
    }
}
