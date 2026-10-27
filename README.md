# Bulk Data Processing Application

A Spring Boot application that processes uploaded CSV files asynchronously using RabbitMQ. It accepts an upload, creates a processing job, and returns a job ID so the client can check the processing status without waiting for the entire file to finish.

## Features

- **Asynchronous processing:** RabbitMQ decouples file uploads from background processing.
- **Job tracking:** Each upload receives a unique job ID and a processing status.
- **CSV validation:** Validates user names and mobile numbers.
- **Batch processing:** Processes valid records in batches.
- **Error tracking:** Stores row-level validation errors for later inspection.
- **Duplicate detection:** Rejects identical file contents using a SHA-256 hash and checks for duplicate mobile numbers.
- **Failure tracking:** Records interrupted processing jobs.
- **Idempotency checks:** Checks whether a row has already been processed within a job.

## Tech Stack

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- RabbitMQ
- Maven
- Docker Compose

## Architecture

```text
Client
  |
  | POST /upload (CSV file)
  v
Spring Boot API
  |
  |-- Save CSV to local disk
  |-- Create job in PostgreSQL
  |-- Publish jobId to RabbitMQ
  |
  v
Return jobId and initial status

RabbitMQ --> Background Consumer
                  |
                  |-- Read CSV
                  |-- Validate rows
                  |-- Save valid records in batches
                  |-- Store row-level errors
                  |-- Update job status
                  v
              PostgreSQL

Client --> GET /job?jobId=... --> Current status and errors
```

RabbitMQ carries the job ID, not the CSV contents. The worker reads the uploaded file from the local filesystem using the path stored in PostgreSQL.

## Prerequisites

Install the following before running the project:

