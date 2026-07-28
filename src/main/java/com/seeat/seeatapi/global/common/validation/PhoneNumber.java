package com.seeat.seeatapi.global.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {

    String message() default "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}