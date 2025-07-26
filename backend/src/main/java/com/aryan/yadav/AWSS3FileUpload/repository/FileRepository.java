package com.aryan.yadav.AWSS3FileUpload.repository;

import com.aryan.yadav.AWSS3FileUpload.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
