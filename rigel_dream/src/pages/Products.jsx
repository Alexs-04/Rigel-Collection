import React, {useContext, useEffect, useMemo, useRef, useState} from 'react'
import api from '../services/api'
import {AuthContext} from '../context/AuthContext'
import {loadDetailSafely} from '../utils/detailLoader'
import {CATEGORY_OPTIONS_ES, formatPriceMxn, toCategoryLabel} from '../utils/productPresentation'

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

export default function Products() {
    const {user} = useContext(AuthContext)
    const isAdmin = useMemo(() => ['ADMIN', 'ROOT'].includes(user?.role || ''), [user])

    const [products, setProducts] = useState([])
    const [suppliers, setSuppliers] = useState([])
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

    const onChangeForm = (key, value) => {
        setForm((prev) => ({...prev, [key]: value}))
    }

    const loadPageData = async () => {
        setLoadingList(true)
        setListError('')
        try {
            const [productsRes, suppliersRes] = await Promise.all([
                api.get('/product/all'),
                api.get('/suppliers/all').catch(() => ({data: []})),
            ])
            const loadedSuppliers = Array.isArray(suppliersRes.data) ? suppliersRes.data : []
            setProducts(Array.isArray(productsRes.data) ? productsRes.data : [])
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
                const data = res.data || {}
                const firstSupplier = Array.isArray(data.suppliers) && data.suppliers.length > 0 ? data.suppliers[0] : null
                return {
                    ...data,
                    supplierName: firstSupplier?.name || '',
                    supplierPrice: firstSupplier?.supplyPrice ?? data.price ?? '',
                }
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

    useEffect(() => {
        loadPageData()
    }, [])

    useEffect(() => {
        return () => {
            if (collapseTimeoutRef.current) clearTimeout(collapseTimeoutRef.current)
        }
    }, [])

    const detailExpanded = Boolean(selectedName) || loadingDetail
    const showDetailContent = Boolean(detail || detailError || loadingDetail || isCollapsingDetail)

    const buildPayload = (source) => {
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

    const addProduct = async (event) => {
        event.preventDefault()
        if (!isAdmin) return

        setSaving(true)
        setFormMessage('')

        try {
            const payload = buildPayload(form)
            if (!payload.supplierName) throw new Error('Selecciona un proveedor para el producto.')

            await api.post('/product/add', payload)
            setForm({
                ...initialForm,
                supplierName: suppliers[0]?.name || '',
            })
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

    return (
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Productos</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Consulta el catalogo y administra productos con proveedores previamente registrados.
                </p>
            </section>

            {isAdmin && (
                <section className="card" style={{padding: 20}}>
                    <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Agregar producto</h2>
                    <form onSubmit={addProduct}
                          style={{display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12}}>
                        <input className="input" placeholder="Nombre" value={form.name}
                               onChange={(e) => onChangeForm('name', e.target.value)} required/>
                        <input className="input" placeholder="Codigo de barras" value={form.barcode}
                               onChange={(e) => onChangeForm('barcode', e.target.value)} required/>
                        <input className="input" placeholder="Descripcion" value={form.description}
                               onChange={(e) => onChangeForm('description', e.target.value)} required/>
                        <select className="input" value={form.category}
                                onChange={(e) => onChangeForm('category', e.target.value)}>
                            {CATEGORY_OPTIONS_ES.map((category) => (
                                <option key={category.value} value={category.value}>{category.label}</option>
                            ))}
                        </select>
                        <input className="input" placeholder="Precio de venta" type="number" min="0" step="0.01"
                               value={form.price} onChange={(e) => onChangeForm('price', e.target.value)} required/>
                        <input className="input" placeholder="Stock" type="number" min="0" step="1" value={form.stock}
                               onChange={(e) => onChangeForm('stock', e.target.value)} required/>
                        <input className="input" placeholder="URL de imagen" value={form.imageUrl}
                               onChange={(e) => onChangeForm('imageUrl', e.target.value)}/>
                        <select className="input" value={form.supplierName}
                                onChange={(e) => onChangeForm('supplierName', e.target.value)} required>
                            <option value="">Selecciona proveedor</option>
                            {suppliers.map((supplier) => (
                                <option key={supplier.name} value={supplier.name}>{supplier.name}</option>
                            ))}
                        </select>
                        <input className="input" placeholder="Precio con proveedor" type="number" min="0" step="0.01"
                               value={form.supplierPrice}
                               onChange={(e) => onChangeForm('supplierPrice', e.target.value)} required/>

                        <div style={{gridColumn: '1 / -1'}}>
                            <button className="btn-primary" type="submit" disabled={saving || suppliers.length === 0}>
                                {saving ? 'Guardando...' : 'Agregar producto'}
                            </button>
                        </div>
                    </form>
                    {suppliers.length === 0 && (
                        <p className="text-muted" style={{marginBottom: 0}}>
                            Debes registrar al menos un proveedor para crear productos.
                        </p>
                    )}
                    {formMessage && <p className="text-muted" style={{marginBottom: 0}}>{formMessage}</p>}
                </section>
            )}

            {!isAdmin && (
                <section className="card" style={{padding: 16}}>
                    <p className="text-muted" style={{margin: 0}}>
                        Tu rol ({user?.role || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o
                        eliminar productos.
                    </p>
                </section>
            )}

            <section style={{display: 'grid', gridTemplateColumns: 'minmax(280px, 1fr) minmax(380px, 1.2fr)', gap: 16}}>
                <div className="card" style={{padding: 16}}>
                    <h2 style={{marginTop: 0, fontSize: 18}}>Lista de productos</h2>
                    {loadingList && <p className="text-muted">Cargando productos...</p>}
                    {!loadingList && listError && <p className="text-muted">{listError}</p>}
                    {!loadingList && !listError && products.length === 0 &&
                        <p className="text-muted">No hay productos registrados.</p>}
                    <div style={{display: 'grid', gap: 8}}>
                        {products.map((product) => (
                            <button
                                key={product.barcode || product.name}
                                type="button"
                                onClick={() => handleProductClick(product.name)}
                                style={{
                                    textAlign: 'left',
                                    border: selectedName === product.name ? '1px solid var(--accent)' : '1px solid var(--border)',
                                    borderRadius: 8,
                                    background: selectedName === product.name ? 'var(--active-bg)' : 'white',
                                    padding: 12,
                                    cursor: 'pointer',
                                    color: 'inherit',
                                }}
                            >
                                <div style={{fontWeight: 600}}>{product.name}</div>
                                <div className="text-muted" style={{fontSize: 13}}>
                                    {toCategoryLabel(product.category || 'OTHERS')} - {formatPriceMxn(product.price)}
                                </div>
                            </button>
                        ))}
                    </div>
                </div>

                <div className="card product-detail-card" style={{padding: 16}}>
                    <h2 style={{marginTop: 0, fontSize: 18}}>Detalle del producto</h2>
                    {!selectedName && !isCollapsingDetail &&
                        <p className="text-muted">Selecciona un producto para ver su informacion.</p>}

                    <div className={`product-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                        {showDetailContent && (
                            <>
                                {loadingDetail && <p className="text-muted">Cargando detalle...</p>}
                                {detailError && <p className="text-muted">{detailError}</p>}

                                {detail && !loadingDetail && (
                                    <>
                                        <div style={{display: 'grid', gap: 10, marginBottom: 14}}>
                                            <input className="input" value={detail.name || ''} disabled={!editing}
                                                   onChange={(e) => setDetail((prev) => ({
                                                       ...prev,
                                                       name: e.target.value
                                                   }))}/>
                                            <input className="input" value={detail.barcode || ''} disabled={!editing}
                                                   onChange={(e) => setDetail((prev) => ({
                                                       ...prev,
                                                       barcode: e.target.value
                                                   }))}/>
                                            <input className="input" value={detail.description || ''}
                                                   disabled={!editing} onChange={(e) => setDetail((prev) => ({
                                                ...prev,
                                                description: e.target.value
                                            }))}/>
                                            <select className="input" value={detail.category || 'OTHERS'}
                                                    disabled={!editing} onChange={(e) => setDetail((prev) => ({
                                                ...prev,
                                                category: e.target.value
                                            }))}>
                                                {CATEGORY_OPTIONS_ES.map((category) => (
                                                    <option key={category.value}
                                                            value={category.value}>{category.label}</option>
                                                ))}
                                            </select>
                                            <input className="input" type="number" min="0" step="0.01"
                                                   value={detail.price ?? ''} disabled={!editing}
                                                   onChange={(e) => setDetail((prev) => ({
                                                       ...prev,
                                                       price: e.target.value
                                                   }))}/>
                                            <input className="input" type="number" min="0" step="1"
                                                   value={detail.stock ?? ''} disabled={!editing}
                                                   onChange={(e) => setDetail((prev) => ({
                                                       ...prev,
                                                       stock: e.target.value
                                                   }))}/>
                                            <input className="input" value={detail.imageUrl || ''} disabled={!editing}
                                                   onChange={(e) => setDetail((prev) => ({
                                                       ...prev,
                                                       imageUrl: e.target.value
                                                   }))}/>
                                            <select className="input" value={detail.supplierName || ''}
                                                    disabled={!editing} onChange={(e) => setDetail((prev) => ({
                                                ...prev,
                                                supplierName: e.target.value
                                            }))}>
                                                <option value="">Selecciona proveedor</option>
                                                {suppliers.map((supplier) => (
                                                    <option key={supplier.name}
                                                            value={supplier.name}>{supplier.name}</option>
                                                ))}
                                            </select>
                                            <input className="input" type="number" min="0" step="0.01"
                                                   value={detail.supplierPrice ?? ''} disabled={!editing}
                                                   onChange={(e) => setDetail((prev) => ({
                                                       ...prev,
                                                       supplierPrice: e.target.value
                                                   }))}/>
                                        </div>

                                        {isAdmin && (
                                            <div style={{display: 'flex', gap: 8, marginBottom: 12}}>
                                                {!editing && <button className="btn-primary" type="button"
                                                                     onClick={() => setEditing(true)}>Editar</button>}
                                                {editing &&
                                                    <button className="btn-primary" type="button" disabled={saving}
                                                            onClick={saveChanges}>Guardar cambios</button>}
                                                {editing && <button className="btn-ghost" type="button" onClick={() => {
                                                    setEditing(false);
                                                    loadDetail(selectedName)
                                                }}>Cancelar</button>}
                                                <button className="btn-ghost" type="button"
                                                        onClick={deleteProduct}>Eliminar
                                                </button>
                                            </div>
                                        )}

                                        <h3 style={{marginBottom: 8}}>Proveedor(es) asociado(s)</h3>
                                        {Array.isArray(detail.suppliers) && detail.suppliers.length > 0 ? (
                                            <div style={{display: 'grid', gap: 8}}>
                                                {detail.suppliers.map((supplier) => (
                                                    <div key={`${detail.name}-${supplier.name}`} style={{
                                                        border: '1px solid var(--border)',
                                                        borderRadius: 8,
                                                        padding: 10
                                                    }}>
                                                        <div style={{fontWeight: 600}}>{supplier.name}</div>
                                                        <div className="text-muted" style={{fontSize: 13}}>
                                                            Precio proveedor: {formatPriceMxn(supplier.supplyPrice)}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <p className="text-muted" style={{marginTop: 0}}>
                                                Este producto no tiene proveedores asociados.
                                            </p>
                                        )}
                                    </>
                                )}
                            </>
                        )}
                    </div>
                </div>
            </section>
        </div>
    )
}

