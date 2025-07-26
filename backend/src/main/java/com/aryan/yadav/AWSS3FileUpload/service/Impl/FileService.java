package com.aryan.yadav.AWSS3FileUpload.service.Impl;

import org.springframework.data.domain.Sort;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.aryan.yadav.AWSS3FileUpload.entity.File;
import com.aryan.yadav.AWSS3FileUpload.repository.FileRepository;
import com.aryan.yadav.AWSS3FileUpload.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileService implements IFileService {

    @Autowired
    private FileRepository fileRepository;

    @Value("${aws.s3.access.key}")
    private String awsS3AccessKey;
    @Value("${aws.s3.secret.key}")
    private String awsS3SecretKey;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Override
    public File saveFile(MultipartFile file, String name) {
        try {
            logger.info("Starting file upload for: {}", file.getOriginalFilename());
            String saveFileUrl = saveFileToAWSS3Bucket(file);
            logger.info("File uploaded to S3 successfully: {}", saveFileUrl);

            File fileToSave = new File();
            fileToSave.setFileUrl(saveFileUrl);
            fileToSave.setName(name);
            
            File savedFile = fileRepository.save(fileToSave);
            logger.info("File metadata saved to database with ID: {}", savedFile.getId());
            
            return savedFile;
        } catch (Exception e) {
            logger.error("Error saving file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<File> getAllFiles() {
        return fileRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        // First get the file from database
        File file = fileRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id: " + id));
        
        try {
            // Extract the file key from the URL
            String fileUrl = file.getFileUrl();
            String fileKey = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            
            // Initialize S3 client
            AwsBasicCredentials credentials = AwsBasicCredentials.create(awsS3AccessKey, awsS3SecretKey);
            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_SOUTH_1)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
            
            // Delete from S3
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
                    
            s3Client.deleteObject(deleteObjectRequest);
            
            // Delete from database
            fileRepository.delete(file);
            
            logger.info("Successfully deleted file with id: {} from S3 and database", id);
            
        } catch (NoSuchKeyException e) {
            logger.warn("File not found in S3, deleting from database only. File ID: {}", id);
            fileRepository.delete(file);
        } catch (Exception e) {
            logger.error("Error deleting file with id: {}", id, e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error deleting file: " + e.getMessage(), 
                e
            );
        }
    }

    private String saveFileToAWSS3Bucket(MultipartFile file) {
        try {
            String s3FileName = file.getOriginalFilename();

            AwsBasicCredentials credentials = AwsBasicCredentials.create(awsS3AccessKey, awsS3SecretKey);

            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_SOUTH_1)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            try (InputStream inputStream = file.getInputStream()) {
                
                // Create the request without ACL since bucket has ACLs disabled
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3FileName)
                        .contentType(file.getContentType())
                        .build();

                s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                        inputStream, file.getSize()));
            }
            return "https://" + bucketName + ".s3.amazonaws.com/" + s3FileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
