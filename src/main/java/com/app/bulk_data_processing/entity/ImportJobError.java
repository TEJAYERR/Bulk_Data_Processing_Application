package com.app.bulk_data_processing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "import_job_errors")
public class ImportJobError {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID errorId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_job_id", nullable = false)
    private ImportJob importJob;

    @Column(nullable = false)
    private long rowNumber;

    @Column(nullable = false)
    private String error;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public ImportJobError(){
    }

    public ImportJobError(ImportJob importJob, long rowNumber, String error){

        this.importJob = importJob;
        this.rowNumber = rowNumber;
        this.error = error;
    }
}
