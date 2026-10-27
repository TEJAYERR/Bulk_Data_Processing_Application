package com.app.bulk_data_processing.consumer;

import com.app.bulk_data_processing.entity.ImportJob;
import com.app.bulk_data_processing.entity.ImportJobError;
import com.app.bulk_data_processing.entity.JobStatus;
import com.app.bulk_data_processing.entity.User;
import com.app.bulk_data_processing.repository.ImportJobErrorRepo;
import com.app.bulk_data_processing.repository.ImportJobRepo;
import com.app.bulk_data_processing.repository.UserRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
public class ImportJobConsumer {

    private final ImportJobRepo importJobRepo;
    private final UserRepo userRepo;
    private final ImportJobErrorRepo importJobErrorRepo;

    public ImportJobConsumer(ImportJobRepo importJobRepo, UserRepo userRepo, ImportJobErrorRepo importJobErrorRepo) {
        this.importJobRepo = importJobRepo;
        this.userRepo = userRepo;
        this.importJobErrorRepo = importJobErrorRepo;
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
            HashSet<String> mobileNumbers = new HashSet<>();
            List<ImportJobError> errors = new ArrayList<>();
            int errorLimit = 25;

            long rowNumber = 1;

            while ((line = bf.readLine()) != null){

                String userDetails[] = line.split(",");
                String name = userDetails[0];
                String mobileNumber = userDetails[1];

                //verify user details and also we can check the idempotent keys for the
                //uniqueness of duplicate and all

                if(isRowAlreadyProcessed(id, rowNumber)) {

                    System.out.println("already processed this row - " + rowNumber + " in Job - " + id);
                }
                else if(!validateUserDetails(name, mobileNumber, importJob, rowNumber, mobileNumbers, errors)) {

                    //List<error> may be and at the for importJob
                    // I will set the errors and update it for the clients
                    if(errors.size() == errorLimit){
                        importJobErrorRepo.saveAll(errors);
                        importJobErrorRepo.flush();
                        errors.clear();
                    }
                }
                else {

                    User newUser = new User(name, mobileNumber, id, rowNumber);

                    users.add(newUser);
                    mobileNumbers.add(mobileNumber);

                    if (users.size() == chunkSize) {
                        userRepo.saveAll(users);
                        userRepo.flush();
                        users.clear();
                        mobileNumbers.clear();
                        Thread.sleep(15000);
                    }
                }

                rowNumber++;
            }

            if(!users.isEmpty()){
                userRepo.saveAll(users);
            }

            if(!errors.isEmpty()){
                importJobErrorRepo.saveAll(errors);
            }

            importJob.setJobStatus(JobStatus.COMPLETED);
            importJobRepo.save(importJob);
            importJobRepo.flush();
        }
        catch (Exception e){
            importJob.setJobStatus(JobStatus.INTERRUPTED);
            importJobRepo.save(importJob);

            throw new RuntimeException(e);
        }
    }

    private boolean isRowAlreadyProcessed(UUID jobId, long rowNumber) {

        return userRepo.existsByJobIdAndRowNumber(jobId, rowNumber);
    }

    private boolean validateUserDetails(String name,
                                        String mobileNumber,
                                        ImportJob importJob,
                                        long rowNumber,
                                        HashSet<String> mobileNumbers,
                                        List<ImportJobError> errors) {

        //validate user no digits in name
        if(!name.matches("^[^0-9]*$")){

            errors.add(
                    new ImportJobError(importJob,
                            rowNumber,
                            "username contains digits so cannot process the row"
                    )
            );
            return false;
        }

        //validate mobileNumber No alphabets in number
        if(!mobileNumber.matches("^[0-9]+$")){

            errors.add(
                    new ImportJobError(importJob,
                            rowNumber,
                            "mobileNumber contains alphabets or special chars which are nit allowed so cannot process the row"
                    )
            );
            return false;
        }

        //verify if the mobilNumber already exists in the db
        if(userRepo.existsByMobileNumber(mobileNumber) || mobileNumbers.contains(mobileNumber)){

            errors.add(
                    new ImportJobError(importJob,
                            rowNumber,
                            "duplicate record insertion failed !"
                    )
            );
            return false;
        }

        return true;

        //I think idempotency is slightly feeling like not needed I am not sure but if we need it we can add just a check only right no worries I think iti helps to understand the concept so I will let it stay
    }
}
