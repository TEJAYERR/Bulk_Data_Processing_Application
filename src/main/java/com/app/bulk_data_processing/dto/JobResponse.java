package com.app.bulk_data_processing.dto;

import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.JobStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class JobResponse {

    private UUID jobId;
    private JobStatus jobStatus;

    public JobResponse(){}

    public JobResponse(ImportJob importJob){
        this.jobId = importJob.getJobId();
        this.jobStatus = importJob.getJobStatus();
    }
}
