import {formatNumber} from './formatters'
import type {DashboardStats} from '../../types/dashboard'
import Card from '../ui/Card'

interface StatsCardsProps {
    stats?: DashboardStats
}

export default function StatsCards({stats}: StatsCardsProps) {
    return (
        <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <Card as="article" className="p-4">
                <p className="m-0 text-xs text-slate-500">Total productos</p>
                <h2 className="mt-1.5 text-2xl font-bold text-slate-900">{formatNumber(stats?.totalProducts)}</h2>
            </Card>
            <Card as="article" className="p-4">
                <p className="m-0 text-xs text-slate-500">Total proveedores</p>
                <h2 className="mt-1.5 text-2xl font-bold text-slate-900">{formatNumber(stats?.totalSuppliers)}</h2>
            </Card>
            <Card as="article" className="p-4">
                <p className="m-0 text-xs text-slate-500">Total lotes</p>
                <h2 className="mt-1.5 text-2xl font-bold text-slate-900">{formatNumber(stats?.totalBatches)}</h2>
            </Card>
            <Card as="article" className="p-4">
                <p className="m-0 text-xs text-slate-500">Total tickets</p>
                <h2 className="mt-1.5 text-2xl font-bold text-slate-900">{formatNumber(stats?.totalTickets)}</h2>
            </Card>
        </section>
    )
}

