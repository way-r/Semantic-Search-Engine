package com.gateway.application.entry;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Entry")
public class Entry {
    
    @Id UUID id;

    String ref;
    @NotEmpty String content;

    @NotEmpty String embed_status;
    @NotNull LocalDateTime uploaded;
    LocalDateTime completed;

    public void markComplete(LocalDateTime completed_time) {
        this.embed_status = "COMPLETE";
        this.completed = completed_time;
    }

    public void markFailed() {
        this.embed_status = "FAILED";
    }
}
