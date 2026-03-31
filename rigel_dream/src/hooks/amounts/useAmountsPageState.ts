import {useEffect, useMemo, useState} from 'react'
import type {FormEvent} from 'react'
import {
    createAmount,
    deleteAmount,
    fetchAmounts,
    fetchAmountTypes,
    markAmountAsBoughtOut,
    markAmountAsReturned,
    updateAmount,
} from '../../services/amountsService'
import type {AmountFormValues, AmountItem, ContainerTypeOption} from '../../types/amounts'

function toDateTimeLocalValue(date: Date): string {
    const offset = date.getTimezoneOffset()
    const local = new Date(date.getTime() - offset * 60_000)
    return local.toISOString().slice(0, 16)
}

function buildDefaultForm(type = 'BEER_CONTAINER'): AmountFormValues {
    const expiration = new Date()
    expiration.setDate(expiration.getDate() + 15)

    return {
        description: '',
        type,
        customerName: '',
        quantity: '1',
        saleUnitPrice: '10',
        buyoutUnitPrice: '5',
        expirationDate: toDateTimeLocalValue(expiration),
        notes: '',
    }
}

function normalizeError(error: unknown, fallback: string): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || fallback
}

function toPayload(form: AmountFormValues) {
    const quantity = Number(form.quantity)
    const saleUnitPrice = Number(form.saleUnitPrice)
    const buyoutUnitPrice = form.buyoutUnitPrice.trim() ? Number(form.buyoutUnitPrice) : null

    if (!form.customerName.trim()) throw new Error('El nombre del cliente es obligatorio.')
    if (!Number.isInteger(quantity) || quantity <= 0) throw new Error('La cantidad debe ser mayor a 0.')
    if (!Number.isFinite(saleUnitPrice) || saleUnitPrice < 0) throw new Error('Precio de venta no valido.')
    if (buyoutUnitPrice !== null && (!Number.isFinite(buyoutUnitPrice) || buyoutUnitPrice < 0)) {
        throw new Error('Precio de compra no valido.')
    }
    if (!form.expirationDate) throw new Error('La fecha limite es obligatoria.')

    return {
        description: form.description.trim(),
        type: form.type,
        customerName: form.customerName.trim(),
        quantity,
        saleUnitPrice,
        buyoutUnitPrice,
        expirationDate: new Date(form.expirationDate).toISOString(),
        notes: form.notes.trim(),
    }
}

