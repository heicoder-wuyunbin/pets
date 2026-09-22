package com.pets.common.web;

public record PageResult(int page_num, int page_size, long total) {
    public static PageResult of(int pageNum, int pageSize, long total) {
        return new PageResult(pageNum, pageSize, total);
    }
}
