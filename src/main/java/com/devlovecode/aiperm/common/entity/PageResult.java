package com.devlovecode.aiperm.common.entity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 *
 * @param <T> 数据类型
 * @author DevLoveCode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码")
    private Long current;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Long size;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Long pages;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    /**
     * 从MyBatis-Plus的Page对象构建分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords());
        return result;
    }

    /**
     * 手动构建分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setPages((total + size - 1) / size);
        result.setRecords(records);
        return result;
    }

    /**
     * 空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(1L, 10L, 0L, 0L, List.of());
    }
}
