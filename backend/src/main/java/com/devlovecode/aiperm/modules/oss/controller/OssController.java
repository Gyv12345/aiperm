package com.devlovecode.aiperm.modules.oss.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.devlovecode.aiperm.common.annotation.Log;
import com.devlovecode.aiperm.common.domain.R;
import com.devlovecode.aiperm.common.enums.OperType;
import com.devlovecode.aiperm.modules.oss.domain.OssResult;
import com.devlovecode.aiperm.modules.oss.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/oss")
@SaCheckLogin
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @Operation(summary = "上传文件")
    @Log(title = "文件管理", operType = OperType.UPLOAD)
    @PostMapping("/upload")
    public R<OssResult> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(ossService.upload(file));
    }

    @Operation(summary = "删除文件")
    @Log(title = "文件管理", operType = OperType.DELETE)
    @DeleteMapping
    public R<Void> delete(@RequestParam String fileName) {
        ossService.delete(fileName);
        return R.ok();
    }
}
