package com.seeat.seeatapi.global.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    // 010-1234-5678 또는 01012345678 형식 허용
    private static final Pattern PATTERN = Pattern.compile("^01[016789]-?\\d{3,4}-?\\d{4}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // 필수 여부는 @NotBlank가 별도로 검증
        }
        return PATTERN.matcher(value).matches();
    }
}