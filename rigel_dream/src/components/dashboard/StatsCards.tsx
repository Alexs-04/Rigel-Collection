import {formatNumber} from './formatters'
import type {DashboardStats} from '../../types/dashboard'

interface StatsCardsProps {
    stats?: DashboardStats
}

export default function StatsCards({stats}: StatsCardsProps) {
    return (
        <section style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12}}>
            <article className="card" style={{padding: 16}}>
                <p className="text-muted" style={{margin: 0, fontSize: 13}}>Total productos</p>
                <h2 style={{margin: '6px 0 0', fontSize: 24}}>{formatNumber(stats?.totalProducts)}</h2>
            </article>
            <article className="card" style={{padding: 16}}>
                <p className="text-muted" style={{margin: 0, fontSize: 13}}>Total proveedores</p>
                <h2 style={{margin: '6px 0 0', fontSize: 24}}>{formatNumber(stats?.totalSuppliers)}</h2>
            </article>
            <article className="card" style={{padding: 16}}>
                <p className="text-muted" style={{margin: 0, fontSize: 13}}>Total lotes</p>
                <h2 style={{margin: '6px 0 0', fontSize: 24}}>{formatNumber(stats?.totalBatches)}</h2>
            </article>
            <article className="card" style={{padding: 16}}>
                <p className="text-muted" style={{margin: 0, fontSize: 13}}>Total tickets</p>
                <h2 style={{margin: '6px 0 0', fontSize: 24}}>{formatNumber(stats?.totalTickets)}</h2>
            </article>
        </section>
    )
}

