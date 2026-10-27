package com.app.bulk_data_processing.repository;

import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.ImportJobError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImportJobErrorRepo extends JpaRepository<ImportJobError, UUID> {
    List<ImportJobError> findByImportJob(ImportJob importJob);
}
