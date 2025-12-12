package com.gateway.application.entry;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record EntryRequest(

    @NotEmpty(message = "Entry must have a valid arxiv_id")
    String arxiv_id,
    @NotEmpty(message = "Entry must have a vali title") 
    String title,
    @NotEmpty(message = "Entry must have a vali doi")
    String doi,
    @NotEmpty(message = "Entry must have a vali abstract")
    String abstract_content,
    String submittor,
    List<String> authors,
    List<String> categories,
    LocalDate publish_date
) {}
