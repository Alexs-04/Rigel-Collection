package com.korebit.rigel.service.image

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.korebit.rigel.dto.response.ImageUploadResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

@Service
class CloudinaryImageService(
    @param:Value("\${cloudinary.api-key:}")
    private val cloudinaryApiKey: String,

    @param:Value("\${cloudinary.api-secret:}")
    private val cloudinaryApiSecret: String,

    @param:Value("\${cloudinary.cloud-name:}")
    private val cloudinaryCloudName: String,

    @param:Value("\${cloudinary.folder:rigel/products}")
    private val cloudinaryFolder: String,

    @param:Value("\${image.storage.local-path:./uploads}")
    private val localStoragePath: String,

    @param:Value($$"${image.storage.public-path:/uploads}")
    private val localPublicPath: String,
) {

    // Cloudinary client is built only once if credentials exist
    private val cloudinary: Cloudinary? by lazy {
        if (isCloudinaryConfigured()) {
            Cloudinary(
                ObjectUtils.asMap(
                    "cloud_name", cloudinaryCloudName,
                    "api_key", cloudinaryApiKey,
                    "api_secret", cloudinaryApiSecret,
                    "secure", true
                )
            )
        } else null
    }

    private data class ProcessedImage(
        val bytes: ByteArray,
        val mimeType: String,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ProcessedImage

            if (!bytes.contentEquals(other.bytes)) return false
            if (mimeType != other.mimeType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }

    /**
     * It processes an image in Base64, converts it to WebP if necessary,
     * and upload to Cloudinary (or local storage as a fallback)
     */
    fun uploadImage(imageBase64: String, fileName: String): ImageUploadResponse {
        try {
            // Decodificar Base64
            val imageBytes = Base64.getDecoder().decode(imageBase64)

            // Read image from bytes
            val bufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
                ?: throw IllegalArgumentException("No se pudo leer la imagen")

            // Convert to WebP
            val processedImage = convertToWebP(bufferedImage)

            // Determinate if it uses Cloudinary or local storage
            return if (isCloudinaryConfigured()) {
                uploadToCloudinary(processedImage, fileName)
            } else {
                uploadToLocalStorage(processedImage, fileName)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al procesar imagen: ${e.message}", e)
        }
    }

    /**
     * Delete an image from Cloudinary (or local) given its public id
     */
    fun deleteImage(publicId: String) {
        try {
            if (isCloudinaryConfigured()) {
                cloudinary?.uploader()?.destroy(publicId, ObjectUtils.emptyMap())
            } else {
                val uploadDir = File(localStoragePath)
                val file = File(uploadDir, "$publicId.webp")
                if (file.exists()) file.delete()
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al eliminar imagen: ${e.message}", e)
        }
    }

    /**
     * Convert image to Webp
     */
    private fun convertToWebP(bufferedImage: BufferedImage): ProcessedImage {
        return try {
            val output = ByteArrayOutputStream()

            // Try using ImageIO to write WebP if the format is available
            val written = ImageIO.write(bufferedImage, "webp", output)

            if (!written) {
                // If WebP is not supported, write as PNG as a fallback.
                ImageIO.write(bufferedImage, "png", output)
                ProcessedImage(output.toByteArray(), "image/png")
            } else {
                ProcessedImage(output.toByteArray(), "image/webp")
            }
        } catch (_: Exception) {
            // If there is an error, return as PNG
            val output = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "png", output)
            ProcessedImage(output.toByteArray(), "image/png")
        }
    }

    /**
     * Create a data URL so the frontend can preview the image immediately
     */
    private fun toDataUrl(imageBytes: ByteArray, mimeType: String): String {
        val encoded = Base64.getEncoder().encodeToString(imageBytes)
        return "data:$mimeType;base64,$encoded"
    }

    /**
     * Upload the image to Cloudinary
     */
    private fun uploadToCloudinary(image: ProcessedImage, fileName: String): ImageUploadResponse {
        val client = cloudinary
            ?: throw IllegalStateException("Cloudinary no está configurado correctamente")

        val publicId = generatePublicId(fileName)

        return try {
            val uploadResult = client.uploader().upload(
                image.bytes,
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", cloudinaryFolder,
                    "resource_type", "image",
                    "overwrite", true,
                    "format", if (image.mimeType == "image/webp") "webp" else "png"
                )
            )

            val secureUrl = uploadResult["secure_url"] as? String
                ?: throw IllegalStateException("Cloudinary no devolvió una URL válida")
            val returnedPublicId = uploadResult["public_id"] as? String ?: publicId

            ImageUploadResponse(
                cloudinaryPublicId = returnedPublicId,
                imageUrl = secureUrl,
                success = true,
                message = "Imagen subida correctamente a Cloudinary"
            )
        } catch (e: Exception) {
            // Si falla la subida a Cloudinary, hacemos fallback a almacenamiento local
            uploadToLocalStorage(image, fileName)
        }
    }

    /**
     * Store the image in local storage as a fallback
     */
    private fun uploadToLocalStorage(image: ProcessedImage, fileName: String): ImageUploadResponse {
        try {
            val uploadDir = File(localStoragePath)
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            val publicId = generatePublicId(fileName)
            val extension = if (image.mimeType == "image/webp") "webp" else "png"
            val storedFileName = "$publicId.$extension"
            val outputFile = File(uploadDir, storedFileName)

            outputFile.writeBytes(image.bytes)

            // Relative path served statically, NOT a data URL
            val relativeUrl = "$localPublicPath/$storedFileName"

            return ImageUploadResponse(
                cloudinaryPublicId = publicId,
                imageUrl = relativeUrl,
                success = true,
                message = "Imagen almacenada localmente"
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al almacenar imagen: ${e.message}", e)
        }
    }

    /**
     * Check if Cloudinary is configured
     */
    private fun isCloudinaryConfigured(): Boolean {
        return cloudinaryApiKey.isNotBlank() &&
                cloudinaryApiSecret.isNotBlank() &&
                cloudinaryCloudName.isNotBlank()
    }

    /**
     * Generates a unique public ID for the image
     */
    private fun generatePublicId(fileName: String): String {
        val timestamp = System.currentTimeMillis()
        val random = UUID.randomUUID().toString().take(8)
        val cleanName = fileName
            .substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9]"), "_")
            .take(20)

        return "product_${cleanName}_${timestamp}_$random"
    }
}