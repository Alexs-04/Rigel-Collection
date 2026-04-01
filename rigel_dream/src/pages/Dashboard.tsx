import {useEffect, useState} from 'react'
import CatalogCompositionChart from '../components/dashboard/CatalogCompositionChart'
import SalesComparisonChart from '../components/dashboard/SalesComparisonChart'
import SalesCards from '../components/dashboard/SalesCards'
import StatsCards from '../components/dashboard/StatsCards'
import TopList from '../components/dashboard/TopList'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import {Input} from '../components/ui/Input'
import {fetchDashboardSnapshot} from '../services/dashboardService'
import type {DashboardSnapshot} from '../types/dashboard'

function toInputDate(value: Date | string | null | undefined): string {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''
    return date.toISOString().slice(0, 10)
}

function normalizeError(error: unknown): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || 'No se pudo cargar la informacion del dashboard.'
}

export default function Dashboard() {
    const [selectedDate, setSelectedDate] = useState<string>(toInputDate(new Date()))
    const [snapshot, setSnapshot] = useState<DashboardSnapshot | null>(null)
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setError] = useState<string>('')

    const loadDashboard = async (): Promise<void> => {
        setLoading(true)
        setError('')
        try {
            const data = await fetchDashboardSnapshot({date: selectedDate || undefined, limit: 5})
            setSnapshot(data)
        } catch (err) {
            setError(normalizeError(err))
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        void loadDashboard()
    }, [selectedDate])

    return (
        <div className="grid gap-4">
            <Card className="p-5">
                <h1 className="mb-1 text-2xl font-bold text-slate-900">Dashboard</h1>
                <p className="m-0 text-sm text-slate-500">
                    Visualiza el estado general del negocio segun la informacion consolidada del backend.
                </p>
            </Card>

            <Card className="p-4">
                <div className="flex flex-wrap items-end gap-3">
                    <div className="min-w-[220px]">
                        <label htmlFor="dashboard-date">Fecha de referencia</label>
                        <Input
                            id="dashboard-date"
                            type="date"
                            value={selectedDate}
                            onChange={(event) => setSelectedDate(event.target.value)}
                        />
                    </div>
                    <Button type="button" onClick={() => void loadDashboard()} disabled={loading}>
                        {loading ? 'Cargando...' : 'Actualizar'}
                    </Button>
                    <span className="text-sm text-slate-500">
                        Fecha backend: {snapshot?.referenceDate || '-'}
                    </span>
                </div>
            </Card>

            {error && (
                <Card className="border-red-200 p-4">
                    <p className="m-0 text-sm text-red-700">{error}</p>
                </Card>
            )}

            <StatsCards stats={snapshot?.stats}/>
            <SalesCards sales={snapshot?.sales}/>

            <section className="grid gap-4 lg:grid-cols-2">
                <SalesComparisonChart sales={snapshot?.sales}/>
                <CatalogCompositionChart stats={snapshot?.stats}/>
            </section>

            <section className="grid gap-4 lg:grid-cols-2">
                <TopList
                    title="Productos mas vendidos"
                    data={snapshot?.topProducts || []}
                    emptyText="No hay ventas registradas para el periodo seleccionado."
                />
                <TopList
                    title="Proveedores con mas ventas"
                    data={snapshot?.topSuppliers || []}
                    emptyText="No hay proveedores con ventas para el periodo seleccionado."
                />
            </section>
        </div>
    )
}

