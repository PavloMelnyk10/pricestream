package com.pricestream.core.domain;

import java.util.List;
import lombok.Builder;

/**
 * Core domain representation of a paginated result.
 */
@Builder
public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
