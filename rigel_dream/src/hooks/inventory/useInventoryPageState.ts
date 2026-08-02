import {useEffect, useMemo, useState} from 'react'
import type {ProductSummary} from '../../types/products'
import {fetchInventoryProducts} from '../../services/inventoryService'

function normalizeError(error: unknown, fallback: string): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || fallback
}

function normalizeText(value: string | undefined | null): string {
    return String(value || '').trim().toLowerCase()
}

export function useInventoryPageState() {
    const [products, setProducts] = useState<ProductSummary[]>([])
    const [search, setSearch] = useState('')
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [selectedBarcode, setSelectedBarcode] = useState('')

    const loadProducts = async () => {
        setLoading(true)
        setError('')

        try {
            const data = await fetchInventoryProducts()
            setProducts(data)
            setSelectedBarcode((current) => {
                if (current && data.some((product) => product.barcode === current)) {
                    return current
                }
                return ''
            })
        } catch (loadError) {
            setError(normalizeError(loadError, 'No se pudo cargar el inventario.'))
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadProducts().then(r => r).catch(e => console.error('Error loading products:', e))
    }, [])

    const filteredProducts = useMemo(() => {
        const normalized = normalizeText(search)
        if (!normalized) return products

        return products.filter((product) => {
            const searchableFields = [
                product.name,
                product.barcode,
                product.description,
                product.category,
                ...((product.suppliers || []).map((supplier) => supplier.name)),
            ]

            return searchableFields.some((field) => normalizeText(field).includes(normalized))
        })
    }, [products, search])

    const selectedProduct = useMemo(() => {
        return products.find((product) => product.barcode === selectedBarcode) || null
    }, [products, selectedBarcode])

    const totalStock = useMemo(() => {
        return products.reduce((sum, product) => sum + Number(product.stock || 0), 0)
    }, [products])

    const totalValue = useMemo(() => {
        return products.reduce((sum, product) => sum + Number(product.price || 0) * Number(product.stock || 0), 0)
    }, [products])

    const allProducts =  products

    return {
        products,
        productCount: products.length,
        filteredProducts,
        selectedProduct,
        selectedBarcode,
        search,
        loading,
        error,
        totalStock,
        totalValue,
        setSearch,
        setSelectedBarcode,
        reload: loadProducts,
        allProducts
    }
}


