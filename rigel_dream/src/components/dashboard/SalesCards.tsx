import type {DashboardSalesSummary} from '../../types/dashboard'
import {formatCurrency} from './formatters'
import Card from '../ui/Card'

interface SalesCardsProps {
    sales?: DashboardSalesSummary
}

export default function SalesCards({sales}: SalesCardsProps) {
    const cards = [
        {label: 'Ventas del dia', value: formatCurrency(sales?.day)},
        {label: 'Ventas del mes', value: formatCurrency(sales?.month)},
        {label: 'Ventas del anio', value: formatCurrency(sales?.year)},
    ]

    return (
        <section className="grid gap-3 md:grid-cols-3">
            {cards.map((item) => (
                <Card key={item.label} as="article" className="p-4">
                    <p className="m-0 text-xs text-slate-500">{item.label}</p>
                    <h3 className="mt-1.5 text-2xl font-semibold text-slate-900">{item.value}</h3>
                </Card>
            ))}
        </section>
    )
}

