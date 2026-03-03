import React, { useContext, useEffect, useMemo, useState } from 'react'
import api from '../services/api'
import { AuthContext } from '../context/AuthContext'

const initialForm = {
  name: '',
  contactEmail: '',
  phoneNumber: '',
  address: '',
}

function normalizeError(error, fallback) {
  return error?.response?.data?.message || fallback
}

export default function Suppliers() {
  const { user } = useContext(AuthContext)
  const isAdmin = useMemo(() => ['ADMIN', 'ROOT'].includes(user?.role || ''), [user])

  const [suppliers, setSuppliers] = useState([])
  const [loadingList, setLoadingList] = useState(true)
  const [listError, setListError] = useState('')

  const [form, setForm] = useState(initialForm)
  const [formMessage, setFormMessage] = useState('')
  const [saving, setSaving] = useState(false)

  const [selectedName, setSelectedName] = useState(null)
  const [detail, setDetail] = useState(null)
  const [products, setProducts] = useState([])
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [editing, setEditing] = useState(false)

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

  const loadDetail = async (name) => {
    setSelectedName(name)
    setLoadingDetail(true)
    setDetailError('')
    setEditing(false)
    try {
      const [detailRes, productsRes] = await Promise.all([
        api.get(`/suppliers/${encodeURIComponent(name)}`),
        api.get(`/suppliers/${encodeURIComponent(name)}/products`).catch(() => ({ data: [] })),
      ])

      setDetail(detailRes.data)
      setProducts(Array.isArray(productsRes.data) ? productsRes.data : [])
    } catch (error) {
      setDetailError(normalizeError(error, 'No se pudo cargar el detalle del proveedor.'))
      setDetail(null)
      setProducts([])
    } finally {
      setLoadingDetail(false)
    }
  }

  const handleSupplierClick = (name) => {
    if (selectedName === name) {
      setSelectedName(null)
      setDetail(null)
      setProducts([])
      setDetailError('')
      setEditing(false)
      return
    }

    loadDetail(name)
  }

  useEffect(() => {
    loadSuppliers()
  }, [])

  const onChangeForm = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const buildPayload = (source) => ({
    name: source.name.trim(),
    contactEmail: source.contactEmail.trim(),
    phoneNumber: source.phoneNumber.trim(),
    address: source.address.trim(),
  })

  const addSupplier = async (event) => {
    event.preventDefault()
    if (!isAdmin) return

    setSaving(true)
    setFormMessage('')
    try {
      const payload = buildPayload(form)
      await api.post('/suppliers/add', payload)
      setForm(initialForm)
      setFormMessage('Proveedor agregado correctamente.')
      await loadSuppliers()
    } catch (error) {
      setFormMessage(normalizeError(error, 'No se pudo agregar el proveedor.'))
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

  return (
    <div style={{ display: 'grid', gap: 16 }}>
      <section className="card" style={{ padding: 20 }}>
        <h1 style={{ marginTop: 0, marginBottom: 6 }}>Proveedores</h1>
        <p className="text-muted" style={{ marginTop: 0 }}>
          Consulta proveedores disponibles y revisa sus productos asociados.
        </p>
      </section>

      {isAdmin && (
        <section className="card" style={{ padding: 20 }}>
          <h2 style={{ marginTop: 0, marginBottom: 12, fontSize: 18 }}>Agregar proveedor</h2>
          <form onSubmit={addSupplier} style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12 }}>
            <input className="input" placeholder="Nombre" value={form.name} onChange={(e) => onChangeForm('name', e.target.value)} required />
            <input className="input" placeholder="Correo" type="email" value={form.contactEmail} onChange={(e) => onChangeForm('contactEmail', e.target.value)} required />
            <input className="input" placeholder="Telefono" value={form.phoneNumber} onChange={(e) => onChangeForm('phoneNumber', e.target.value)} required />
            <input className="input" placeholder="Direccion" value={form.address} onChange={(e) => onChangeForm('address', e.target.value)} required />
            <div style={{ gridColumn: '1 / -1' }}>
              <button className="btn-primary" type="submit" disabled={saving}>
                {saving ? 'Guardando...' : 'Agregar proveedor'}
              </button>
            </div>
          </form>
          {formMessage && <p className="text-muted" style={{ marginBottom: 0 }}>{formMessage}</p>}
        </section>
      )}

      {!isAdmin && (
        <section className="card" style={{ padding: 16 }}>
          <p className="text-muted" style={{ margin: 0 }}>
            Tu rol ({user?.role || 'USER'}) permite consulta. Solo administradores pueden agregar, editar o eliminar proveedores.
          </p>
        </section>
      )}

      <section style={{ display: 'grid', gridTemplateColumns: 'minmax(280px, 1fr) minmax(360px, 1.2fr)', gap: 16 }}>
        <div className="card" style={{ padding: 16 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Lista de proveedores</h2>
          {loadingList && <p className="text-muted">Cargando proveedores...</p>}
          {!loadingList && listError && <p className="text-muted">{listError}</p>}
          {!loadingList && !listError && suppliers.length === 0 && <p className="text-muted">No hay proveedores registrados.</p>}
          <div style={{ display: 'grid', gap: 8 }}>
            {suppliers.map((supplier) => (
              <button
                key={supplier.name}
                type="button"
                onClick={() => handleSupplierClick(supplier.name)}
                style={{
                  textAlign: 'left',
                  border: selectedName === supplier.name ? '1px solid var(--accent)' : '1px solid var(--border)',
                  borderRadius: 8,
                  background: selectedName === supplier.name ? 'var(--active-bg)' : 'white',
                  padding: 12,
                  cursor: 'pointer',
                  color : 'inherit',
                }}
              >
                <div style={{ fontWeight: 600 }}>{supplier.name}</div>
                <div className="text-muted" style={{ fontSize: 13 }}>{supplier.contactEmail}</div>
              </button>
            ))}
          </div>
        </div>

        <div className="card" style={{ padding: 16 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Detalle del proveedor</h2>
          {!selectedName && <p className="text-muted">Selecciona un proveedor para ver su informacion.</p>}
          {loadingDetail && <p className="text-muted">Cargando detalle...</p>}
          {detailError && <p className="text-muted">{detailError}</p>}

          {detail && !loadingDetail && (
            <>
              <div style={{ display: 'grid', gap: 10, marginBottom: 14 }}>
                <input className="input" value={detail.name} disabled={!editing} onChange={(e) => setDetail((prev) => ({ ...prev, name: e.target.value }))} />
                <input className="input" type="email" value={detail.contactEmail} disabled={!editing} onChange={(e) => setDetail((prev) => ({ ...prev, contactEmail: e.target.value }))} />
                <input className="input" value={detail.phoneNumber} disabled={!editing} onChange={(e) => setDetail((prev) => ({ ...prev, phoneNumber: e.target.value }))} />
                <input className="input" value={detail.address || ''} disabled={!editing} onChange={(e) => setDetail((prev) => ({ ...prev, address: e.target.value }))} />
              </div>

              {isAdmin && (
                <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                  {!editing && <button className="btn-primary" type="button" onClick={() => setEditing(true)}>Editar</button>}
                  {editing && <button className="btn-primary" type="button" disabled={saving} onClick={saveChanges}>Guardar cambios</button>}
                  {editing && <button className="btn-ghost" type="button" onClick={() => { setEditing(false); loadDetail(selectedName) }}>Cancelar</button>}
                  <button className="btn-ghost" type="button" onClick={deleteSupplier}>Eliminar</button>
                </div>
              )}

              <h3 style={{ marginBottom: 8 }}>Productos asociados</h3>
              {products.length === 0 ? (
                <p className="text-muted" style={{ marginTop: 0 }}>Este proveedor no tiene productos asociados.</p>
              ) : (
                <div style={{ display: 'grid', gap: 8 }}>
                  {products.map((product) => (
                    <div key={product.name} style={{ border: '1px solid var(--border)', borderRadius: 8, padding: 10 }}>
                      <div style={{ fontWeight: 600 }}>{product.name}</div>
                      <div className="text-muted" style={{ fontSize: 13 }}>
                        ${product.price} - Stock: {product.stock}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </section>
    </div>
  )
}
