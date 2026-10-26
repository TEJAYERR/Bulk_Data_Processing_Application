package com.app.bulk_data_processing.consumer;

import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.JobStatus;
import com.app.bulk_data_processing.entity.User;
import com.app.bulk_data_processing.repository.ImportJobRepo;
import com.app.bulk_data_processing.repository.UserRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ImportJobConsumer {

    private final ImportJobRepo importJobRepo;
    private final UserRepo userRepo;

    public ImportJobConsumer(ImportJobRepo importJobRepo, UserRepo userRepo) {
        this.importJobRepo = importJobRepo;
        this.userRepo = userRepo;
    }

    @RabbitListener(queues = "import.queue")
    public void consume(String jobId){

        UUID id = UUID.fromString(jobId);
        ImportJob importJob = importJobRepo.findById(id).orElseThrow();
        importJob.setJobStatus(JobStatus.PROCESSING);

        importJob = importJobRepo.save(importJob);

        try(BufferedReader bf = new BufferedReader(
                new FileReader(new File(importJob.getFilePath()))
        )) {

            bf.readLine();
            int chunkSize = 25;
            String line;
            List<User> users = new ArrayList<>();

            while ((line = bf.readLine()) != null){

                String userDetails[] = line.split(",");
                String name = userDetails[0];
                String mobileNumber = userDetails[1];

                User newUser = new User();
                newUser.setName(name);
                newUser.setMobileNumber(mobileNumber);

                users.add(newUser);
                
                if(users.size() == chunkSize){
                    userRepo.saveAll(users);
                    userRepo.flush();
                    users.clear();
                }
            }

            if(!users.isEmpty()){
                userRepo.saveAll(users);
            }

            importJob.setJobStatus(JobStatus.COMPLETED);
            importJobRepo.save(importJob);
        }
        catch (IOException e){
            throw new RuntimeException();
        }
    }
}
