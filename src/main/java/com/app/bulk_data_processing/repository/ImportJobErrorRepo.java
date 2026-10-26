package com.app.bulk_data_processing.repository;

import com.app.bulk_data_processing.entity.ImportJobError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportJobErrorRepo extends JpaRepository<ImportJobError, UUID> {
}
