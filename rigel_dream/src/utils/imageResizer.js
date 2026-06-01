/**
 * Redimensiona una imagen a 600x600px manteniendo aspecto
 * @param {File} file - Archivo de imagen
 * @returns {Promise<Blob>} - Blob redimensionado
 */
export async function resizeImage(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()

        reader.onload = (event) => {
            const img = new Image()

            img.onload = () => {
                const canvas = document.createElement('canvas')
                const ctx = canvas.getContext('2d')

                const targetSize = 600
                const maxWidth = targetSize
                const maxHeight = targetSize

                let width = img.width
                let height = img.height

                // Mantener proporciones
                if (width > height) {
                    if (width > maxWidth) {
                        height = Math.round((height * maxWidth) / width)
                        width = maxWidth
                    }
                } else {
                    if (height > maxHeight) {
                        width = Math.round((width * maxHeight) / height)
                        height = maxHeight
                    }
                }

                // Crear canvas con fondo transparente/blanco
                canvas.width = targetSize
                canvas.height = targetSize
                ctx.fillStyle = '#ffffff'
                ctx.fillRect(0, 0, targetSize, targetSize)

                // Centrar imagen
                const x = (targetSize - width) / 2
                const y = (targetSize - height) / 2
                ctx.drawImage(img, x, y, width, height)

                canvas.toBlob(resolve, 'image/png', 0.95)
            }

            img.onerror = () => {
                reject(new Error('No se pudo cargar la imagen'))
            }

            img.src = event.target.result
        }

        reader.onerror = () => {
            reject(new Error('Error al leer el archivo'))
        }

        reader.readAsDataURL(file)
    })
}

/**
 * Convierte un Blob a Base64
 * @param {Blob} blob - Blob a convertir
 * @returns {Promise<string>} - String en Base64
 */
export function blobToBase64(blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => {
            const base64 = reader.result.split(',')[1]
            resolve(base64)
        }
        reader.onerror = () => {
            reject(new Error('Error al convertir a Base64'))
        }
        reader.readAsDataURL(blob)
    })
}

/**
 * Valida que el archivo sea una imagen soportada
 * @param {File} file - Archivo a validar
 * @returns {boolean} - True si es imagen válida
 */
export function isValidImageFile(file) {
    const validMimes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/bmp']
    return validMimes.includes(file.type) && file.size <= 10 * 1024 * 1024 // 10MB max
}

