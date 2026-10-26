package com.app.bulk_data_processing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "import_jobs")
@Data
public class ImportJob {

    @Id
    private UUID jobId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus jobStatus;
    @Column(nullable = false)
    private String filePath;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private String originalFileName;

    @Column
    private List<String> errors;

    @Column(nullable = false, unique = true)
    private String fileHash;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

