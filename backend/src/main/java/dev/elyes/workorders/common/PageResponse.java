package dev.elyes.workorders.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** A small, stable JSON shape for paged results instead of serialising Spring's Page directly. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
            page.getContent().stream().map(mapper).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages());
    }
}
