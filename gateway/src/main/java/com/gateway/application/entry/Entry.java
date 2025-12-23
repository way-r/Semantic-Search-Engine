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
import lombok.Getter;

@Entity
@Getter
@Table(name = "Entries")
public class Entry {
    
    @Id UUID id;

    String arxiv_id;
    String title;
    String doi;
    String abstract_content;
    String submitter;
    String category;
    List<String> authors;
    LocalDate publish_date;

    @NotEmpty String embed_status;
    @NotNull LocalDateTime uploaded;
    LocalDateTime completed;

    public Entry() {
    }

    public Entry(UUID id, String arxiv_id, String title, String doi, String abstract_content, String submitter, List<String> authors, String category, LocalDate publish_date, String embed_status, LocalDateTime uploaded, LocalDateTime completed) {
        this.id = id;
        this.arxiv_id = arxiv_id;
        this.title = title;
        this.doi = doi;
        this.abstract_content = abstract_content;
        this.submitter = submitter;
        this.authors = authors;
        this.category = category;
        this.publish_date = publish_date;
        this.embed_status = embed_status;
        this.uploaded = uploaded;
        this.completed = completed;
    }

    public void markComplete(LocalDateTime completed_time) {
        this.embed_status = "COMPLETE";
        this.completed = completed_time;
    }

    public void markFailed() {
        this.embed_status = "FAILED";
    }
}
