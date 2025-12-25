package com.gateway.application.search;

import jakarta.validation.constraints.NotEmpty;

public record Search(
    @NotEmpty(message = "Search must be valid") String keyword,
    int k
) {

    public Search(String keyword) {
        this(keyword, 5);
    }
}
