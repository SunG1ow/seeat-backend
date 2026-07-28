package com.seeat.seeatapi.domain.product.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class FileStorageService {

    private static final String MOCK_CDN_BASE_URL = "https://cdn.seeat.com";

    // TODO: 실제 AWS S3 연동 시 이 메서드 내부만 교체 (업로드 흐름/호출부는 변경 없음)
    public String upload(MultipartFile file, String directory) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        return MOCK_CDN_BASE_URL + "/" + directory + "/" + fileName;
    }
}