package com.korebit.rigel.service.image

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

    @param:Value("\${image.storage.local-path:./uploads}")
    private val localStoragePath: String,
) {

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
            val webpBytes = convertToWebP(bufferedImage)

            // Determinate if it uses Cloudinary or local storage
            return if (isCloudinaryConfigured()) {
                uploadToCloudinary(fileName)
            } else {
                uploadToLocalStorage(webpBytes, fileName)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al procesar imagen: ${e.message}", e)
        }
    }

    /**
     * Convert image to Webp
     */
    private fun convertToWebP(bufferedImage: BufferedImage): ByteArray {
        return try {
            val output = ByteArrayOutputStream()

            // Try using ImageIO to write WebP if the format is available
            val written = ImageIO.write(bufferedImage, "webp", output)

            if (!written) {
                // If WebP is not supported, write as PNG as a fallback.
                ImageIO.write(bufferedImage, "png", output)
            }

            output.toByteArray()
        } catch (_: Exception) {
            // If there is an error, return as PNG
            val output = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "png", output)
            output.toByteArray()
        }
    }

    /**
     * Upload the image to Cloudinary (configuration required)
     */
    private fun uploadToCloudinary(fileName: String): ImageUploadResponse {
        // TODO: Implement real integration with Cloudinary SDK when available
        // For now, return simulated response
        val publicId = generatePublicId(fileName)
        val simulatedUrl = "https://res.cloudinary.com/$cloudinaryCloudName/image/upload/$publicId.webp"

        return ImageUploadResponse(
            cloudinaryPublicId = publicId,
            imageUrl = simulatedUrl,
            success = true,
            message = "Imagen procesada y lista para Cloudinary"
        )
    }

    /**
     * Store the image in local storage as a fallback
     */
    private fun uploadToLocalStorage(imageBytes: ByteArray, fileName: String): ImageUploadResponse {
        try {
            val uploadDir = File(localStoragePath)
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            val publicId = generatePublicId(fileName)
            val webpFileName = "$publicId.webp"
            val outputFile = File(uploadDir, webpFileName)

            outputFile.writeBytes(imageBytes)

            val url = "/uploads/$webpFileName"

            return ImageUploadResponse(
                cloudinaryPublicId = publicId,
                imageUrl = url,
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







