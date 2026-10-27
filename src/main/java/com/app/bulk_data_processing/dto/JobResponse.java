package com.app.bulk_data_processing.dto;

import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.ImportJobError;
import com.app.bulk_data_processing.entity.JobStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class JobResponse {

    private UUID jobId;
    private JobStatus jobStatus;
    private List<JobErrorResponse> jobErrors;

    public JobResponse(){}

    public JobResponse(ImportJob importJob, List<JobErrorResponse> errors){
        this.jobId = importJob.getJobId();
        this.jobStatus = importJob.getJobStatus();
        this.jobErrors = errors;
    }
}
