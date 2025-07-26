package com.aryan.yadav.AWSS3FileUpload.service;

import com.aryan.yadav.AWSS3FileUpload.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileService {
    File saveFile(MultipartFile file, String name);
    List<File> getAllFiles();
    void deleteFile(Long id);
}
