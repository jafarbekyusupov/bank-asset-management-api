package com.bank.assets.common.response;
import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
    List<T> items,
    int currentPage,
    int itemsPerPage,
    long totalItems,
    int totalPages,
    boolean hasNextPage,
    boolean hasPreviousPage
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
