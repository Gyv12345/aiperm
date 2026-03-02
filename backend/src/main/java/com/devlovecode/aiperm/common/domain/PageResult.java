package com.devlovecode.aiperm.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页结果封装
 *
 * @param <T> 数据类型
 * @author devlovecode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "当前页码")
    private Long pageNum;

    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "总页数")
    private Long pages;

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> list, Long pageNum, Long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setList(list);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(pageSize > 0 ? (total + pageSize - 1) / pageSize : 0L);
        return result;
    }

    /**
     * 构建空分页结果
     */
    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return of(0L, Collections.emptyList(), pageNum, pageSize);
    }

    /**
     * 转换列表元素类型
     */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedList = this.list.stream()
                .map(mapper)
                .collect(Collectors.toList());
        return of(this.total, mappedList, this.pageNum, this.pageSize);
    }
}
