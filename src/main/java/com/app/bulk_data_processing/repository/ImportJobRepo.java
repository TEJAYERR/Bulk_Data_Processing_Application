package com.app.bulk_data_processing.repository;

import com.app.bulk_data_processing.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportJobRepo extends JpaRepository<ImportJob, UUID> {
}
