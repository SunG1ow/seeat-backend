package com.seeat.seeatapi.global.common;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// 테스트 전용: 실제 네트워크 호출 없이 가짜 URL만 반환 (Cloudinary 실호출 방지)
@Component
@Profile("test")
public class MockFileStorageService implements FileStorageService {

    private static final String MOCK_CDN_BASE_URL = "https://mock-cdn.test";

    @Override
    public String upload(MultipartFile file, String directory) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        return MOCK_CDN_BASE_URL + "/" + directory + "/" + fileName;
    }
}