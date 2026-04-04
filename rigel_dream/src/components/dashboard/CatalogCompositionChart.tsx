import {formatNumber, percentage} from './formatters'
import type {DashboardStats} from '../../types/dashboard'
import Card from '../ui/Card'

interface CatalogCompositionChartProps {
    stats?: DashboardStats
}

export default function CatalogCompositionChart({stats}: CatalogCompositionChartProps) {
    const slices = [
        {label: 'Productos', value: Number(stats?.totalProducts || 0), color: '#6366f1'},
        {label: 'Proveedores', value: Number(stats?.totalSuppliers || 0), color: '#22c55e'},
        {label: 'Lotes', value: Number(stats?.totalBatches || 0), color: '#0ea5e9'},
        {label: 'Tickets', value: Number(stats?.totalTickets || 0), color: '#f59e0b'},
    ]

    const total = slices.reduce((acc, item) => acc + item.value, 0)

    return (
        <Card className="p-4">
            <h2 className="mb-3 text-lg font-semibold text-slate-900">Composicion del sistema</h2>
            <div className="flex h-3.5 overflow-hidden rounded-full bg-slate-200">
                {slices.map((item) => (
                    <div
                        key={item.label}
                        title={`${item.label}: ${formatNumber(item.value)}`}
                        style={{
                            width: `${percentage(item.value, total)}%`,
                            background: item.color,
                            minWidth: item.value > 0 ? 4 : 0,
                        }}
                    />
                ))}
            </div>

            <div className="mt-3 grid gap-2">
                {slices.map((item) => (
                    <div key={item.label} className="flex items-center justify-between">
                        <div className="inline-flex items-center gap-2">
                            <span className="inline-block h-2.5 w-2.5 rounded-full" style={{background: item.color}}/>
                            <span className="text-xs text-slate-700">{item.label}</span>
                        </div>
                        <strong className="text-xs text-slate-900">{formatNumber(item.value)}</strong>
                    </div>
                ))}
            </div>
        </Card>
    )
}

