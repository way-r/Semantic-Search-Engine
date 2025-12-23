package com.gateway.application.entry;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record EntryRequest(

    @NotEmpty(message = "Entry must have a valid arxiv_id")
    String arxiv_id,
    @NotEmpty(message = "Entry must have a valid title") 
    String title,
    @NotEmpty(message = "Entry must have a valid doi")
    String doi,
    @NotEmpty(message = "Entry must have a valid abstract")
    String abstract_content,
    String submitter,
    String category,
    List<String> authors,
    LocalDate publish_date
) {}
