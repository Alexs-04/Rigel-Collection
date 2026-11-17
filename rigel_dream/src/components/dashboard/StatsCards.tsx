import {formatNumber} from './formatters'
import type {DashboardStats} from '../../types/dashboard'
import Card from '../ui/Card'

interface StatsCardsProps {
    stats?: DashboardStats
}

export default function StatsCards({stats}: StatsCardsProps) {
    const figures = [
        {label: 'Productos', value: stats?.totalProducts},
        {label: 'Proveedores', value: stats?.totalSuppliers},
        {label: 'Lotes', value: stats?.totalBatches},
        {label: 'Tickets', value: stats?.totalTickets},
    ]

    return (
        <Card className="grid grid-cols-2 !p-0 xl:grid-cols-4">
            {figures.map((item, index) => (
                <article
                    key={item.label}
                    className={`border-app-border p-5 ${index % 2 ? 'border-l' : ''} ${index > 1 ? 'border-t xl:border-t-0' : ''} ${index === 2 ? 'xl:border-l' : ''}`}
                >
                    <p className="ui-figure-label">{item.label}</p>
                    <h2 className="ui-figure-value">{formatNumber(item.value)}</h2>
                </article>
            ))}
        </Card>
    )
}
