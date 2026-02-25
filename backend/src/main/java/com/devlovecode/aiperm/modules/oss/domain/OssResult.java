package com.devlovecode.aiperm.modules.oss.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssResult {
    /** 存储文件名（UUID生成）*/
    private String fileName;
    /** 原始文件名 */
    private String originalName;
    /** 文件访问URL */
    private String url;
    /** 文件大小（字节）*/
    private Long size;
    /** MIME类型 */
    private String contentType;
}
