package com.example.trans_backend_file.config;

import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * minio自动配置.
 *
 * @author Mei rx
 * @since 2021/08/05
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@ConditionalOnClass({MinioClient.class})
@ConditionalOnProperty({"minio.endpoint"})
public class MinioAutoConfiguration {

    @Autowired
    private Environment environment;

    public MinioAutoConfiguration() {
    }

    @Bean
    public MinioClient minioClient() {
        String endpoint = this.environment.getProperty("minio.endpoint");
        String accessKey = this.environment.getProperty("minio.accessKey");
        String secretKey = this.environment.getProperty("minio.secretKey");
        String bucketName = this.environment.getProperty("minio.bucketName");
        if (endpoint != null && !endpoint.isEmpty()) {
            if (accessKey != null && !accessKey.isEmpty()) {
                if (secretKey != null && !secretKey.isEmpty()) {
                    if (bucketName != null && !bucketName.isEmpty()) {
                        MinioClient minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
                        this.makeBucket(minioClient, bucketName);
                        return minioClient;
                    } else {
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Minio存储桶名称未在application.yml配置！");
                    }
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Minio密码未在application.yml配置！");
                }
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Minio用户名未在application.yml配置！");
            }
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Minio的URL未在application.yml配置！");
        }
    }

    private void makeBucket(MinioClient minioClient, String bucketName) {
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String policyJson = "Bucket already exists.";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policyJson).build());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建minio存储桶异常");
        }
    }
}
