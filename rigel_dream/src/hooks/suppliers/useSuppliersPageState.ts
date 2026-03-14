import {useEffect, useRef, useState} from 'react'
import type {FormEvent} from 'react'
import api from '../../services/api'
import {loadDetailSafely} from '../../utils/detailLoader'
import type {SupplierDetail, SupplierFormValues, SupplierProduct, SupplierSummary} from '../../types/suppliers'

const initialForm: SupplierFormValues = {
    name: '',
    contactEmail: '',
    phoneNumber: '',
    address: '',
    products: [],
}

function normalizeError(error: unknown, fallback: string): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || fallback
}

function buildPayload(source: Partial<SupplierFormValues> | Partial<SupplierDetail>) {
    return {
        name: String(source.name || '').trim(),
        contactEmail: String(source.contactEmail || '').trim(),
        phoneNumber: String(source.phoneNumber || '').trim(),
        address: String(source.address || '').trim(),
        products: Array.isArray(source.products) ? source.products : [],
    }
}

export function useSuppliersPageState({isAdmin}: {isAdmin: boolean}) {
    const [suppliers, setSuppliers] = useState<SupplierSummary[]>([])
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')

    const [form, setForm] = useState<SupplierFormValues>(initialForm)
    const [formMessage, setFormMessage] = useState('')
    const [saving, setSaving] = useState(false)

    const [selectedName, setSelectedName] = useState<string | null>(null)
    const [detail, setDetail] = useState<SupplierDetail | null>(null)
    const [products, setProducts] = useState<SupplierProduct[]>([])
    const [loadingDetail, setLoadingDetail] = useState(false)
    const [detailError, setDetailError] = useState('')
    const [editing, setEditing] = useState(false)

    const collapseTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
    const detailRequestRef = useRef(0)
    const [isCollapsingDetail, setIsCollapsingDetail] = useState(false)

    // @ts-ignore
    const loadSuppliers = async () => {
        setLoadingList(true)
        setListError('')

        try {
            const res = await api.get('/suppliers/all')
            setSuppliers(Array.isArray(res.data) ? res.data : [])
        } catch (error) {
            setListError(normalizeError(error, 'No se pudo cargar la lista de proveedores.'))
        } finally {
            setLoadingList(false)
        }
    }

    // @ts-ignore
    const loadDetail = async (name: string) => {
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
                // @ts-ignore
                const [detailRes, productsRes] = await Promise.all([
                    api.get(`/suppliers/${encodeURIComponent(name)}`),
                    api.get(`/suppliers/${encodeURIComponent(name)}/products`).catch(() => ({data: []})),
                ])

                return {
                    detail: detailRes.data as SupplierDetail,
                    products: Array.isArray(productsRes.data) ? productsRes.data : [],
                }
            },
            onSuccess: (resolved) => {
                setDetail(resolved.detail)
                setProducts(resolved.products)
            },
            onError: (error) => {
                setDetailError(normalizeError(error, 'No se pudo cargar el detalle del proveedor.'))
                setDetail(null)
                setProducts([])
            },
            onFinally: () => {
                setLoadingDetail(false)
            },
        })
    }

    const handleSupplierClick = (name: string) => {
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
                setProducts([])
                setIsCollapsingDetail(false)
            }, 180)
            return
        }

        loadDetail(name)
    }

    const onChangeForm = (key: keyof SupplierFormValues, value: string) => {
        setForm((prev) => ({...prev, [key]: value}))
    }

    const onChangeDetail = (key: keyof SupplierDetail, value: string) => {
        setDetail((prev) => (prev ? {...prev, [key]: value} : prev))
    }

    // @ts-ignore
    const addSupplier = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        if (!isAdmin) return

        setSaving(true)
        setFormMessage('')

        try {
            await api.post('/suppliers/add', buildPayload(form))
            setForm(initialForm)
            setFormMessage('Proveedor agregado correctamente.')
            await loadSuppliers()
        } catch (error) {
            setFormMessage(normalizeError(error, 'No se pudo agregar el proveedor.'))
        } finally {
            setSaving(false)
        }
    }

    // @ts-ignore
    const saveChanges = async () => {
        if (!detail || !selectedName || !isAdmin) return

        setSaving(true)
        setDetailError('')

        try {
            const payload = buildPayload(detail)
            await api.put(`/suppliers/${encodeURIComponent(selectedName)}`, payload)
            setEditing(false)
            await loadSuppliers()
            await loadDetail(payload.name)
        } catch (error) {
            setDetailError(normalizeError(error, 'No se pudo actualizar el proveedor.'))
        } finally {
            setSaving(false)
        }
    }

    // @ts-ignore
    const deleteSupplier = async () => {
        if (!detail || !isAdmin) return
        const confirmed = window.confirm(`Deseas eliminar al proveedor ${detail.name}?`)
        if (!confirmed) return

        setSaving(true)
        setDetailError('')

        try {
            await api.delete(`/suppliers/${encodeURIComponent(detail.name)}`)
            setDetail(null)
            setProducts([])
            setSelectedName(null)
            await loadSuppliers()
        } catch (error) {
            setDetailError(normalizeError(error, 'No se pudo eliminar el proveedor.'))
        } finally {
            setSaving(false)
        }
    }

    useEffect(() => {
        loadSuppliers()
    }, [])

    useEffect(() => {
        return () => {
            if (collapseTimeoutRef.current) clearTimeout(collapseTimeoutRef.current)
        }
    }, [])

    return {
        suppliers,
        loadingList,
        listError,
        form,
        formMessage,
        saving,
        selectedName,
        detail,
        products,
        loadingDetail,
        detailError,
        editing,
        isCollapsingDetail,
        detailExpanded: Boolean(selectedName) || loadingDetail,
        showDetailContent: Boolean(detail || detailError || loadingDetail || isCollapsingDetail),
        setEditing,
        onChangeForm,
        onChangeDetail,
        addSupplier,
        saveChanges,
        deleteSupplier,
        handleSupplierClick,
        loadDetail,
    }
}


