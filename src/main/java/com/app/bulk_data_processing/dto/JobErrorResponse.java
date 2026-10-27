package com.app.bulk_data_processing.dto;

import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.ImportJobError;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JobErrorResponse {

    private UUID errorId;
    private long rowNumber;
    private String error;
    private LocalDateTime createdAt;

    public JobErrorResponse(){
    }

    public JobErrorResponse(ImportJobError importJobError){

        this.errorId = importJobError.getErrorId();
        this.rowNumber = importJobError.getRowNumber();
        this.error = importJobError.getError();
        this.createdAt = importJobError.getCreatedAt();
    }
}
