package com.app.bulk_data_processing.controller;

import com.app.bulk_data_processing.dto.JobResponse;
import com.app.bulk_data_processing.dto.UploadRequest;
import com.app.bulk_data_processing.service.ImportJobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ImportJobController {

    private final ImportJobService importJobService;

    public ImportJobController(ImportJobService importJobService) {
        this.importJobService = importJobService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> createJob(@Valid @ModelAttribute UploadRequest uploadRequest){

        return ResponseEntity.ok().body(importJobService.createJob(uploadRequest));
    }

    @GetMapping("/job")
    public ResponseEntity<?> jobStatus(@RequestParam UUID jobId){

        return ResponseEntity.ok().body(importJobService.status(jobId));
    }
}
