import {useEffect, useMemo, useState} from 'react'
import {fetchDashboardSnapshot} from '../services/dashboardService'

function formatCurrency(value) {
    return new Intl.NumberFormat('es-MX', {
        style: 'currency',
        currency: 'MXN',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(Number(value || 0))
}

function formatNumber(value) {
    return new Intl.NumberFormat('es-MX').format(Number(value || 0))
}

function toInputDate(value) {
    if (!value) return ''
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return ''
    return date.toISOString().slice(0, 10)
}

function normalizeError(error) {
    return error?.response?.data?.message || 'No se pudo cargar la informacion del dashboard.'
}

function TopList({title, data, emptyText}) {
    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>{title}</h2>
            {!data.length && <p className="text-muted" style={{margin: 0}}>{emptyText}</p>}
            {!!data.length && (
                <div style={{display: 'grid', gap: 10}}>
                    {data.map((item, index) => (
                        <div
                            key={`${item.name}-${index}`}
                            style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                padding: '10px 12px',
                                border: '1px solid rgba(2, 6, 23, 0.08)',
                                borderRadius: 8,
                            }}
                        >
                            <span>{item.name}</span>
                            <strong>{formatNumber(item.total)}</strong>
                        </div>
                    ))}
                </div>
            )}
        </section>
    )
}

export default function Dashboard() {
    const [selectedDate, setSelectedDate] = useState(toInputDate(new Date()))
    const [snapshot, setSnapshot] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const stats = snapshot?.stats
    const sales = snapshot?.sales

    const salesCards = useMemo(() => ([
        {label: 'Ventas del dia', value: formatCurrency(sales?.day)},
        {label: 'Ventas del mes', value: formatCurrency(sales?.month)},
        {label: 'Ventas del anio', value: formatCurrency(sales?.year)},
    ]), [sales])

    const loadDashboard = async () => {
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
        loadDashboard()
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
                    <button type="button" className="btn-primary" onClick={loadDashboard} disabled={loading}>
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

            <section style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12}}>
                {salesCards.map((item) => (
                    <article key={item.label} className="card" style={{padding: 16}}>
                        <p className="text-muted" style={{margin: 0, fontSize: 13}}>{item.label}</p>
                        <h3 style={{margin: '6px 0 0', fontSize: 22}}>{item.value}</h3>
                    </article>
                ))}
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

