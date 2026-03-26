import type {DashboardSalesSummary} from '../../types/dashboard'
import {formatCurrency} from './formatters'

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
        <section style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12}}>
            {cards.map((item) => (
                <article key={item.label} className="card" style={{padding: 16}}>
                    <p className="text-muted" style={{margin: 0, fontSize: 13}}>{item.label}</p>
                    <h3 style={{margin: '6px 0 0', fontSize: 22}}>{item.value}</h3>
                </article>
            ))}
        </section>
    )
}

