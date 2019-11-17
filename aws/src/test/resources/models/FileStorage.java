package models;

import com.nimbusframework.nimbuscore.annotations.deployment.FileUpload;
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucket;

@FileStorageBucket(bucketName = "ImageBucket")
@FileUpload(bucketName = "ImageBucket", localPath = "test", targetPath = "test")
public class FileStorage {}
