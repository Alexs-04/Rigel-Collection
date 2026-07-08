import {useEffect, useRef, useState} from 'react'
import type {FormEvent} from 'react'
import api from '../../services/api'
import {loadDetailSafely} from '../../utils/detailLoader'
import type {UserDetail, UserFormValues, UserSummary} from '../../types/users'

const initialForm: UserFormValues = {
    name: '',
    username: '',
    email: '',
    phoneNumber: '',
    role: 'USER',
}

function normalizeError(error: unknown, fallback: string): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || fallback
}

function buildEditPayload(source: Partial<UserDetail>) {
    return {
        name: String(source.name || '').trim(),
        username: String(source.username || '').trim(),
        email: String(source.email || '').trim(),
        phoneNumber: String(source.phoneNumber || '').trim(),
        role: String(source.role || 'USER').trim().toUpperCase(),
    }
}

interface UseUsersPageStateReturn {
    users: UserSummary[]
    loadingList: boolean
    listError: string
    search: string
    setSearch: (search: string) => void
    form: UserFormValues
    formMessage: string
    saving: boolean
    selectedId: number | null
    detail: UserDetail | null
    loadingDetail: boolean
    detailError: string
    editing: boolean
    isCollapsingDetail: boolean
    detailExpanded: boolean
    showDetailContent: boolean
    setEditing: (editing: boolean) => void
    onChangeForm: (key: keyof UserFormValues | 'password', value: string) => void
    onChangeDetail: (key: keyof UserDetail | 'password', value: string) => void
    addUser: (event: FormEvent<HTMLFormElement>) => Promise<void>
    saveChanges: () => Promise<void>
    toggleUserStatus: () => Promise<void>
    handleUserClick: (selectedUser: UserSummary) => void
    loadDetail: (id: number) => Promise<void>
}

export function useUsersPageState({isRoot}: {isRoot: boolean}): UseUsersPageStateReturn {
    const [users, setUsers] = useState<UserSummary[]>([])
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')
    const [search, setSearch] = useState('')

    const [form, setForm] = useState<UserFormValues>(initialForm)
    const [formMessage, setFormMessage] = useState('')
    const [saving, setSaving] = useState(false)

    const [selectedId, setSelectedId] = useState<number | null>(null)
    const [detail, setDetail] = useState<UserDetail | null>(null)
    const [loadingDetail, setLoadingDetail] = useState(false)
    const [detailError, setDetailError] = useState('')
    const [editing, setEditing] = useState(false)

    const collapseTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
    const detailRequestRef = useRef(0)
    const [isCollapsingDetail, setIsCollapsingDetail] = useState(false)

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

    const loadDetail = async (id: number) => {
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
                const cached = users.find((item) => item.id === id)
                if (cached) return cached

                const res = await api.get('/consumer/api/users')
                const list = Array.isArray(res.data) ? (res.data as UserSummary[]) : []
                const found = list.find((item) => item.id === id)
                if (!found) throw new Error('Usuario no encontrado')
                return found
            },
            onSuccess: (resolvedDetail: UserDetail) => {
                setDetail(resolvedDetail)
            },
            onError: (error: unknown) => {
                setDetailError(normalizeError(error, 'No se pudo cargar el detalle del usuario.'))
                setDetail(null)
            },
            onFinally: () => {
                setLoadingDetail(false)
            },
        })
    }

    const handleUserClick = (selectedUser: UserSummary) => {
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

    const onChangeForm = (key: keyof UserFormValues | 'password', value: string) => {
        setForm((prev) => ({...prev, [key]: value}))
    }

    const onChangeDetail = (key: keyof UserDetail | 'password', value: string) => {
        setDetail((prev) => (prev ? {...prev, [key]: value} : prev))
    }

    // Sends an invitation email — no password required.
    // The user sets their own password when they click the activation link.
    const addUser = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        if (!isRoot) return

        setSaving(true)
        setFormMessage('')

        try {
            const payload = {
                name: form.name.trim(),
                username: form.username.trim(),
                email: form.email.trim(),
                phoneNumber: form.phoneNumber.trim(),
                role: form.role,
            }

            const res = await api.post('/admin/users/invite', payload)
            const message: string = res.data?.message || 'Invitacion enviada.'

            setForm(initialForm)
            setFormMessage(message)
            await loadUsers(search)
        } catch (error) {
            const fallback = error instanceof Error ? error.message : 'No se pudo enviar la invitacion.'
            setFormMessage(normalizeError(error, fallback))
        } finally {
            setSaving(false)
        }
    }

    const saveChanges = async () => {
        if (!detail || !isRoot) return

        setSaving(true)
        setDetailError('')

        try {
            const payload = buildEditPayload(detail)
            await api.put(`/consumer/api/users/${detail.id}`, payload)
            setEditing(false)
            await loadUsers(search)
            await loadDetail(detail.id)
        } catch (error) {
            const fallback = error instanceof Error ? error.message : 'No se pudo actualizar el usuario.'
            setDetailError(normalizeError(error, fallback))
        } finally {
            setSaving(false)
        }
    }

    const toggleUserStatus = async () => {
        if (!detail || !isRoot) return

        const nextStatus = !detail.active
        const confirmed = window.confirm(`${nextStatus ? 'Activar' : 'Desactivar'} al usuario ${detail.username}?`)
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

    return {
        users,
        loadingList,
        listError,
        search,
        setSearch,
        form,
        formMessage,
        saving,
        selectedId,
        detail,
        loadingDetail,
        detailError,
        editing,
        isCollapsingDetail,
        detailExpanded: Boolean(selectedId) || loadingDetail,
        showDetailContent: Boolean(detail || detailError || loadingDetail || isCollapsingDetail),
        setEditing,
        onChangeForm,
        onChangeDetail,
        addUser,
        saveChanges,
        toggleUserStatus,
        handleUserClick,
        loadDetail,
    } as UseUsersPageStateReturn
}