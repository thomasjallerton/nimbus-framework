package models;

import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucket;

@FileStorageBucket(
    bucketName = "website",
    staticWebsite = true
)
public class Website {}
