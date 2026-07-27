package com.seeat.seeatapi.global.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessRegistrationNumber {
    String message() default "유효하지 않은 사업자등록번호 형식입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}