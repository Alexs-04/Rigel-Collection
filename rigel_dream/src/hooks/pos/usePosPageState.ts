import {useEffect, useMemo, useState} from 'react'
import type {MethodPayment, PosCartItem, PosCatalogItem, PosTicketCreatePayload, PosTicket} from '../../types/pos'
import {createPosTicket, fetchPosCatalog, fetchPosTickets} from '../../services/posService'

function normalizeError(error: unknown, fallback: string): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || fallback
}

function toMoney(value: number): number {
    return Number(value.toFixed(2))
}

function toTodayIso(): string {
    return new Date().toISOString().slice(0, 10)
}

export function usePosPageState(currentConsumerEmail: string) {
    const [catalog, setCatalog] = useState<PosCatalogItem[]>([])
    const [tickets, setTickets] = useState<PosTicket[]>([])
    const [cart, setCart] = useState<PosCartItem[]>([])
    const [search, setSearch] = useState('')
    const [description, setDescription] = useState('')
    const [methodPayment, setMethodPayment] = useState<MethodPayment>('CASH')

    const [loadingData, setLoadingData] = useState(true)
    const [listError, setListError] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [checkoutMessage, setCheckoutMessage] = useState('')

    const filteredCatalog = useMemo(() => {
        const normalized = search.trim().toLowerCase()
        if (!normalized) return catalog

        return catalog.filter((item) => {
            return [item.name, item.barcode, item.description, item.category]
                .some((value) => String(value || '').toLowerCase().includes(normalized))
        })
    }, [catalog, search])

    const totals = useMemo(() => {
        return cart.reduce(
            (acc, item) => {
                const gross = item.quantity * item.unitPrice
                const net = Math.max(0, gross - item.discount)
                acc.items += item.quantity
                acc.subtotal = toMoney(acc.subtotal + gross)
                acc.discount = toMoney(acc.discount + item.discount)
                acc.total = toMoney(acc.total + net)
                return acc
            },
            {items: 0, subtotal: 0, discount: 0, total: 0}
        )
    }, [cart])

    const loadPosData = async () => {
        setLoadingData(true)
        setListError('')

        try {
            const [catalogData, ticketsData] = await Promise.all([
                fetchPosCatalog(),
                fetchPosTickets(),
            ])

            setCatalog(catalogData)
            setTickets(
                [...ticketsData]
                    .sort((a, b) => String(b.dateAndTime || '').localeCompare(String(a.dateAndTime || '')))
                    .slice(0, 12)
            )
        } catch (error) {
            setListError(normalizeError(error, 'No se pudo cargar la informacion del punto de venta.'))
        } finally {
            setLoadingData(false)
        }
    }

    const addToCart = (product: PosCatalogItem) => {
        if (product.availableUnits <= 0) return

        setCheckoutMessage('')
        setCart((prev) => {
            const existing = prev.find((item) => item.barcode === product.barcode)
            if (!existing) {
                return [
                    ...prev,
                    {
                        barcode: product.barcode,
                        productName: product.name,
                        quantity: 1,
                        unitPrice: toMoney(product.price),
                        discount: 0,
                    },
                ]
            }

            return prev.map((item) => {
                if (item.barcode !== product.barcode) return item
                return {...item, quantity: item.quantity + 1}
            })
        })
    }

    const updateCartItem = (barcode: string, key: 'quantity' | 'unitPrice' | 'discount', value: number) => {
        setCheckoutMessage('')

        setCart((prev) => {
            return prev.map((item) => {
                if (item.barcode !== barcode) return item

                if (key === 'quantity') {
                    const nextQty = Math.max(1, Math.floor(value || 1))
                    return {...item, quantity: nextQty}
                }

                if (key === 'unitPrice') {
                    return {...item, unitPrice: Math.max(0, toMoney(value || 0))}
                }

                const gross = item.quantity * item.unitPrice
                const nextDiscount = Math.max(0, Math.min(toMoney(value || 0), toMoney(gross)))
                return {...item, discount: nextDiscount}
            })
        })
    }

    const removeCartItem = (barcode: string) => {
        setCheckoutMessage('')
        setCart((prev) => prev.filter((item) => item.barcode !== barcode))
    }

    const clearCart = () => {
        setCheckoutMessage('')
        setCart([])
    }

    const submitSale = async () => {
        setCheckoutMessage('')

        if (!currentConsumerEmail?.trim()) {
            setCheckoutMessage('No se detecto el usuario actual para registrar la venta.')
            return
        }

        if (cart.length === 0) {
            setCheckoutMessage('Agrega al menos un producto al carrito para cobrar.')
            return
        }

        const payload: PosTicketCreatePayload = {
            description: description.trim(),
            dateAndTime: toTodayIso(),
            currentConsumerEmail: currentConsumerEmail.trim(),
            methodPayment,
            products: cart.map((item) => ({
                barcode: item.barcode,
                quantity: item.quantity,
                price: toMoney(item.unitPrice),
                discount: toMoney(item.discount),
            })),
        }

        setSubmitting(true)

        try {
            await createPosTicket(payload)
            setCheckoutMessage('Venta registrada correctamente.')
            setDescription('')
            setMethodPayment('CASH')
            setCart([])
            await loadPosData()
        } catch (error) {
            setCheckoutMessage(normalizeError(error, 'No se pudo registrar la venta.'))
        } finally {
            setSubmitting(false)
        }
    }

    useEffect(() => {
        loadPosData()
    }, [])

    return {
        catalog,
        filteredCatalog,
        tickets,
        cart,
        totals,
        search,
        setSearch,
        description,
        setDescription,
        methodPayment,
        setMethodPayment,
        loadingData,
        listError,
        submitting,
        checkoutMessage,
        addToCart,
        updateCartItem,
        removeCartItem,
        clearCart,
        submitSale,
        reload: loadPosData,
    }
}

