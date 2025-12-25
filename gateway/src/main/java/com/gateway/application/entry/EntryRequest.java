package com.gateway.application.entry;

import jakarta.validation.constraints.NotEmpty;

public record EntryRequest(
    String ref,
    @NotEmpty(message = "Entry must have valid content") String content
) {}
