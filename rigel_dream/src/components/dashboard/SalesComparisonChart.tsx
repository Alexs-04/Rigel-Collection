import {formatCurrency} from './formatters'
import type {DashboardSalesSummary} from '../../types/dashboard'

interface SalesComparisonChartProps {
    sales?: DashboardSalesSummary
}

export default function SalesComparisonChart({sales}: SalesComparisonChartProps) {
    const values = [
        {label: 'Dia', value: Number(sales?.day || 0), color: '#6366f1'},
        {label: 'Mes', value: Number(sales?.month || 0), color: '#22c55e'},
        {label: 'Anio', value: Number(sales?.year || 0), color: '#0ea5e9'},
    ]

    const maxValue = Math.max(...values.map((item) => item.value), 0)

    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Comparativo de ventas</h2>
            <div style={{display: 'grid', gap: 10}}>
                {values.map((item) => {
                    const width = maxValue > 0 ? (item.value / maxValue) * 100 : 0
                    return (
                        <div key={item.label}>
                            <div style={{display: 'flex', justifyContent: 'space-between', marginBottom: 4}}>
                                <span style={{fontSize: 13}}>{item.label}</span>
                                <strong style={{fontSize: 13}}>{formatCurrency(item.value)}</strong>
                            </div>
                            <div style={{height: 10, borderRadius: 999, background: 'rgba(2, 6, 23, 0.08)', overflow: 'hidden'}}>
                                <div
                                    style={{
                                        width: `${width}%`,
                                        height: '100%',
                                        borderRadius: 999,
                                        background: item.color,
                                        transition: 'width 180ms ease',
                                    }}
                                />
                            </div>
                        </div>
                    )
                })}
            </div>
        </section>
    )
}