- [Git](https://git-scm.com/)
- [Java 21 JDK](https://adoptium.net/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [PostgreSQL](https://www.postgresql.org/download/)

Maven is included through the Maven Wrapper (`mvnw.cmd`), so a separate Maven installation is not required.

## Run Locally

### 1. Clone the repository

```bash
git clone https://github.com/TEJAYERR/Bulk_Data_Processing_Application.git
cd Bulk_Data_Processing_Application
```

### 2. Create the PostgreSQL database

Start PostgreSQL and connect using your preferred PostgreSQL client or `psql`.

Create the database:

```sql
CREATE DATABASE bulk_data_processing_application;
```

The default application configuration uses:

- Host: `localhost`
- Port: `5432`
- Database: `bulk_data_processing_application`
- Username: `postgres`

Update `src/main/resources/application.properties` to match your local PostgreSQL username and password:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bulk_data_processing_application
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

Replace `YOUR_POSTGRES_PASSWORD` with your actual local database password.

Hibernate is configured with `ddl-auto=update`, so it can create or update entity tables when the application starts. The database itself must exist first.

### 3. Start RabbitMQ

Docker Compose is used to run RabbitMQ; the Spring Boot application runs directly on your machine.

From the repository root, run:

```bash
docker compose up -d
```

This starts RabbitMQ Management with the following ports:

| Service | Address |
|---|---|
| RabbitMQ messaging | `localhost:5672` |
| RabbitMQ Management UI | http://localhost:15672 |

The default development credentials are:

- Username: `guest`
- Password: `guest`

Open the Management UI to verify that RabbitMQ is running.

To stop RabbitMQ later:

```bash
docker compose down
```

### 4. Configure the CSV upload directory

The current implementation uses a hardcoded Windows directory in `ImportJobService`:

```java
String dir = "D:\\projects\\bulk-data-processing\\uploads";
```

Before running the application, change this directory to a writable location on your computer. For example, on Windows:

```java
String dir = "C:\\bulk-data-processing\\uploads";
```

Create the directory if necessary. The application also attempts to create its parent directory when saving an upload.

**Important:** The CSV files are stored on your local filesystem, not inside RabbitMQ or PostgreSQL. The configured directory must remain accessible to the application while jobs are being processed.

### 5. Run the Spring Boot application

On Windows PowerShell, run:

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatively, open the project in IntelliJ IDEA and run the Spring Boot main application class.

The API starts on:

```text
http://localhost:8080
```

Make sure PostgreSQL and RabbitMQ are running before starting the application.

## API Endpoints

Base URL: `http://localhost:8080`

### 1. Upload a CSV file

**Endpoint:** `POST /upload`

**Content type:** `multipart/form-data`

The multipart field name is `multipartFile`.

Using Windows PowerShell:

```powershell
curl.exe -X POST "http://localhost:8080/upload" `
  -F "multipartFile=@test-data/sample.csv"
```

Replace `test-data/sample.csv` with the path to your CSV file.

The API returns a job ID and its initial status. Save the returned `jobId` to check the job later.

### 2. Check job status

**Endpoint:** `GET /job?jobId={jobId}`

Example:

```powershell
curl.exe "http://localhost:8080/job?jobId=YOUR_JOB_ID"
```

Replace `YOUR_JOB_ID` with the UUID returned by the upload endpoint.

The response includes job information and any recorded row-level errors.

## CSV Format

The processor expects a header row followed by user records. Use the following format:

```csv
name,mobileNumber
Alice,9876543210
Bob,9123456780
Charlie,9988776655
```

The first row is treated as the header and is skipped.

Current validation rules include:

- Names must not contain digits.
- Mobile numbers must contain only digits.
- Duplicate mobile numbers are rejected.
- Valid records are saved in batches.

Keep the column order as `name,mobileNumber`. The current parser uses simple comma-separated splitting, so fields containing commas are not supported.

A sample dataset is available in the repository's `test-data` directory.

## Job Statuses

The application tracks job progress using status values such as:

| Status | Meaning |
|---|---|
| `ACCEPTED` | The job has been created and accepted for processing. |
| `PROCESSING` | The background consumer is processing the file. |
| `COMPLETED` | Processing finished, including the recording of any row-level validation errors. |
| `INTERRUPTED` | Processing encountered an unexpected failure. |
| `FAILED` | Failure status available for the job lifecycle. |
| `PARTIALLY_COMPLETED` | Status available for jobs that require partial-completion semantics. |

Check the returned status rather than assuming the job has finished immediately after uploading. For larger files, poll the status endpoint until processing finishes or is interrupted.

## Testing the Application

1. Start PostgreSQL.
2. Start RabbitMQ with Docker Compose.
3. Run the Spring Boot application.
4. Upload the sample CSV.
5. Copy the returned job ID.
6. Call the job status endpoint to inspect progress and recorded errors.
7. Upload the same file again to observe duplicate-file rejection.

You can also modify a sample CSV to include invalid names or duplicate mobile numbers and inspect the stored errors through the status endpoint.

## Project Structure

```text
src/
└── main/
    ├── java/com/app/bulk_data_processing/
    │   ├── config/       # RabbitMQ configuration
    │   ├── controller/   # REST endpoints
    │   ├── consumer/     # Asynchronous job processing
    │   ├── producer/     # Publishing job IDs
    │   ├── entity/       # JPA entities and job statuses
    │   ├── repository/   # Database access
    │   └── service/      # Upload and job management
    └── resources/
        └── application.properties

test-data/                # Sample CSV files
uploads/                  # Local file storage directory
docker-compose.yaml       # RabbitMQ service
pom.xml                   # Maven dependencies and build configuration
```

## Key Learning Outcomes

This project explores practical backend engineering concepts:

- Asynchronous job processing with message queues
- Separating request handling from long-running work
- Persisting job state and processing errors
- Batch database writes
- CSV validation and duplicate detection
- Retry-safe processing through row-level idempotency checks
- Tracking failures in an asynchronous workflow

## Author

**Teja Yerriboyina**

GitHub: [TEJAYERR](https://github.com/TEJAYERR)
