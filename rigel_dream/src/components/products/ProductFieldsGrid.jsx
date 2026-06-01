import React, {useState} from 'react'
import {CATEGORY_OPTIONS_ES} from '../../utils/productPresentation.js'
import {imageUploadService} from '../../services/imageUploadService.js'

export default function ProductFieldsGrid({value, onChange, suppliers, disabled, isRequired}) {
    const [uploading, setUploading] = useState(false)
    const [uploadError, setUploadError] = useState('')
    return (
        <div className="mb-3.5 grid gap-2.5">
            <input
                className="ui-input"
                placeholder="Nombre"
                value={value?.name || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('name', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Codigo de barras"
                value={value?.barcode || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('barcode', e.target.value)}
            />
            <input
                className="ui-input"
                placeholder="Descripcion"
                value={value?.description || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('description', e.target.value)}
            />
            <select
                className="ui-input"
                value={value?.category || 'OTHERS'}
                disabled={disabled}
                onChange={(e) => onChange('category', e.target.value)}
            >
                {CATEGORY_OPTIONS_ES.map((category) => (
                    <option key={category.value} value={category.value}>{category.label}</option>
                ))}
            </select>
            <input
                className="ui-input"
                placeholder="Precio de venta"
                type="number"
                min="0"
                step="0.01"
                value={value?.price ?? ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('price', e.target.value)}
            />
            <div className="sm:col-span-2">
                <label className="block text-sm font-medium mb-2">Imagen del producto (600x600px)</label>
                <div className="flex gap-4 items-start">
                    <input
                        type="file"
                        accept="image/*"
                        disabled={disabled || uploading}
                        onChange={async (e) => {
                            const file = e.target.files?.[0]
                            if (!file) return

                            setUploading(true)
                            setUploadError('')
                            try {
                                const {publicId, url} = await imageUploadService.uploadImage(file)
                                onChange('cloudinaryPublicId', publicId)
                                onChange('imageUrl', url)
                                setUploadError('')
                            } catch (error) {
                                setUploadError(error.message)
                            } finally {
                                setUploading(false)
                                e.target.value = ''
                            }
                        }}
                        className="flex-1 ui-input"
                    />
                    {value?.imageUrl && (
                        <img
                            src={value.imageUrl}
                            alt="Preview"
                            className="h-24 w-24 object-cover rounded border"
                        />
                    )}
                </div>
                {uploading && <p className="text-xs text-blue-600 mt-1">Subiendo imagen...</p>}
                {uploadError && <p className="text-xs text-red-600 mt-1">{uploadError}</p>}
            </div>
            <select
                className="ui-input"
                value={value?.supplierName || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('supplierName', e.target.value)}
            >
                <option value="">Selecciona proveedor</option>
                {suppliers.map((supplier) => (
                    <option key={supplier.name} value={supplier.name}>{supplier.name}</option>
                ))}
            </select>
            <input
                className="ui-input"
                placeholder="Precio con proveedor"
                type="number"
                min="0"
                step="0.01"
                value={value?.supplierPrice ?? ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('supplierPrice', e.target.value)}
            />
        </div>
    )
}

