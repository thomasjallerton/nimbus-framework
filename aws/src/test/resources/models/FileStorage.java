package models;

import com.nimbusframework.nimbuscore.annotations.deployment.FileUpload;
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition;

@FileStorageBucketDefinition(bucketName = "ImageBucket")
@FileUpload(fileStorageBucket = FileStorage.class, localPath = "test", targetPath = "test")
public class FileStorage {}
