package com.example.trans_backend_file.util;

import cn.hutool.core.date.DateUtil;

import cn.hutool.core.util.StrUtil;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.google.common.base.Charsets;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Component
@Slf4j
public class MinioUtil {
    @Value("${minio.bucketName}")
    private String bucketName;
    @Resource
    private MinioClient minioClient;
    private static MinioUtil minioUtil;

    public MinioUtil() {
    }

    @PostConstruct
    public void init() {
        minioUtil = this;
    }

    public static String uploadFile(InputStream inputStream, String objectName, String fileName) {
        if (StrUtil.isNotBlank(fileName)) {
            objectName = objectName + "/" + fileName;
        }
        try {
            if (objectName != null && !objectName.isEmpty()) {
                try {
                    minioUtil.minioClient.putObject(PutObjectArgs
                            .builder()
                            .bucket(minioUtil.bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .build());
                    log.info("文件上传成功!");
                } catch (Exception e) {
                    log.error("添加存储对象异常", e);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加存储对象异常");
                }
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"存储对象名称objectName不能为空！");
            }
            log.info("文件上传成功!");
            return minioUtil.getUrl(objectName);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("上传发生错误: {}！", ex.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, ex.getMessage());
        }
    }

    public static InputStream download(String fileUrl) {
        try {
            fileUrl = fileUrl.substring(fileUrl.indexOf(minioUtil.bucketName) + minioUtil.bucketName.length());
            InputStream inputStream = minioUtil.get(fileUrl);
            log.info("下载成功");
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载发生错误: {}！", e.getMessage());
            return null;
        }
    }

    public static void batchDownload(List<String> fileUrlList, String zipName, HttpServletResponse httpServletResponse) {
        ZipOutputStream zos;
        ZipEntry zipEntry;
        byte[] buff = new byte[1024];
        if (fileUrlList != null && !fileUrlList.isEmpty()) {
            try {
                if (StrUtil.isEmpty(zipName)) {
                    zipName = "批量下载" + DateUtil.format(new Date(), "yyyyMMddHHmmss");
                }
                //清除缓冲区中存在的所有数据以及状态代码和标头。如果已提交响应，则此方法将抛出IllegalStateException
                httpServletResponse.reset();
                //Content-Disposition为属性名，attachment以附件方式下载，filename下载文件默认名字
                httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(zipName, Charsets.UTF_8.name()) + ".zip");
                //另存为弹框加载
                httpServletResponse.setContentType("application/x-msdownload");
                httpServletResponse.setCharacterEncoding("utf-8");
                zos = new ZipOutputStream(httpServletResponse.getOutputStream());
                for (String fileUrl : fileUrlList) {
                    //获取minio对应路径文件流（从数据库中获取的url是编码的，这里要先进行解码，不然minioClient.getObject()方法里面会再进行一次编码，就获取不到对象）
                    String url = URLDecoder.decode(fileUrl, Charsets.UTF_8.name());
                    String downloadUrl = url.substring(url.indexOf(minioUtil.bucketName) + minioUtil.bucketName.length());
                    InputStream inputStream = minioUtil.get(downloadUrl);
                    String fileName = url.substring(url.lastIndexOf("/"), url.lastIndexOf("."));
                    zipEntry = new ZipEntry(fileName + url.substring(url.lastIndexOf(".")));
                    zos.putNextEntry(zipEntry);
                    int length;
                    while ((length = inputStream.read(buff)) > 0) {
                        zos.write(buff, 0, length);
                    }
                }
                log.info("批量下载成功!");
                zos.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                log.error("批量下载发生错误! msg=" + ioException.getMessage());
            } finally {
            }
        } else {
            log.error("批量下载发生错误,文件访问路径集合不能为空!");
        }

    }

    public static void removeFile(String fileUrl) {
        try {
            String downloadUrl = fileUrl.substring(fileUrl.indexOf(minioUtil.bucketName) + minioUtil.bucketName.length());
            minioUtil.rm(downloadUrl);
            log.info("文件删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件删除失败! msg=" + e.getMessage());
        }

    }

    public static void batchRemoveFile(List<String> fileUrlList) {
        if (fileUrlList != null && !fileUrlList.isEmpty()) {
            try {
                for (String fileUrl : fileUrlList) {
                    String downloadUrl = fileUrl.substring(fileUrl.indexOf(minioUtil.bucketName) + minioUtil.bucketName.length());
                    minioUtil.rm(downloadUrl);
                }
                log.info("文件批量删除成功!");
            } catch (Exception e) {
                e.printStackTrace();
                log.error("文件批量删除失败! msg=" + e.getMessage());
            }
        }
    }

    public static InputStream get(String objectName) {
        InputStream inputStream;
        try {
            inputStream = minioUtil.minioClient.getObject(GetObjectArgs.builder().bucket(minioUtil.bucketName).object(objectName).build());
            return inputStream;
        } catch (Exception e) {
            log.error("读取存储对象异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"读取存储对象异常");
        }
    }

    public void download(String objectName, String fileName) {
        try {
            this.minioClient.downloadObject(DownloadObjectArgs.builder().bucket(this.bucketName).object(objectName).filename(fileName).build());
        } catch (Exception e) {
            log.error("下载发生错误:[{}]", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载发生错误");
        }
    }

    public String getUrl(String objectName) {
        try {
            return this.minioClient.getObjectUrl(this.bucketName, objectName);
        } catch (Exception e) {
            log.error("获取存储对象url异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取存储对象url异常");
        }
    }

    public void rm(String objectName) {
        try {
            this.minioClient.removeObject(RemoveObjectArgs.builder().bucket(this.bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("删除存储对象异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除存储对象异常");
        }
    }

    public void rmBatch(Collection<String> objectNames) {
        if (!CollectionUtils.isEmpty(objectNames)) {
            objectNames.forEach(this::rm);
        }

    }

}