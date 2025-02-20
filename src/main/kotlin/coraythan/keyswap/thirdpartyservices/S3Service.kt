package coraythan.keyswap.thirdpartyservices

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3control.model.S3ObjectMetadata
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.http.engine.crt.CrtHttpEngine
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class S3Service(
    @Value("\${aws-access-key}")
    private val awsAccesskey: String,
    @Value("\${aws-secret-key}")
    private val awsSecretkey: String,

    private val restTemplate: RestTemplate,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val userContentBucket = "dok-user-content"
        private const val cardImagesBucket = "keyforge-card-images"
        const val cardImagesFolder = "card-images-houses"

        fun userContentUrl(key: String) = "https://$userContentBucket.s3-us-west-2.amazonaws.com/$key"
    }

    private fun s3Client(): S3Client = S3Client {
        httpClient = CrtHttpEngine()
        region = "us-west-2"
        credentialsProvider = StaticCredentialsProvider(
            credentials = Credentials(
                accessKeyId = awsAccesskey,
                secretAccessKey = awsSecretkey
            )
        )
    }

    fun addDeckImage(deckImage: MultipartFile, deckId: Long, userId: UUID, extension: String): String {
        return addImage(deckImage, "deck-ownership", "$deckId-$userId", extension)
    }

    fun addStoreIcon(storeIcon: MultipartFile, userId: UUID, extension: String): String {
        return addImage(storeIcon, "stores", "$userId-icon", extension)
    }

    fun addStoreBanner(storeBanner: MultipartFile, userId: UUID, extension: String): String {
        return addImage(storeBanner, "stores", "$userId-banner", extension)
    }

    fun addGenericUserImg(img: MultipartFile, extension: String): String {
        return addImage(img, "user-imgs", UUID.randomUUID().toString(), extension)
    }

    fun addTeamImg(img: MultipartFile, extension: String, teamId: UUID): String {
        return addImage(img, "teams", teamId.toString(), extension)
    }

    fun deleteUserContent(objKey: String) {
        runBlocking {
            s3Client().use { s3 ->
                s3.deleteObject(DeleteObjectRequest {
                    bucket = userContentBucket
                    key = objKey
                })
            }
        }
    }

    private fun cardObjKey(cardUrl: String) = "$cardImagesFolder/$cardUrl.png"

    fun checkIfCardImageExists(cardUrl: String): Boolean {

        val objKey = cardObjKey(cardUrl)
        try {
            restTemplate.headForHeaders("https://keyforge-card-images.s3-us-west-2.amazonaws.com/$objKey")
            return true
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.UNAUTHORIZED || e.statusCode == HttpStatus.FORBIDDEN) {
                // Expected status codes when a card does not exist
                return false
            }
            throw e
        }
    }

    suspend fun addCardImage(image: ByteArray, cardUrl: String) {

        val objKey = cardObjKey(cardUrl)

        s3Client().use { s3 ->

            s3.putObject(PutObjectRequest {
                bucket = cardImagesBucket
                key = objKey
                body = ByteStream.fromBytes(image)
                metadata = S3ObjectMetadata.invoke {
                    cacheControl = "max-age=31536000"
                    contentType = "image/png"
                }.userMetadata
            })

            log.debug("Uploaded image for: $cardUrl")
        }
    }

    private fun addImage(
        image: MultipartFile,
        folderName: String,
        details: String,
        extension: String? = null
    ): String {

        val objKey = "$folderName/$details-${UUID.randomUUID()}${if (extension.isNullOrBlank()) "" else ".$extension"}"

        runBlocking {
            s3Client().use { s3 ->
                s3.putObject(PutObjectRequest {
                    bucket = userContentBucket
                    key = objKey
                    body = ByteStream.fromBytes(image.bytes)
                    metadata = mapOf(
                        "Cache-Control" to "max-age=31536000",
                        "Content-Type" to "image/jpeg",
                    )
                })
            }
        }

        return objKey
    }
}
