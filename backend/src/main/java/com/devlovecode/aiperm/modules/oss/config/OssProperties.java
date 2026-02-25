package com.devlovecode.aiperm.modules.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** 存储类型：local / aliyun */
    private String storageType = "local";

    private Local local = new Local();
    private Aliyun aliyun = new Aliyun();

    @Data
    public static class Local {
        private String path = "./uploads";
        private String accessUrl = "http://localhost:8080/files";
    }

    @Data
    public static class Aliyun {
        private String endpoint;
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String accessUrl;
    }
}
