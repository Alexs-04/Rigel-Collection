import {formatCurrency} from './formatters'
import type {DashboardSalesSummary} from '../../types/dashboard'
import Card from '../ui/Card'

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
        <Card className="p-4">
            <h2 className="mb-3 text-lg font-semibold text-slate-900">Comparativo de ventas</h2>
            <div className="grid gap-3">
                {values.map((item) => {
                    const width = maxValue > 0 ? (item.value / maxValue) * 100 : 0
                    return (
                        <div key={item.label}>
                            <div className="mb-1 flex justify-between">
                                <span className="text-xs text-slate-600">{item.label}</span>
                                <strong className="text-xs text-slate-900">{formatCurrency(item.value)}</strong>
                            </div>
                            <div className="h-2.5 overflow-hidden rounded-full bg-slate-200">
                                <div
                                    style={{
                                        width: `${width}%`,
                                        background: item.color,
                                    }}
                                    className="h-full rounded-full transition-[width] duration-200"
                                />
                            </div>
                        </div>
                    )
                })}
            </div>
        </Card>
    )
}

