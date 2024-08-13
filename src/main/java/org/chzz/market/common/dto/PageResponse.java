package org.chzz.market.common.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class PageResponse<E> {
    private final boolean hasNext;
    private final List<E> items;
    private final int pageNumber;
    private final int pageSize;
    private final int totalPages;
    private final long totalElements;
    private final boolean isLast;


    public static <E> PageResponse<E> from(final Page<E> page) {
        return new PageResponse<>(page.hasNext(), page.getContent(), page.getNumber(), page.getSize(), page.getTotalPages(), page.getTotalElements(), page.isLast());
    }
}