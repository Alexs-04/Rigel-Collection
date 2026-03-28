import {useEffect, useState} from 'react'
import {fetchSystemMovementById, fetchSystemMovements} from '../services/logsService'

function normalizeError(error) {
    return error?.response?.data?.message || 'No se pudieron cargar los movimientos del sistema.'
}

function formatDateTime(value) {
    if (!value) return '-'
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return '-'
    return date.toLocaleString('es-MX')
}

function methodColor(method) {
    if (method === 'POST') return '#0369a1'
    if (method === 'PUT') return '#7c3aed'
    if (method === 'DELETE') return '#b91c1c'
    if (method === 'PATCH') return '#b45309'
    return '#334155'
}

export default function Logs() {
    const [filters, setFilters] = useState({
        search: '',
        method: '',
        status: '',
        fromDate: '',
        toDate: '',
    })
    const [pageData, setPageData] = useState({items: [], page: 0, size: 20, totalElements: 0, totalPages: 0})
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')
    const [selectedId, setSelectedId] = useState(null)
    const [detail, setDetail] = useState(null)
    const [loadingDetail, setLoadingDetail] = useState(false)
    const [detailError, setDetailError] = useState('')
    const [refreshKey, setRefreshKey] = useState(0)

    const loadMovements = async () => {
        setLoadingList(true)
        setListError('')
        try {
            const data = await fetchSystemMovements({
                search: filters.search || undefined,
                method: filters.method || undefined,
                status: filters.status ? Number(filters.status) : undefined,
                fromDate: filters.fromDate || undefined,
                toDate: filters.toDate || undefined,
                page: pageData.page,
                size: pageData.size,
            })
            setPageData(data)
        } catch (error) {
            setListError(normalizeError(error))
        } finally {
            setLoadingList(false)
        }
    }

    const loadDetail = async (id) => {
        setLoadingDetail(true)
        setDetailError('')
        try {
            const item = await fetchSystemMovementById(id)
            setDetail(item)
        } catch (error) {
            setDetailError(normalizeError(error))
        } finally {
            setLoadingDetail(false)
        }
    }

    useEffect(() => {
        void loadMovements()
    }, [pageData.page, pageData.size, refreshKey])

    const onSearch = () => {
        setPageData((prev) => ({...prev, page: 0}))
        setRefreshKey((prev) => prev + 1)
    }

    const onClear = () => {
        setFilters({search: '', method: '', status: '', fromDate: '', toDate: ''})
        setSelectedId(null)
        setDetail(null)
        setPageData((prev) => ({...prev, page: 0}))
        setRefreshKey((prev) => prev + 1)
    }

    const onSelect = (id) => {
        if (selectedId === id) {
            setSelectedId(null)
            setDetail(null)
            setDetailError('')
            return
        }
        setSelectedId(id)
        void loadDetail(id)
    }

    return (
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Bitácora</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Movimientos del sistema. Solo usuarios con rol ROOT o ADMIN pueden acceder.
                </p>
            </section>

            <section className="card" style={{padding: 16}}>
                <div style={{display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr 1fr auto auto', gap: 10}}>
                    <input
                        className="input"
                        placeholder="Buscar por usuario, ruta o correlación"
                        value={filters.search}
                        onChange={(event) => setFilters((prev) => ({...prev, search: event.target.value}))}
                    />
                    <select
                        className="input"
                        value={filters.method}
                        onChange={(event) => setFilters((prev) => ({...prev, method: event.target.value}))}
                    >
                        <option value="">Metodo</option>
                        <option value="GET">GET</option>
                        <option value="POST">POST</option>
                        <option value="PUT">PUT</option>
                        <option value="PATCH">PATCH</option>
                        <option value="DELETE">DELETE</option>
                    </select>
                    <input
                        className="input"
                        type="number"
                        min={100}
                        max={599}
                        placeholder="Status"
                        value={filters.status}
                        onChange={(event) => setFilters((prev) => ({...prev, status: event.target.value}))}
                    />
                    <input
                        className="input"
                        type="date"
                        value={filters.fromDate}
                        onChange={(event) => setFilters((prev) => ({...prev, fromDate: event.target.value}))}
                    />
                    <input
                        className="input"
                        type="date"
                        value={filters.toDate}
                        onChange={(event) => setFilters((prev) => ({...prev, toDate: event.target.value}))}
                    />
                    <button type="button" className="btn-primary" onClick={onSearch} disabled={loadingList}>
                        Buscar
                    </button>
                    <button type="button" className="btn-ghost" onClick={onClear} disabled={loadingList}>
                        Limpiar
                    </button>
                </div>
            </section>

            <section
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'minmax(360px, 1fr) minmax(340px, 1fr)',
                    gap: 16,
                }}
            >
                <article className="card" style={{padding: 16}}>
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10}}>
                        <h2 style={{margin: 0, fontSize: 18}}>Movimientos</h2>
                        <span className="text-muted" style={{fontSize: 13}}>
                            {pageData.totalElements} registros
                        </span>
                    </div>

                    {loadingList && <p className="text-muted">Cargando movimientos...</p>}
                    {!loadingList && listError && <p style={{margin: 0, color: '#b91c1c'}}>{listError}</p>}

                    {!loadingList && !listError && (
                        <div style={{display: 'grid', gap: 8}}>
                            {pageData.items.length === 0 && (
                                <p className="text-muted" style={{margin: 0}}>
                                    No se encontraron movimientos para el filtro seleccionado.
                                </p>
                            )}

                            {pageData.items.map((item) => (
                                <button
                                    key={item.id}
                                    type="button"
                                    onClick={() => onSelect(item.id)}
                                    style={{
                                        border: selectedId === item.id ? '1px solid #c7d2fe' : '1px solid rgba(2,6,23,0.06)',
                                        background: selectedId === item.id ? '#eef2ff' : 'white',
                                        borderRadius: 10,
                                        padding: 12,
                                        textAlign: 'left',
                                        cursor: 'pointer',
                                    }}
                                >
                                    <div style={{display: 'flex', justifyContent: 'space-between', gap: 10}}>
                                        <strong style={{fontSize: 14, color: methodColor(item.method)}}>{item.method}</strong>
                                        <span style={{fontSize: 12, color: '#64748b'}}>#{item.correlationId}</span>
                                    </div>
                                    <div style={{fontSize: 13, marginTop: 6, color: '#0f172a'}}>{item.path}</div>
                                    <div style={{display: 'flex', gap: 14, marginTop: 8, fontSize: 12, color: '#64748b'}}>
                                        <span>{item.username} ({item.role})</span>
                                        <span>HTTP {item.status}</span>
                                        <span>{item.durationMs} ms</span>
                                        <span>{formatDateTime(item.occurredAt)}</span>
                                    </div>
                                </button>
                            ))}
                        </div>
                    )}

                    <div style={{display: 'flex', justifyContent: 'space-between', marginTop: 14}}>
                        <button
                            type="button"
                            className="btn-ghost"
                            disabled={loadingList || pageData.page <= 0}
                            onClick={() => setPageData((prev) => ({...prev, page: prev.page - 1}))}
                        >
                            Anterior
                        </button>
                        <span className="text-muted" style={{fontSize: 13}}>
                            Página {pageData.totalPages === 0 ? 0 : pageData.page + 1} de {pageData.totalPages}
                        </span>
                        <button
                            type="button"
                            className="btn-ghost"
                            disabled={loadingList || pageData.page + 1 >= pageData.totalPages}
                            onClick={() => setPageData((prev) => ({...prev, page: prev.page + 1}))}
                        >
                            Siguiente
                        </button>
                    </div>
                </article>

                <article className="card" style={{padding: 16}}>
                    <h2 style={{marginTop: 0, fontSize: 18}}>Detalle</h2>

                    {!selectedId && (
                        <p className="text-muted" style={{margin: 0}}>
                            Selecciona un movimiento para ver su informacion detallada.
                        </p>
                    )}

                    {selectedId && loadingDetail && <p className="text-muted">Cargando detalle...</p>}
                    {selectedId && !loadingDetail && detailError && <p style={{margin: 0, color: '#b91c1c'}}>{detailError}</p>}

                    {selectedId && !loadingDetail && !detailError && detail && (
                        <div style={{display: 'grid', gap: 10, fontSize: 14}}>
                            <div><strong>ID:</strong> {detail.id}</div>
                            <div><strong>Fecha:</strong> {formatDateTime(detail.occurredAt)}</div>
                            <div><strong>Usuario:</strong> {detail.username}</div>
                            <div><strong>Rol:</strong> {detail.role}</div>
                            <div><strong>Metodo:</strong> {detail.method}</div>
                            <div><strong>Ruta:</strong> {detail.path}</div>
                            <div><strong>Status HTTP:</strong> {detail.status}</div>
                            <div><strong>Duracion:</strong> {detail.durationMs} ms</div>
                            <div><strong>Correlation ID:</strong> {detail.correlationId}</div>
                        </div>
                    )}
                </article>
            </section>
        </div>
    )
}

