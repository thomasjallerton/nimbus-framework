package services

import com.nimbusframework.nimbuscore.persisted.NimbusState
import org.apache.maven.plugin.logging.Log
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.File
import java.lang.Exception
import java.net.URI
import java.util.regex.Pattern

class S3Service(
        region: Region,
        private val config: NimbusState,
        private val logger: Log
) {

    private val s3Client = S3Client.builder()
            .region(region)
            .build()

    private val fileService: FileService = FileService(logger)

    fun readFileFromS3(bucketName: String, s3Path: String): String {
        return try {
            val file = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(s3Path).build())
            file.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun uploadFileToCompilationFolder(bucketName: String, filePath: String, s3Path: String): Boolean {
        val finalPath = "nimbus/${config.projectName}/" + config.compilationTimeStamp + "/" + s3Path
        return uploadFileToS3(bucketName, filePath, finalPath)
    }

    fun uploadFileToS3(bucketName: String, filePath: String, s3Path: String, substitutionVariables: Map<String, String?> = mapOf(), substitutionRegex: String = ""): Boolean {
        logger.info("Uploading $filePath to ${bucketName}/$s3Path")
        try {
            //Upload to S3
            val compiledPattern = Pattern.compile(substitutionRegex)
            val matcher: (String) -> Boolean = if (substitutionRegex.isNotEmpty()) {
                { compiledPattern.matcher(it).matches() }
            } else {
                { false }
            }
            val file = File(filePath)
            if (file.isFile) {
                if (matcher(file.name)) {
                    val putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Path)
                        .build()
                    s3Client.putObject(putObjectRequest, RequestBody.fromFile(fileService.replaceInFile(substitutionVariables, file)))
                } else {
                    val putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Path)
                        .build()
                    s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))
                }
            } else if (file.isDirectory){
                val newPath = if (s3Path.endsWith("/") || s3Path.isEmpty()) {
                    s3Path
                } else {
                    "$s3Path/"
                }
                uploadDirectoryToS3(bucketName, file, newPath, substitutionVariables, matcher)
                logger.info("Successfully uploaded directory $filePath")
            }
            return true
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            logger.error(e)
            e.printStackTrace()
        }
        return false
    }

    fun uploadStringToS3(bucketName: String, contents: String, s3Path: String): Boolean {
        try {
            //Upload to S3
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Path)
                .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromString(contents))
            return true
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            logger.error(e)
            e.printStackTrace()
        }
        return false
    }

    private fun uploadDirectoryToS3(bucketName: String, directory: File, s3Path: String, substitutionVariables: Map<String, String?>, matcher: (String) -> Boolean) {
        for (file in directory.listFiles()) {
            val newPath = if (s3Path.isEmpty()) {
                file.name
            } else {
                "$s3Path/${file.name}"
            }

            if (file.isFile) {
                if (matcher(file.name)) {
                    val putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(newPath)
                        .build()
                    s3Client.putObject(putObjectRequest, RequestBody.fromFile(fileService.replaceInFile(substitutionVariables, file)))
                } else {
                    val putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(newPath)
                        .build()
                    s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))
                }
            } else if (file.isDirectory){
                uploadDirectoryToS3(bucketName, file, newPath, substitutionVariables, matcher)
            }
        }
    }

    fun getUri(bucketName: String, s3Path: String): URI {
        val key = "nimbus/${config.projectName}/" + config.compilationTimeStamp + "/" + s3Path
        return s3Client.utilities().getUrl { it.bucket(bucketName).key(key) }.toURI()
    }

    fun deleteBucket(bucketName: String) {
        try {

            // Delete all objects from the bucket. This is sufficient
            // for unversioned buckets. For versioned buckets, when you attempt to delete objects, Amazon S3 inserts
            // delete markers for all objects, but doesn't delete the object versions.
            // To delete objects from versioned buckets, delete all of the object versions before deleting
            // the bucket (see below for an example).
            val listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build()
            s3Client.listObjectsV2Paginator(listObjectsRequest).stream().forEach { response ->
                response.contents().forEach { s3Object ->
                    val deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Object.key())
                        .build()
                    s3Client.deleteObject(deleteObjectRequest)
                }
            }

            // Delete all object versions (required for versioned buckets).
            val listObjectVersionsRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .build()
            s3Client.listObjectVersionsPaginator(listObjectVersionsRequest).stream().forEach { response ->
                response.versions().forEach { version ->
                    val deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(version.key())
                        .versionId(version.versionId())
                        .build()
                    s3Client.deleteObject(deleteObjectRequest)
                }
                response.deleteMarkers().forEach { deleteMarker ->
                    val deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(deleteMarker.key())
                        .versionId(deleteMarker.versionId())
                        .build()
                    s3Client.deleteObject(deleteObjectRequest)
                }
            }

            // After all objects and object versions are deleted, delete the bucket.
            val deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build()
            s3Client.deleteBucket(deleteBucketRequest)
        } catch (e: S3Exception) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace()
        } catch (e: SdkClientException) {
            // Amazon S3 couldn't be contacted for a response, or the client couldn't
            // parse the response from Amazon S3.
            e.printStackTrace()
        }

    }
}
