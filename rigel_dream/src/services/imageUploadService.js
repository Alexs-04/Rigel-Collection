import api from './api.js'
import {resizeImage, blobToBase64, isValidImageFile} from '../utils/imageResizer.js'

/**
 * Servicio para manejar carga de imágenes a Cloudinary
 */
export const imageUploadService = {
    /**
     * Procesa y sube una imagen
     * @param {File} file - Archivo de imagen
     * @returns {Promise<{publicId: string, url: string}>}
     */
    async uploadImage(file) {
        // Validar archivo
        if (!isValidImageFile(file)) {
            throw new Error('Formato de imagen no soportado. Use PNG, JPG, WebP, GIF o BMP (máx 10MB)')
        }

        try {
            // Redimensionar imagen a 600x600
            const resizedBlob = await resizeImage(file)

            // Convertir a Base64
            const base64 = await blobToBase64(resizedBlob)

            // Enviar al backend para conversión a WebP y carga en Cloudinary
            const response = await api.post('/product/upload-image', {
                imageBase64: base64,
                fileName: file.name
            })

            return {
                publicId: response.data.cloudinaryPublicId,
                url: response.data.imageUrl
            }
        } catch (error) {
            const message = error?.response?.data?.message || error.message || 'Error al cargar imagen'
            throw new Error(message)
        }
    }
}

