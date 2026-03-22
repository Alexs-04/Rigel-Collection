import React from 'react'
import {CATEGORY_OPTIONS_ES} from '../../utils/productPresentation.js'

export default function ProductFieldsGrid({value, onChange, suppliers, disabled, isRequired}) {
    return (
        <div style={{display: 'grid', gap: 10, marginBottom: 14}}>
            <input
                className="input"
                placeholder="Nombre"
                value={value?.name || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('name', e.target.value)}
            />
            <input
                className="input"
                placeholder="Codigo de barras"
                value={value?.barcode || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('barcode', e.target.value)}
            />
            <input
                className="input"
                placeholder="Descripcion"
                value={value?.description || ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('description', e.target.value)}
            />
            <select
                className="input"
                value={value?.category || 'OTHERS'}
                disabled={disabled}
                onChange={(e) => onChange('category', e.target.value)}
            >
                {CATEGORY_OPTIONS_ES.map((category) => (
                    <option key={category.value} value={category.value}>{category.label}</option>
                ))}
            </select>
            <input
                className="input"
                placeholder="Precio de venta"
                type="number"
                min="0"
                step="0.01"
                value={value?.price ?? ''}
                disabled={disabled}
                required={isRequired}
                onChange={(e) => onChange('price', e.target.value)}
            />
            <input
                className="input"
                placeholder="URL de imagen"
                value={value?.imageUrl || ''}
                disabled={disabled}
                onChange={(e) => onChange('imageUrl', e.target.value)}
            />
            <select
                className="input"
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
                className="input"
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

