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

function percentage(value, total) {
    if (!total) return 0
    return Math.max(0, Math.min(100, (Number(value || 0) / total) * 100))
}

function SalesComparisonChart({sales}) {
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

function CatalogCompositionChart({stats}) {
    const slices = [
        {label: 'Productos', value: Number(stats?.totalProducts || 0), color: '#6366f1'},
        {label: 'Proveedores', value: Number(stats?.totalSuppliers || 0), color: '#22c55e'},
        {label: 'Lotes', value: Number(stats?.totalBatches || 0), color: '#0ea5e9'},
        {label: 'Tickets', value: Number(stats?.totalTickets || 0), color: '#f59e0b'},
    ]

    const total = slices.reduce((acc, item) => acc + item.value, 0)

    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Composicion del sistema</h2>
            <div style={{height: 14, borderRadius: 999, overflow: 'hidden', display: 'flex', background: 'rgba(2, 6, 23, 0.08)'}}>
                {slices.map((item) => (
                    <div
                        key={item.label}
                        title={`${item.label}: ${formatNumber(item.value)}`}
                        style={{
                            width: `${percentage(item.value, total)}%`,
                            background: item.color,
                            minWidth: item.value > 0 ? 4 : 0,
                        }}
                    />
                ))}
            </div>

            <div style={{display: 'grid', gap: 8, marginTop: 12}}>
                {slices.map((item) => (
                    <div key={item.label} style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <div style={{display: 'inline-flex', alignItems: 'center', gap: 8}}>
                            <span style={{display: 'inline-block', width: 10, height: 10, borderRadius: 999, background: item.color}}/>
                            <span style={{fontSize: 13}}>{item.label}</span>
                        </div>
                        <strong style={{fontSize: 13}}>{formatNumber(item.value)}</strong>
                    </div>
                ))}
            </div>
        </section>
    )
}

function TopList({title, data, emptyText}) {
    const maxTotal = Math.max(...data.map((item) => Number(item.total || 0)), 0)

    return (
        <section className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>{title}</h2>
            {!data.length && <p className="text-muted" style={{margin: 0}}>{emptyText}</p>}
            {!!data.length && (
                <div style={{display: 'grid', gap: 10}}>
                    {data.map((item, index) => (
                        <div key={`${item.name}-${index}`}>
                            <div
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
                            <div style={{height: 6, marginTop: 8, borderRadius: 999, background: 'rgba(2, 6, 23, 0.08)', overflow: 'hidden'}}>
                                <div
                                    style={{
                                        height: '100%',
                                        width: `${maxTotal > 0 ? (Number(item.total || 0) / maxTotal) * 100 : 0}%`,
                                        borderRadius: 999,
                                        background: '#6366f1',
                                    }}
                                />
                            </div>
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

            <section style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 16}}>
                <SalesComparisonChart sales={sales}/>
                <CatalogCompositionChart stats={stats}/>
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

