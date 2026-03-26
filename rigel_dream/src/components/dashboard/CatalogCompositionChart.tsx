import {formatNumber, percentage} from './formatters'
import type {DashboardStats} from '../../types/dashboard'

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
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Composicion del sistema</h2>
            <div style={{height: 14, borderRadius: 999, overflow: 'hidden', display: 'flex', background: 'rgba(2, 6, 23, 0.08)'}}>
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

            <div style={{display: 'grid', gap: 8, marginTop: 12}}>
                {slices.map((item) => (
                    <div key={item.label} style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <div style={{display: 'inline-flex', alignItems: 'center', gap: 8}}>
                            <span style={{display: 'inline-block', width: 10, height: 10, borderRadius: 999, background: item.color}}/>
                            <span style={{fontSize: 13}}>{item.label}</span>
                        </div>
                        <strong style={{fontSize: 13}}>{formatNumber(item.value)}</strong>
                    </div>
                ))}
            </div>
        </section>
    )
}

