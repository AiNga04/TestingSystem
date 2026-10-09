package org.vti.jamie.com.project_spring_boot.utils;

import org.springframework.data.domain.*;
import java.util.Map;

public final class PageRequests {
    private PageRequests() {}
    public static Pageable of(int page, int size, String sortBy, String direction,
                              Map<String, String> allowed, String tieBreaker) {
        String property = allowed.get(sortBy);
        if (property == null) throw new IllegalArgumentException("sortBy chỉ nhận: " + String.join(", ", allowed.keySet()));
        Sort.Direction order = Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> new IllegalArgumentException("direction chỉ nhận asc hoặc desc"));
        Sort sort = Sort.by(order, property);
        if (!property.equals(tieBreaker)) sort = sort.and(Sort.by(tieBreaker));
        return PageRequest.of(page, size, sort);
    }
}
