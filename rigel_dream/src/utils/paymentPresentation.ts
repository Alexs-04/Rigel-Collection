import type {MethodPayment} from '../types/pos'

const PAYMENT_LABELS_ES: Record<MethodPayment, string> = {
    CASH: 'Efectivo',
    CARD: 'Tarjeta',
    TRANSFER: 'Transferencia',
    DEPOSIT: 'Deposito',
}

export const PAYMENT_OPTIONS_ES: Array<{value: MethodPayment; label: string}> = (
    Object.entries(PAYMENT_LABELS_ES) as Array<[MethodPayment, string]>
).map(([value, label]) => ({value, label}))

export function toPaymentLabel(payment: MethodPayment | string | null | undefined): string {
    if (!payment) return PAYMENT_LABELS_ES.CASH
    return PAYMENT_LABELS_ES[payment as MethodPayment] || String(payment)
}

