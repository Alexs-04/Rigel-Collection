/**
 * Resize an image to a maximum size of 600x600 pixels, maintaining the aspect ratio and adding a white background if necessary.
 * @param {File} file - Image file
 * @returns {Promise<Blob>} - Resized blob
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

                canvas.width = targetSize
                canvas.height = targetSize
                ctx.fillStyle = '#ffffff'
                ctx.fillRect(0, 0, targetSize, targetSize)

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
 * Converts a Blob to Base64
 * @param {Blob} blob - Blob to convert
 * @returns {Promise<string>} - Base64 string
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
 * Validates that the file is a supported image
 * @param {File} file - File to validate
 * @returns {boolean} - True if it's a valid image
 */
export function isValidImageFile(file) {
    const validMimes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/bmp']
    return validMimes.includes(file.type) && file.size <= 10 * 1024 * 1024 // 10MB max
}

