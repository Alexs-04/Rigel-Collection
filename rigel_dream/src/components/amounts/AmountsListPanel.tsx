import type {AmountItem} from '../../types/amounts'
import {formatCurrency} from '../dashboard/formatters'
import Card from '../ui/Card'
import {Input} from '../ui/Input'

interface Props {
    loading: boolean
    error: string
    search: string
    onSearchChange: (value: string) => void
    amounts: AmountItem[]
    selectedFolio: number | null
    onSelect: (folio: number) => void
}

const STATUS_LABEL: Record<string, string> = {
    ACTIVE: 'Activo',
    EXPIRED: 'Vencido',
    RETURNED: 'Devuelto',
    BOUGHT_OUT: 'Comprado',
}

export default function AmountsListPanel({
    loading,
    error,
    search,
    onSearchChange,
    amounts,
    selectedFolio,
    onSelect,
}: Props) {
    return (
        <Card className="p-4">
            <h2 className="mb-3 text-lg font-semibold text-slate-900">Importes registrados</h2>
            <Input
                placeholder="Buscar por folio, cliente, tipo o estado"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
                className="mb-3"
            />

            {loading ? <p className="text-sm text-slate-500">Cargando importes...</p> : null}
            {error ? <p className="text-sm text-red-700">{error}</p> : null}

            {!loading && !error && amounts.length === 0 ? <p className="text-sm text-slate-500">Sin importes registrados.</p> : null}

            <div className="grid gap-2">
                {amounts.map((item) => {
                    const selected = selectedFolio === item.folio
                    return (
                        <button
                            key={item.folio}
                            type="button"
                            className={`w-full rounded-lg border px-3 py-2 text-left transition ${
                                selected
                                    ? 'border-brand-300 bg-brand-50'
                                    : 'border-slate-200 bg-white hover:border-brand-200 hover:bg-brand-50/40'
                            }`}
                            onClick={() => onSelect(item.folio)}
                        >
                            <strong className="text-sm text-slate-900">#{item.folio} - {item.customerName}</strong>
                            <div className="text-xs text-slate-500">
                                {item.typeLabel} · {STATUS_LABEL[item.status] || item.status} · {formatCurrency(item.total)}
                            </div>
                        </button>
                    )
                })}
            </div>
        </Card>
    )
}

