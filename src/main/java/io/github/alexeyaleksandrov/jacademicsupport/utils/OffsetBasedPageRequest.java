package io.github.alexeyaleksandrov.jacademicsupport.utils;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Custom Pageable implementation for offset-based pagination.
 * Unlike page-based pagination, this allows precise control over the starting position.
 */
public class OffsetBasedPageRequest implements Pageable {

    private final int limit;
    private final int offset;
    private final Sort sort;

    public OffsetBasedPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    public OffsetBasedPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one");
        }
        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest((int) (getOffset() + getPageSize()), getPageSize(), getSort());
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() 
            ? new OffsetBasedPageRequest((int) (getOffset() - getPageSize()), getPageSize(), getSort())
            : first();
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest(pageNumber * getPageSize(), getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
