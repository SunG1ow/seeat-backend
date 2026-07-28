package com.seeat.seeatapi.global.response;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {

    private final List<T> content;
    private final PageInfo page;

    private PageResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }

    // Spring Data Page<T>를 명세서 포맷(content + page)으로 변환
    public static <T> PageResponse<T> of(Page<T> springPage) {
        PageInfo pageInfo = new PageInfo(
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
        return new PageResponse<>(springPage.getContent(), pageInfo);
    }

    public List<T> getContent() {
        return content;
    }

    public PageInfo getPage() {
        return page;
    }

    public static class PageInfo {
        private final int number;
        private final int size;
        private final long totalElements;
        private final int totalPages;

        public PageInfo(int number, int size, long totalElements, int totalPages) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getNumber() {
            return number;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }
}