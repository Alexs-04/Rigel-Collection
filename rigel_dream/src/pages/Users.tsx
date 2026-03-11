// @ts-ignore
import React, {useContext, useEffect, useMemo, useRef, useState} from 'react'
import api from '../services/api'
import {AuthContext} from '../context/AuthContext'
import {loadDetailSafely} from '../utils/detailLoader'

const initialForm = {
    name: '',
    username: '',
    email: '',
    phoneNumber: '',
    role: 'USER',
    password: '',
}

const roleOptions = ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']

function normalizeError(error, fallback) {
    return error?.response?.data?.message || fallback
}

export default function Users() {
    const {user} = useContext(AuthContext)
    const isRoot = useMemo(() => user?.role === 'ROOT', [user])

    const [users, setUsers] = useState([])
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')
    const [search, setSearch] = useState('')

    const [form, setForm] = useState(initialForm)
    const [formMessage, setFormMessage] = useState('')
    const [saving, setSaving] = useState(false)

    const [selectedId, setSelectedId] = useState(null)
    const [detail, setDetail] = useState(null)
    const [loadingDetail, setLoadingDetail] = useState(false)
    const [detailError, setDetailError] = useState('')
    const [editing, setEditing] = useState(false)

    const collapseTimeoutRef = useRef(null)
    const detailRequestRef = useRef(0)
    const [isCollapsingDetail, setIsCollapsingDetail] = useState(false)

    // @ts-ignore
    const loadUsers = async (rawSearch = '') => {
        setLoadingList(true)
        setListError('')
        try {
            const params = rawSearch.trim() ? {search: rawSearch.trim()} : undefined
            const res = await api.get('/consumer/api/users', {params})
            setUsers(Array.isArray(res.data) ? res.data : [])
        } catch (error) {
            setListError(normalizeError(error, 'No se pudo cargar la lista de usuarios.'))
        } finally {
            setLoadingList(false)
        }
    }

    // @ts-ignore
    const loadDetail = async (id) => {
        if (collapseTimeoutRef.current) {
            clearTimeout(collapseTimeoutRef.current)
            collapseTimeoutRef.current = null
        }

        setSelectedId(id)
        setIsCollapsingDetail(false)
        setLoadingDetail(true)
        setDetailError('')
        setEditing(false)

        await loadDetailSafely({
            requestRef: detailRequestRef,
            fetchDetail: async () => {
                const res = await api.get('/consumer/api/users')
                const list = Array.isArray(res.data) ? res.data : []
                // @ts-ignore
                const found = list.find((item) => item.id === id)
                if (!found) throw new Error('Usuario no encontrado')
                return found
            },
            onSuccess: (resolvedDetail) => {
                setDetail({...resolvedDetail, password: ''})
            },
            onError: (error) => {
                setDetailError(normalizeError(error, 'No se pudo cargar el detalle del usuario.'))
                setDetail(null)
            },
            onFinally: () => {
                setLoadingDetail(false)
            },
        })
    }

    const handleUserClick = (selectedUser) => {
        if (selectedId === selectedUser.id) {
            detailRequestRef.current += 1
            setLoadingDetail(false)
            setSelectedId(null)
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

        loadDetail(selectedUser.id)
    }

    useEffect(() => {
        loadUsers('')
    }, [])

    useEffect(() => {
        const timeout = setTimeout(() => {
            loadUsers(search)
        }, 250)
        return () => clearTimeout(timeout)
    }, [search])

    useEffect(() => {
        return () => {
            if (collapseTimeoutRef.current) clearTimeout(collapseTimeoutRef.current)
        }
    }, [])

    const onChangeForm = (key, value) => {
        setForm((prev) => ({...prev, [key]: value}))
    }

    const buildPayload = (source, includePassword = true) => {
        const payload = {
            name: (source.name || '').trim(),
            username: (source.username || '').trim(),
            email: (source.email || '').trim(),
            phoneNumber: (source.phoneNumber || '').trim(),
            role: (source.role || 'USER').trim().toUpperCase(),
            password: source.password || '',
        }

        const password = (source.password || '').trim()
        if (includePassword || password) {
            payload.password = password
        }
        return payload
    }

    // @ts-ignore
    const addUser = async (event) => {
        event.preventDefault()
        if (!isRoot) return

        setSaving(true)
        setFormMessage('')

        try {
            const payload = buildPayload(form, true)
            if (!payload.password) {
                throw new Error('La contraseña es obligatoria al crear usuarios.')
            }

            await api.post('/consumer/api/users', payload)
            setForm(initialForm)
            setFormMessage('Usuario agregado correctamente.')
            await loadUsers(search)
        } catch (error) {
            setFormMessage(normalizeError(error, error.message || 'No se pudo agregar el usuario.'))
        } finally {
            setSaving(false)
        }
    }

    // @ts-ignore
    const saveChanges = async () => {
        if (!detail || !isRoot) return

        setSaving(true)
        setDetailError('')

        try {
            const payload = buildPayload(detail, false)
            await api.put(`/consumer/api/users/${detail.id}`, payload)
            setEditing(false)
            await loadUsers(search)
            await loadDetail(detail.id)
        } catch (error) {
            setDetailError(normalizeError(error, error.message || 'No se pudo actualizar el usuario.'))
        } finally {
            setSaving(false)
        }
    }

    // @ts-ignore
    const toggleUserStatus = async () => {
        if (!detail || !isRoot) return

        const nextStatus = !detail.active
        const confirmed = window.confirm(
            `${nextStatus ? 'Activar' : 'Desactivar'} al usuario ${detail.username}?`
        )
        if (!confirmed) return

        setSaving(true)
        setDetailError('')
        try {
            await api.patch(`/consumer/api/users/${detail.id}/status`, {active: nextStatus})
            await loadUsers(search)
            await loadDetail(detail.id)
        } catch (error) {
            setDetailError(normalizeError(error, 'No se pudo cambiar el estado del usuario.'))
        } finally {
            setSaving(false)
        }
    }

    const detailExpanded = Boolean(selectedId) || loadingDetail
    const showDetailContent = Boolean(detail || detailError || loadingDetail || isCollapsingDetail)

    return (
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Usuarios</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Gestiona cuentas registradas. Solo rol ROOT puede acceder y administrar usuarios.
                </p>
            </section>

            {!isRoot && (
                <section className="card" style={{padding: 16}}>
                    <p className="text-muted" style={{margin: 0}}>
                        Acceso denegado. Esta seccion es exclusiva para usuarios ROOT.
                    </p>
                </section>
            )}

            {isRoot && (
                <>
                    <section className="card" style={{padding: 20}}>
                        <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Agregar usuario</h2>
                        <form onSubmit={addUser}
                              style={{display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 12}}>
                            <input className="input" placeholder="Nombre" value={form.name}
                                   onChange={(e) => onChangeForm('name', e.target.value)} required/>
                            <input className="input" placeholder="Usuario" value={form.username}
                                   onChange={(e) => onChangeForm('username', e.target.value)} required/>
                            <input className="input" placeholder="Correo" type="email" value={form.email}
                                   onChange={(e) => onChangeForm('email', e.target.value)} required/>
                            <input className="input" placeholder="Telefono" value={form.phoneNumber}
                                   onChange={(e) => onChangeForm('phoneNumber', e.target.value)}/>
                            <select className="input" value={form.role}
                                    onChange={(e) => onChangeForm('role', e.target.value)}>
                                {roleOptions.map((role) => (
                                    <option key={role} value={role}>{role}</option>
                                ))}
                            </select>
                            <input className="input" placeholder="Contrasena" type="password" value={form.password}
                                   onChange={(e) => onChangeForm('password', e.target.value)} required/>
                            <div style={{gridColumn: '1 / -1'}}>
                                <button className="btn-primary" type="submit" disabled={saving}>
                                    {saving ? 'Guardando...' : 'Agregar usuario'}
                                </button>
                            </div>
                        </form>
                        {formMessage && <p className="text-muted" style={{marginBottom: 0}}>{formMessage}</p>}
                    </section>

                    <section
                        style={{display: 'grid', gridTemplateColumns: 'minmax(300px, 1fr) minmax(380px, 1.2fr)', gap: 16}}>
                        <div className="card" style={{padding: 16}}>
                            <h2 style={{marginTop: 0, fontSize: 18}}>Lista de usuarios</h2>
                            <input
                                className="input"
                                placeholder="Buscar por nombre, usuario o correo"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                style={{marginBottom: 12}}
                            />

                            {loadingList && <p className="text-muted">Cargando usuarios...</p>}
                            {!loadingList && listError && <p className="text-muted">{listError}</p>}
                            {!loadingList && !listError && users.length === 0 &&
                                <p className="text-muted">No hay usuarios registrados.</p>}

                            <div style={{display: 'grid', gap: 8}}>
                                {users.map((item) => (
                                    <button
                                        key={item.id}
                                        type="button"
                                        onClick={() => handleUserClick(item)}
                                        style={{
                                            textAlign: 'left',
                                            border: selectedId === item.id ? '1px solid var(--accent)' : '1px solid var(--border)',
                                            borderRadius: 8,
                                            background: selectedId === item.id ? 'var(--active-bg)' : 'white',
                                            padding: 12,
                                            cursor: 'pointer',
                                            color: 'inherit',
                                        }}
                                    >
                                        <div style={{display: 'flex', justifyContent: 'space-between', gap: 8}}>
                                            <strong>{item.username}</strong>
                                            <span className="text-muted" style={{fontSize: 12}}>
                                                {item.active ? 'Activo' : 'Inactivo'}
                                            </span>
                                        </div>
                                        <div className="text-muted" style={{fontSize: 13}}>{item.email}</div>
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="card supplier-detail-card" style={{padding: 16}}>
                            <h2 style={{marginTop: 0, fontSize: 18}}>Detalle del usuario</h2>
                            {!selectedId && !isCollapsingDetail &&
                                <p className="text-muted">Selecciona un usuario para ver su informacion.</p>}

                            <div className={`supplier-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                                {showDetailContent && (
                                    <>
                                        {loadingDetail && <p className="text-muted">Cargando detalle...</p>}
                                        {detailError && <p className="text-muted">{detailError}</p>}

                                        {detail && !loadingDetail && (
                                            <>
                                                <div style={{display: 'grid', gap: 10, marginBottom: 14}}>
                                                    <input className="input" value={detail.name || ''}
                                                           disabled={!editing}
                                                           onChange={(e) => setDetail((prev) => ({
                                                               ...prev,
                                                               name: e.target.value
                                                           }))}/>
                                                    <input className="input" value={detail.username || ''}
                                                           disabled={!editing}
                                                           onChange={(e) => setDetail((prev) => ({
                                                               ...prev,
                                                               username: e.target.value
                                                           }))}/>
                                                    <input className="input" type="email" value={detail.email || ''}
                                                           disabled={!editing}
                                                           onChange={(e) => setDetail((prev) => ({
                                                               ...prev,
                                                               email: e.target.value
                                                           }))}/>
                                                    <input className="input" value={detail.phoneNumber || ''}
                                                           disabled={!editing}
                                                           onChange={(e) => setDetail((prev) => ({
                                                               ...prev,
                                                               phoneNumber: e.target.value
                                                           }))}/>
                                                    <select className="input" value={detail.role || 'USER'}
                                                            disabled={!editing}
                                                            onChange={(e) => setDetail((prev) => ({
                                                                ...prev,
                                                                role: e.target.value
                                                            }))}>
                                                        {roleOptions.map((role) => (
                                                            <option key={role} value={role}>{role}</option>
                                                        ))}
                                                    </select>
                                                    {editing && (
                                                        <input className="input" type="password"
                                                               placeholder="Nueva contrasena (opcional)"
                                                               value={detail.password || ''}
                                                               onChange={(e) => setDetail((prev) => ({
                                                                   ...prev,
                                                                   password: e.target.value
                                                               }))}/>
                                                    )}
                                                    <input className="input" value={detail.active ? 'Activo' : 'Inactivo'}
                                                           disabled/>
                                                </div>

                                                <div style={{display: 'flex', gap: 8, marginBottom: 12}}>
                                                    {!editing && <button className="btn-primary" type="button"
                                                                         onClick={() => setEditing(true)}>Editar</button>}
                                                    {editing &&
                                                        <button className="btn-primary" type="button" disabled={saving}
                                                                onClick={saveChanges}>Guardar cambios</button>}
                                                    {editing &&
                                                        <button className="btn-ghost" type="button" onClick={() => {
                                                            setEditing(false)
                                                            loadDetail(detail.id)
                                                        }}>Cancelar</button>}
                                                    <button className="btn-ghost" type="button" disabled={saving}
                                                            onClick={toggleUserStatus}>
                                                        {detail.active ? 'Desactivar' : 'Activar'}
                                                    </button>
                                                </div>
                                            </>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </section>
                </>
            )}
        </div>
    )
}

