import {useEffect, useMemo, useState} from 'react'
import type {FormEvent} from 'react'
import api from '../../services/api'
import type {ProductBatch, ProductSummary, ProductSupplier} from '../../types/products'
import type {PurchaseDto, PurchaseFormValues} from '../../types/purchases'

function toDateInputValue(date: Date): string {
    return date.toISOString().slice(0, 10)
}

function defaultForm(): PurchaseFormValues {
    const today = new Date()
    const expiration = new Date(today)
    expiration.setMonth(expiration.getMonth() + 6)

    return {
        productName: '',
        supplierName: '',
        quantity: '',
        unitPrice: '',
        purchaseDate: toDateInputValue(today),
        notes: '',
        useExistingBatch: true,
        batchId: '',
        batchCode: '',
        receptionDate: toDateInputValue(today),
        expirationDate: toDateInputValue(expiration),
        available: true,
        batchPrice: '',
    }
}

function normalizeError(error: unknown, fallback: string): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || fallback
}

export function usePurchasesPageState() {
    const [purchases, setPurchases] = useState<PurchaseDto[]>([])
    const [products, setProducts] = useState<ProductSummary[]>([])
    const [search, setSearch] = useState('')
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')

    const [form, setForm] = useState<PurchaseFormValues>(defaultForm)
    const [formMessage, setFormMessage] = useState('')
    const [saving, setSaving] = useState(false)

    const [selectedId, setSelectedId] = useState<number | null>(null)

    const selectedProduct = useMemo(() => {
        return products.find((product) => product.name === form.productName) || null
    }, [products, form.productName])

    const supplierOptions: ProductSupplier[] = useMemo(() => {
        return selectedProduct?.suppliers || []
    }, [selectedProduct])

    const selectedSupplier = useMemo(() => {
        return supplierOptions.find((supplier) => supplier.name === form.supplierName) || null
    }, [supplierOptions, form.supplierName])

    const batchOptions: ProductBatch[] = useMemo(() => {
        if (!selectedSupplier) return []
        return selectedSupplier.batches || []
    }, [selectedSupplier])

    const filteredPurchases = useMemo(() => {
        const normalized = search.trim().toLowerCase()
        if (!normalized) return purchases

        return purchases.filter((item) => {
            return [item.code, item.productName, item.supplierName, item.batchCode]
                .some((value) => String(value || '').toLowerCase().includes(normalized))
        })
    }, [purchases, search])

    const selectedPurchase = useMemo(() => {
        return purchases.find((purchase) => purchase.id === selectedId) || null
    }, [purchases, selectedId])

    const loadPageData = async () => {
        setLoadingList(true)
        setListError('')

        try {
            const [purchasesRes, productsRes] = await Promise.all([
                api.get('/purchases/all'),
                api.get('/product/all'),
            ])

            const loadedPurchases = Array.isArray(purchasesRes.data) ? purchasesRes.data as PurchaseDto[] : []
            const loadedProducts = Array.isArray(productsRes.data) ? productsRes.data as ProductSummary[] : []
            setPurchases(loadedPurchases)
            setProducts(loadedProducts)

            setForm((prev) => {
                if (prev.productName) return prev
                const firstProduct = loadedProducts[0]
                const firstSupplier = firstProduct?.suppliers?.[0]
                const firstBatch = firstSupplier?.batches?.[0]

                return {
                    ...prev,
                    productName: firstProduct?.name || '',
                    supplierName: firstSupplier?.name || '',
                    unitPrice: String(firstSupplier?.supplyPrice ?? ''),
                    batchId: firstBatch ? String(firstBatch.id) : '',
                }
            })
        } catch (error) {
            setListError(normalizeError(error, 'No se pudo cargar la pagina de compras.'))
        } finally {
            setLoadingList(false)
        }
    }

    const onChangeForm = (key: keyof PurchaseFormValues, value: string | boolean) => {
        if (key === 'productName') {
            const nextProduct = products.find((product) => product.name === value) || null
            const nextSupplier = nextProduct?.suppliers?.[0] || null
            const nextBatch = nextSupplier?.batches?.[0] || null

            setForm((prev) => ({
                ...prev,
                productName: String(value),
                supplierName: nextSupplier?.name || '',
                unitPrice: String(nextSupplier?.supplyPrice ?? prev.unitPrice ?? ''),
                batchId: nextBatch ? String(nextBatch.id) : '',
            }))
            return
        }

        if (key === 'supplierName') {
            const nextSupplier = supplierOptions.find((supplier) => supplier.name === value) || null
            const nextBatch = nextSupplier?.batches?.[0] || null

            setForm((prev) => ({
                ...prev,
                supplierName: String(value),
                unitPrice: String(nextSupplier?.supplyPrice ?? prev.unitPrice ?? ''),
                batchId: nextBatch ? String(nextBatch.id) : '',
            }))
            return
        }

        setForm((prev) => ({...prev, [key]: value}))
    }

    const createPurchase = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        setSaving(true)
        setFormMessage('')

        try {
            const quantity = Number(form.quantity)
            const unitPrice = Number(form.unitPrice)
            if (!form.productName) throw new Error('Selecciona un producto.')
            if (!form.supplierName) throw new Error('Selecciona un proveedor.')
            if (!Number.isFinite(quantity) || quantity <= 0) throw new Error('La cantidad debe ser mayor a 0.')
            if (!Number.isFinite(unitPrice) || unitPrice < 0) throw new Error('El precio unitario no es valido.')

            const payload: Record<string, unknown> = {
                productName: form.productName,
                supplierName: form.supplierName,
                quantity,
                unitPrice,
                purchaseDate: form.purchaseDate,
                notes: String(form.notes || '').trim(),
                available: form.available,
            }

            if (form.useExistingBatch && form.batchId.trim()) {
                payload.batchId = Number(form.batchId)
            } else {
                const batchCode = form.batchCode.trim()
                if (!batchCode) throw new Error('El codigo de lote es obligatorio cuando no se usa lote existente.')
                if (!form.expirationDate) throw new Error('La fecha de caducidad del lote es obligatoria.')

                payload.batchCode = batchCode
                payload.receptionDate = form.receptionDate
                payload.expirationDate = form.expirationDate
                payload.batchPrice = Number(form.batchPrice || form.unitPrice || 0)
            }

            await api.post('/purchases/add', payload)
            setForm(defaultForm())
            setFormMessage('Compra registrada correctamente.')
            await loadPageData()
        } catch (error) {
            const fallback = error instanceof Error ? error.message : 'No se pudo registrar la compra.'
            setFormMessage(normalizeError(error, fallback))
        } finally {
            setSaving(false)
        }
    }

    const handleSelectPurchase = (id: number) => {
        setSelectedId((prev) => (prev === id ? null : id))
    }

    useEffect(() => {
        loadPageData()
    }, [])

    return {
        purchases,
        products,
        search,
        setSearch,
        loadingList,
        listError,
        filteredPurchases,
        form,
        formMessage,
        saving,
        selectedId,
        selectedPurchase,
        supplierOptions,
        batchOptions,
        onChangeForm,
        createPurchase,
        handleSelectPurchase,
        reload: loadPageData,
    }
}

