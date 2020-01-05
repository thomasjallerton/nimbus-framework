package models;

import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition;

@FileStorageBucketDefinition(
    bucketName = "website",
    staticWebsite = true
)
public class Website {}
