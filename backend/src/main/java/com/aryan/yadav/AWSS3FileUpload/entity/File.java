package com.aryan.yadav.AWSS3FileUpload.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    // Explicit getters for JSON serialization
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    public void setName(String name) {
        this.name = name;
    }
}