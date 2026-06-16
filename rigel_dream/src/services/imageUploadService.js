import api from './api.js'
import {resizeImage, blobToBase64, isValidImageFile} from '../utils/imageResizer.js'

/**
 * Service to handle image uploads to Cloudinary
 */
export const imageUploadService = {
    /**
     * Process and upload an image
     * @param {File} file - Image file
     * @returns {Promise<{publicId: string, url: string}>}
     */
    async uploadImage(file) {
        // Validate file
        if (!isValidImageFile(file)) {
            throw new Error('Formato de imagen no soportado. Use PNG, JPG, WebP, GIF o BMP (máx 10MB)')
        }

        try {
            // Resize image to 600x600px
            const resizedBlob = await resizeImage(file)

            // Convert to Base64
            const base64 = await blobToBase64(resizedBlob)

            // Send to backend for conversion to WebP and upload to Cloudinary
            const response = await api.post('/product/upload-image', {
                imageBase64: base64,
                fileName: file.name
            })

            const data = response?.data || {}
            if (data.success === false) {
                throw new Error(data.message || 'El servidor rechazó la imagen')
            }

            const publicId = data.cloudinaryPublicId || data.publicId || ''
            const url = data.imageUrl || data.url || ''

            if (!url) {
                throw new Error('El servidor no devolvió una URL de imagen válida')
            }

            return {
                publicId,
                url
            }
        } catch (error) {
            const message = error?.response?.data?.message || error.message || 'Error al cargar imagen'
            throw new Error(message)
        }
    }
}

