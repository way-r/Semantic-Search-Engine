package com.gateway.application.entry;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Entries")
public class Entry {
    
    @Id UUID id;

    String arxiv_id;
    String title;
    String doi;
    String abstract_content;
    String submittor;
    List<String> authors;
    List<String> categories;
    LocalDate publish_date;

    @NotEmpty String embed_status;
    @NotNull LocalDateTime uploaded;
    LocalDateTime completed;

    public Entry() {
    }

    public Entry(UUID id, String arxiv_id, String title, String doi, String abstract_content, String submittor, List<String> authors, List<String> categories, LocalDate publish_date, String embed_status, LocalDateTime uploaded, LocalDateTime completed) {
        this.id = id;
        this.arxiv_id = arxiv_id;
        this.title = title;
        this.doi = doi;
        this.abstract_content = abstract_content;
        this.submittor = submittor;
        this.authors = authors;
        this.categories = categories;
        this.publish_date = publish_date;
        this.embed_status = embed_status;
        this.uploaded = uploaded;
        this.completed = completed;
    }

    public String get_content() {
        return this.abstract_content;
    }

    public void set_embed_status(String status) {
        this.embed_status = status;
    }

    public void set_completed(LocalDateTime completed_time) {
        this.completed = completed_time;
    }
}
