import {useEffect, useState} from 'react'
import CatalogCompositionChart from '../components/dashboard/CatalogCompositionChart'
import SalesComparisonChart from '../components/dashboard/SalesComparisonChart'
import SalesCards from '../components/dashboard/SalesCards'
import StatsCards from '../components/dashboard/StatsCards'
import TopList from '../components/dashboard/TopList'
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
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Dashboard</h1>
                <p className="text-muted" style={{marginTop: 0, marginBottom: 0}}>
                    Visualiza el estado general del negocio segun la informacion consolidada del backend.
                </p>
            </section>

            <section className="card" style={{padding: 16}}>
                <div style={{display: 'flex', gap: 12, alignItems: 'end', flexWrap: 'wrap'}}>
                    <div style={{minWidth: 220}}>
                        <label htmlFor="dashboard-date">Fecha de referencia</label>
                        <input
                            id="dashboard-date"
                            type="date"
                            className="input"
                            value={selectedDate}
                            onChange={(event) => setSelectedDate(event.target.value)}
                        />
                    </div>
                    <button type="button" className="btn-primary" onClick={() => void loadDashboard()} disabled={loading}>
                        {loading ? 'Cargando...' : 'Actualizar'}
                    </button>
                    <span className="text-muted" style={{fontSize: 13}}>
                        Fecha backend: {snapshot?.referenceDate || '-'}
                    </span>
                </div>
            </section>

            {error && (
                <section className="card" style={{padding: 16, borderColor: 'rgba(239, 68, 68, 0.3)'}}>
                    <p style={{margin: 0, color: '#b91c1c'}}>{error}</p>
                </section>
            )}

            <StatsCards stats={snapshot?.stats}/>
            <SalesCards sales={snapshot?.sales}/>

            <section style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16}}>
                <SalesComparisonChart sales={snapshot?.sales}/>
                <CatalogCompositionChart stats={snapshot?.stats}/>
            </section>

            <section style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16}}>
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

