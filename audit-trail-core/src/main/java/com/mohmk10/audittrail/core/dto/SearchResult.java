package com.mohmk10.audittrail.core.dto;

import java.util.List;

public record SearchResult<T>(
        List<T> items,
        long totalCount,
        int page,
        int size,
        int totalPages
) {
    public static <T> SearchResult<T> of(List<T> items, long totalCount, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalCount / size);
        return new SearchResult<>(items, totalCount, page, size, totalPages);
    }

    public static <T> SearchResult<T> empty(int page, int size) {
        return new SearchResult<>(List.of(), 0, page, size, 0);
    }
}
