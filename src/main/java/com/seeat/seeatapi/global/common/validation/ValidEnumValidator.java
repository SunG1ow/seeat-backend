package com.seeat.seeatapi.global.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ValidEnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValidEnum annotation) {
        this.enumClass = annotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 필수 여부는 @NotBlank가 별도로 검증
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .anyMatch(name -> name.equals(value));
    }
}