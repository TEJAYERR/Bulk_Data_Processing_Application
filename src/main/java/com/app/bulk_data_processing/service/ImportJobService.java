package com.app.bulk_data_processing.service;

import com.app.bulk_data_processing.dto.JobResponse;
import com.app.bulk_data_processing.dto.UploadRequest;
import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.JobStatus;
import com.app.bulk_data_processing.producer.ImportJobProducer;
import com.app.bulk_data_processing.repository.ImportJobRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ImportJobService {

    private final ImportJobRepo importJobRepo;
    private final ImportJobProducer importJobProducer;

    public ImportJobService(ImportJobRepo importJobRepo, ImportJobProducer importJobProducer) {
        this.importJobRepo = importJobRepo;
        this.importJobProducer = importJobProducer;
    }

    public JobResponse createJob(UploadRequest uploadRequest){

        //check if file already exist and processed in the database
        String fileHash = calculateFileHash(uploadRequest.getMultipartFile());

        if(importJobRepo.existsByFileHash(fileHash)){
            throw new RuntimeException("already in storage will be processed soon.");
        }
        //save file logic
        UUID jobId = UUID.randomUUID();

        String originalFileName = uploadRequest.getMultipartFile().getOriginalFilename();
        String dir = "D:\\projects\\bulk-data-processing\\uploads";
        String fileName = jobId+"_"+originalFileName; //create file name but the jobId comes only after the storing of job
        Path path = Paths.get(dir, fileName); //I will copy contents from one file to another is it ok here is some confusion

        try {
            Files.createDirectories(path.getParent());
            uploadRequest.getMultipartFile().transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ImportJob newJob = new ImportJob();
        newJob.setJobId(jobId);
        newJob.setJobStatus(JobStatus.ACCEPTED);
        newJob.setOriginalFileName(originalFileName);
        newJob.setFilePath(path.toAbsolutePath().toString());
        newJob.setFileHash(fileHash);

        newJob = importJobRepo.save(newJob);
        importJobProducer.publish(jobId);

        JobResponse jobResponse = new JobResponse();
        jobResponse.setJobId(jobId);
        jobResponse.setJobStatus(newJob.getJobStatus());

        return jobResponse;
    }

    private String calculateFileHash(MultipartFile file) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] result = messageDigest.digest(file.getBytes());

            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JobResponse status(UUID jobId){

        ImportJob importJob = importJobRepo.findById(jobId).orElseThrow();
        return new JobResponse(importJob);
    }
}
