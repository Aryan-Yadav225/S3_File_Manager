package com.aryan.yadav.AWSS3FileUpload.controller;

import com.aryan.yadav.AWSS3FileUpload.entity.File;
import com.aryan.yadav.AWSS3FileUpload.service.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Object> saveFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name) {
        
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("File is required"));
        }
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Name is required"));
        }
        
        try {
            File savedFile = fileService.saveFile(file, name);
            return ResponseEntity.ok(createSuccessResponse(savedFile));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }
    
    private Map<String, Object> createSuccessResponse(File file) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "File uploaded successfully");
        response.put("data", Map.of(
            "id", file.getId(),
            "name", file.getName(),
            "fileUrl", file.getFileUrl()
        ));
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File deleted successfully");
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(createErrorResponse(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete file: " + e.getMessage()));
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<File>> getAllFiles(){
        return ResponseEntity.ok(fileService.getAllFiles());
    }
}