export function useAmountsPageState(isAdmin: boolean) {
    const [amounts, setAmounts] = useState<AmountItem[]>([])
    const [types, setTypes] = useState<ContainerTypeOption[]>([])
    const [search, setSearch] = useState('')
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [form, setForm] = useState<AmountFormValues>(buildDefaultForm)
    const [saving, setSaving] = useState(false)
    const [formMessage, setFormMessage] = useState('')

    const [selectedFolio, setSelectedFolio] = useState<number | null>(null)
    const [editing, setEditing] = useState(false)
    const [buyoutPrice, setBuyoutPrice] = useState('')

    const loadData = async () => {
        setLoading(true)
        setError('')
        try {
            const [loadedAmounts, loadedTypes] = await Promise.all([fetchAmounts(), fetchAmountTypes()])
            setAmounts(loadedAmounts)
            setTypes(loadedTypes)
            if (!form.type && loadedTypes[0]) {
                setForm(buildDefaultForm(String(loadedTypes[0].value || 'BEER_CONTAINER')))
            }
        } catch (e) {
            setError(normalizeError(e, 'No se pudieron cargar los importes.'))
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadData()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const filteredAmounts = useMemo(() => {
        const normalized = search.trim().toLowerCase()
        if (!normalized) return amounts
        return amounts.filter((item) => {
            const bag = [
                item.folio,
                item.customerName,
                item.typeLabel,
                item.ownerUsername,
                item.status,
                item.description,
            ]
            return bag.some((value) => String(value || '').toLowerCase().includes(normalized))
        })
    }, [amounts, search])

    const selectedAmount = useMemo(
        () => amounts.find((item) => item.folio === selectedFolio) || null,
        [amounts, selectedFolio]
    )

    const onChangeForm = (key: keyof AmountFormValues, value: string) => {
        setForm((prev) => ({...prev, [key]: value}))

        if (key === 'type') {
            const match = types.find((item) => item.value === value)
            if (match) {
                setForm((prev) => ({
                    ...prev,
                    type: value,
                    saleUnitPrice: String(match.suggestedSalePrice),
                    buyoutUnitPrice: String(match.suggestedBuyoutPrice),
                }))
            }
        }
    }

    const submitCreate = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        setSaving(true)
        setFormMessage('')
        try {
            const payload = toPayload(form)
            await createAmount(payload)
            setFormMessage('Importe registrado correctamente.')
            setForm(buildDefaultForm(form.type))
            await loadData()
        } catch (e) {
            const fallback = e instanceof Error ? e.message : 'No se pudo registrar el importe.'
            setFormMessage(normalizeError(e, fallback))
        } finally {
            setSaving(false)
        }
    }

    const startEdit = () => {
        if (!isAdmin || !selectedAmount) return
        setEditing(true)
        setForm({
            description: selectedAmount.description || '',
            type: selectedAmount.type,
            customerName: selectedAmount.customerName || '',
            quantity: String(selectedAmount.quantity),
            saleUnitPrice: String(selectedAmount.saleUnitPrice),
            buyoutUnitPrice: String(selectedAmount.buyoutUnitPrice ?? ''),
            expirationDate: toDateTimeLocalValue(new Date(selectedAmount.expirationDate)),
            notes: selectedAmount.notes || '',
        })
    }

    const cancelEdit = () => {
        setEditing(false)
        setForm(buildDefaultForm(types[0]?.value || 'BEER_CONTAINER'))
    }

    const submitEdit = async () => {
        if (!selectedAmount || !isAdmin) return
        setSaving(true)
        setFormMessage('')
        try {
            const payload = toPayload(form)
            await updateAmount(selectedAmount.folio, payload)
            setFormMessage('Importe actualizado correctamente.')
            setEditing(false)
            await loadData()
        } catch (e) {
            const fallback = e instanceof Error ? e.message : 'No se pudo actualizar el importe.'
            setFormMessage(normalizeError(e, fallback))
        } finally {
            setSaving(false)
        }
    }

    const submitReturn = async () => {
        if (!selectedAmount) return
        setSaving(true)
        setFormMessage('')
        try {
            await markAmountAsReturned(selectedAmount.folio)
            setFormMessage('Importe marcado como devuelto.')
            await loadData()
        } catch (e) {
            setFormMessage(normalizeError(e, 'No se pudo marcar como devuelto.'))
        } finally {
            setSaving(false)
        }
    }

    const submitBuyout = async () => {
        if (!selectedAmount) return
        const parsedPrice = Number(buyoutPrice || selectedAmount.buyoutUnitPrice || 0)
        if (!Number.isFinite(parsedPrice) || parsedPrice < 0) {
            setFormMessage('Precio de compra no valido.')
            return
        }

        setSaving(true)
        setFormMessage('')
        try {
            await markAmountAsBoughtOut(selectedAmount.folio, {buyoutUnitPrice: parsedPrice})
            setFormMessage('Compra del importe registrada.')
            setBuyoutPrice('')
            await loadData()
        } catch (e) {
            setFormMessage(normalizeError(e, 'No se pudo registrar la compra del importe.'))
        } finally {
            setSaving(false)
        }
    }

    const submitDelete = async () => {
        if (!selectedAmount || !isAdmin) return
        if (!window.confirm(`Eliminar el importe #${selectedAmount.folio}?`)) return

        setSaving(true)
        setFormMessage('')
        try {
            await deleteAmount(selectedAmount.folio)
            setFormMessage('Importe eliminado correctamente.')
            setSelectedFolio(null)
            await loadData()
        } catch (e) {
            setFormMessage(normalizeError(e, 'No se pudo eliminar el importe.'))
        } finally {
            setSaving(false)
        }
    }

    return {
        amounts: filteredAmounts,
        allAmounts: amounts,
        types,
        search,
        setSearch,
        loading,
        error,
        form,
        saving,
        formMessage,
        selectedFolio,
        selectedAmount,
        editing,
        buyoutPrice,
        setBuyoutPrice,
        onChangeForm,
        onSelectAmount: (folio: number) => setSelectedFolio((prev) => (prev === folio ? null : folio)),
        submitCreate,
        startEdit,
        cancelEdit,
        submitEdit,
        submitReturn,
        submitBuyout,
        submitDelete,
    }
}

