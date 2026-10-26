package com.app.bulk_data_processing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "jobId_rowNumber",
                columnNames = {"jobId", "rowNumber"}
        )
})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID userId;
    @Column(nullable = false)
    String name;
    @Column(nullable = false, unique = true)
    String mobileNumber;
    @Column(nullable = false)
    UUID jobId;
    @Column(nullable = false)
    long rowNumber;

    public User(String name, String mobileNumber, UUID jobId, long rowNumber){
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.jobId = jobId;
        this.rowNumber = rowNumber;
    }

    public User() {
    }
}
