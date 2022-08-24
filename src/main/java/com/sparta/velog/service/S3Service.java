package com.sparta.velog.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@NoArgsConstructor
@Service
public class S3Service {
    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretkey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public String uploadImage(MultipartFile file) {

        String fileName = UUID.randomUUID() + "-" + Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        try {
            // 확장자 점검
            if (!(fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
                throw new IllegalArgumentException("bmp,jpg,jpeg,png 형식의 이미지 파일이 요구됨.");
            }
            // 파일 업로드
            s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new IllegalArgumentException("S3 Bucket 객체 업로드 실패.");
        }
        return s3Client.getUrl(bucket, fileName).toString();    ///url string 리턴
    }

    public List<DeleteObjectsRequest.KeyVersion> getImageKeys() {
        ListObjectsV2Result result = s3Client.listObjectsV2(bucket);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        List<DeleteObjectsRequest.KeyVersion> imageNameList = new CopyOnWriteArrayList<>();
        for (S3ObjectSummary os : objects) {
            imageNameList.add(new DeleteObjectsRequest.KeyVersion(os.getKey()));
        }
        return imageNameList;
    }

    public void deleteObject(String sourceKey) {
        s3Client.deleteObject(bucket, sourceKey);
    }

    public void deleteObjects(List<DeleteObjectsRequest.KeyVersion> object_keys) {
        DeleteObjectsRequest dor = new DeleteObjectsRequest(bucket)
                .withKeys(object_keys);
        try {
            s3Client.deleteObjects(dor);    // exception 처리.
        } catch (AmazonServiceException e) {
            log.error("s3 Objects 삭제 도중 AmazonServiceException 발생");
            System.err.println(e.getErrorMessage());
            throw new IllegalArgumentException("S3 Bucket 객체 삭제 실패.");
        }
    }
}