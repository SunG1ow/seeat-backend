package com.seeat.seeatapi.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.function.LongSupplier;

public class PageableExecutionUtilsWrapper {
    public static <T> Page<T> getPage(List<T> content, Pageable pageable, LongSupplier totalSupplier) {
        return PageableExecutionUtils.getPage(content, pageable, totalSupplier);
    }
}