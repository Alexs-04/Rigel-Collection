import {useEffect, useMemo, useRef, useState} from 'react'
import api from '../../services/api.js'
import {loadDetailSafely} from '../../utils/detailLoader.js'

const initialForm = {
    name: '',
    description: '',
    barcode: '',
    category: 'OTHERS',
    price: '',
    stock: '',
    imageUrl: '',
    supplierName: '',
    supplierPrice: '',
}

function normalizeError(error, fallback) {
    return error?.response?.data?.message || fallback
}

function mapProductDetail(data) {
    const firstSupplier = Array.isArray(data.suppliers) && data.suppliers.length > 0 ? data.suppliers[0] : null
    return {
        ...data,
        supplierName: firstSupplier?.name || '',
        supplierPrice: firstSupplier?.supplyPrice ?? data.price ?? '',
    }
}

function buildPayload(source) {
    const supplierName = (source.supplierName || '').trim()
    return {
        name: (source.name || '').trim(),
        description: (source.description || '').trim(),
        barcode: (source.barcode || '').trim(),
        category: (source.category || 'OTHERS').trim().toUpperCase(),
        price: Number(source.price || 0),
        stock: Number(source.stock || 0),
        imageUrl: (source.imageUrl || '').trim(),
        supplierName,
        supplierPrice: Number(source.supplierPrice || source.price || 0),
    }
}

function matchesSearch(product, rawSearch) {
    const search = rawSearch.trim().toLowerCase()
    if (!search) return true

    const targets = [
        product?.name,
        product?.barcode,
        product?.description,
        product?.category,
    ]

    return targets.some((item) => String(item || '').toLowerCase().includes(search))
}

export function useProductsPageState({isAdmin}) {
    const [products, setProducts] = useState([])
    const [suppliers, setSuppliers] = useState([])
    const [search, setSearch] = useState('')
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')

    const [form, setForm] = useState(initialForm)
    const [formMessage, setFormMessage] = useState('')
    const [saving, setSaving] = useState(false)

    const [selectedName, setSelectedName] = useState(null)
    const [detail, setDetail] = useState(null)
    const [loadingDetail, setLoadingDetail] = useState(false)
    const [detailError, setDetailError] = useState('')
    const [editing, setEditing] = useState(false)

    const collapseTimeoutRef = useRef(null)
    const detailRequestRef = useRef(0)
    const [isCollapsingDetail, setIsCollapsingDetail] = useState(false)

    const filteredProducts = useMemo(() => {
        return products.filter((product) => matchesSearch(product, search))
    }, [products, search])

    const loadPageData = async () => {
        setLoadingList(true)
        setListError('')

        try {
            const [productsRes, suppliersRes] = await Promise.all([
                api.get('/product/all'),
                api.get('/suppliers/all').catch(() => ({data: []})),
            ])

            const loadedProducts = Array.isArray(productsRes.data) ? productsRes.data : []
            const loadedSuppliers = Array.isArray(suppliersRes.data) ? suppliersRes.data : []

            setProducts(loadedProducts)
            setSuppliers(loadedSuppliers)

            setForm((prev) => {
                if (prev.supplierName) return prev
                const firstSupplier = loadedSuppliers[0]?.name || ''
                return {...prev, supplierName: firstSupplier}
            })
        } catch (error) {
            setListError(normalizeError(error, 'No se pudo cargar la informacion de productos.'))
        } finally {
            setLoadingList(false)
        }
    }

    const loadDetail = async (name) => {
        if (collapseTimeoutRef.current) {
            clearTimeout(collapseTimeoutRef.current)
            collapseTimeoutRef.current = null
        }

        setIsCollapsingDetail(false)
        setSelectedName(name)
        setLoadingDetail(true)
        setDetailError('')
        setEditing(false)

        await loadDetailSafely({
            requestRef: detailRequestRef,
            fetchDetail: async () => {
                const res = await api.get(`/product/${encodeURIComponent(name)}`)
                return mapProductDetail(res.data || {})
            },
            onSuccess: (resolvedDetail) => {
                setDetail(resolvedDetail)
            },
            onError: (error) => {
                setDetailError(normalizeError(error, 'No se pudo cargar el detalle del producto.'))
                setDetail(null)
            },
            onFinally: () => {
                setLoadingDetail(false)
            },
        })
    }

    const handleProductClick = (name) => {
        if (selectedName === name) {
            detailRequestRef.current += 1
            setLoadingDetail(false)
            setSelectedName(null)
            setDetailError('')
            setEditing(false)
            setIsCollapsingDetail(true)

            if (collapseTimeoutRef.current) clearTimeout(collapseTimeoutRef.current)
            collapseTimeoutRef.current = setTimeout(() => {
                setDetail(null)
                setIsCollapsingDetail(false)
            }, 180)
            return
        }

        loadDetail(name)
    }

    const onChangeForm = (key, value) => {
        setForm((prev) => ({...prev, [key]: value}))
    }

    const onChangeDetail = (key, value) => {
        setDetail((prev) => ({...prev, [key]: value}))
    }

    const addProduct = async (event) => {
        event.preventDefault()
        if (!isAdmin) return

        setSaving(true)
        setFormMessage('')

        try {
            const payload = buildPayload(form)
            if (!payload.supplierName) throw new Error('Selecciona un proveedor para el producto.')

            await api.post('/product/add', payload)
            setForm({...initialForm, supplierName: suppliers[0]?.name || ''})
            setFormMessage('Producto agregado correctamente.')
            await loadPageData()
        } catch (error) {
            setFormMessage(normalizeError(error, error.message || 'No se pudo agregar el producto.'))
        } finally {
            setSaving(false)
        }
    }

    const saveChanges = async () => {
        if (!detail || !isAdmin) return

        setSaving(true)
        setDetailError('')

        try {
            const payload = buildPayload(detail)
            if (!payload.supplierName) throw new Error('Selecciona un proveedor para el producto.')

            await api.put(`/product/${encodeURIComponent(selectedName)}`, payload)
            setEditing(false)
            await loadPageData()
            await loadDetail(payload.name)
        } catch (error) {
            setDetailError(normalizeError(error, error.message || 'No se pudo actualizar el producto.'))
        } finally {
            setSaving(false)
        }
    }

    const deleteProduct = async () => {
        if (!detail || !isAdmin) return

        const confirmed = window.confirm(`Deseas eliminar el producto ${detail.name}?`)
        if (!confirmed) return

        setSaving(true)
        setDetailError('')

        try {
            await api.delete(`/product/${encodeURIComponent(detail.name)}`)
            setDetail(null)
            setSelectedName(null)
            await loadPageData()
        } catch (error) {
            setDetailError(normalizeError(error, 'No se pudo eliminar el producto.'))
        } finally {
            setSaving(false)
        }
    }

    useEffect(() => {
        loadPageData()
    }, [])

    useEffect(() => {
        return () => {
            if (collapseTimeoutRef.current) clearTimeout(collapseTimeoutRef.current)
        }
    }, [])

    return {
        products,
        suppliers,
        search,
        setSearch,
        loadingList,
        listError,
        filteredProducts,
        form,
        formMessage,
        saving,
        selectedName,
        detail,
        loadingDetail,
        detailError,
        editing,
        isCollapsingDetail,
        detailExpanded: Boolean(selectedName) || loadingDetail,
        showDetailContent: Boolean(detail || detailError || loadingDetail || isCollapsingDetail),
        setEditing,
        onChangeForm,
        onChangeDetail,
        addProduct,
        saveChanges,
        deleteProduct,
        handleProductClick,
        loadDetail,
    }
}

