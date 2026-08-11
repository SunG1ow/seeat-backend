package com.seeat.seeatapi.global.common;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

// 운영/로컬 환경: 실제 Cloudinary 업로드 (test 프로필에서는 제외되고 MockFileStorageService가 대신 등록됨)
@Component
@Profile("!test")
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryFileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        try {
            String publicId = directory + "/" + UUID.randomUUID();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", directory,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            return (String) result.get("secure_url");
        } catch (Exception e) {
            // Cloudinary SDK는 IOException 외에도 com.cloudinary.api.exceptions.* 계열의
            // checked exception을 던질 수 있어 Exception으로 폭넓게 처리
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
}