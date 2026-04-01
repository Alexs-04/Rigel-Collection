import {useCallback, useEffect, useState} from 'react'
import {fetchSystemMovementById, fetchSystemMovements} from '../services/logsService'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import {Input} from '../components/ui/Input'
import {Select} from '../components/ui/Select'

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
        importantOnly: true,
    })
    const [pageData, setPageData] = useState({items: [], page: 0, size: 20, totalElements: 0, totalPages: 0})
    const [loadingList, setLoadingList] = useState(true)
    const [listError, setListError] = useState('')
    const [selectedId, setSelectedId] = useState(null)
    const [detail, setDetail] = useState(null)
    const [loadingDetail, setLoadingDetail] = useState(false)
    const [detailError, setDetailError] = useState('')
    const [refreshKey, setRefreshKey] = useState(0)

    const loadMovements = useCallback(async () => {
        setLoadingList(true)
        setListError('')
        try {
            const data = await fetchSystemMovements({
                search: filters.search || undefined,
                method: filters.method || undefined,
                status: filters.status ? Number(filters.status) : undefined,
                fromDate: filters.fromDate || undefined,
                toDate: filters.toDate || undefined,
                importantOnly: filters.importantOnly,
                page: pageData.page,
                size: pageData.size,
            })
            setPageData(data)
        } catch (error) {
            setListError(normalizeError(error))
        } finally {
            setLoadingList(false)
        }
    }, [filters, pageData.page, pageData.size])

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
    }, [loadMovements, refreshKey])

    const onSearch = () => {
        setPageData((prev) => ({...prev, page: 0}))
        setRefreshKey((prev) => prev + 1)
    }

    const onClear = () => {
        setFilters({search: '', method: '', status: '', fromDate: '', toDate: '', importantOnly: true})
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
        <div className="grid gap-4">
            <Card className="p-5">
                <h1 className="mb-1 text-2xl font-bold text-slate-900">Bitácora</h1>
                <p className="mt-0 text-sm text-slate-500">
                    Movimientos del sistema. Solo usuarios con rol ROOT o ADMIN pueden acceder.
                </p>
            </Card>

            <Card className="p-4">
                <div className="grid grid-cols-1 gap-2 xl:grid-cols-[2fr_1fr_1fr_1fr_1fr_auto_auto]">
                    <Input
                        placeholder="Buscar por usuario, ruta o correlación"
                        value={filters.search}
                        onChange={(event) => setFilters((prev) => ({...prev, search: event.target.value}))}
                    />
                    <Select
                        value={filters.method}
                        onChange={(event) => setFilters((prev) => ({...prev, method: event.target.value}))}
                    >
                        <option value="">Metodo</option>
                        <option value="GET">GET</option>
                        <option value="POST">POST</option>
                        <option value="PUT">PUT</option>
                        <option value="PATCH">PATCH</option>
                        <option value="DELETE">DELETE</option>
                    </Select>
                    <Input
                        type="number"
                        min={100}
                        max={599}
                        placeholder="Status"
                        value={filters.status}
                        onChange={(event) => setFilters((prev) => ({...prev, status: event.target.value}))}
                    />
                    <Input
                        type="date"
                        value={filters.fromDate}
                        onChange={(event) => setFilters((prev) => ({...prev, fromDate: event.target.value}))}
                    />
                    <Input
                        type="date"
                        value={filters.toDate}
                        onChange={(event) => setFilters((prev) => ({...prev, toDate: event.target.value}))}
                    />
                    <Button type="button" onClick={onSearch} disabled={loadingList}>
                        Buscar
                    </Button>
                    <Button type="button" variant="ghost" onClick={onClear} disabled={loadingList}>
                        Limpiar
                    </Button>
                </div>

                <label className="mt-2.5 inline-flex items-center gap-2 text-xs text-slate-600">
                    <input
                        type="checkbox"
                        className="h-4 w-4 accent-brand-600"
                        checked={filters.importantOnly}
                        onChange={(event) => setFilters((prev) => ({...prev, importantOnly: event.target.checked}))}
                    />
                    Solo eventos importantes (crear, actualizar, eliminar o fallos)
                </label>
            </Card>

            <section className="grid grid-cols-1 gap-4 xl:grid-cols-[minmax(360px,1fr)_minmax(340px,1fr)]">
                <Card as="article" className="p-4">
                    <div className="mb-2.5 flex items-center justify-between">
                        <h2 className="m-0 text-lg font-semibold text-slate-900">Movimientos</h2>
                        <span className="text-xs text-slate-500">
                            {pageData.totalElements} registros
                        </span>
                    </div>

                    {loadingList && <p className="text-sm text-slate-500">Cargando movimientos...</p>}
                    {!loadingList && listError && <p className="m-0 text-sm text-red-700">{listError}</p>}

                    {!loadingList && !listError && (
                        <div className="grid gap-2">
                            {pageData.items.length === 0 && (
                                <p className="m-0 text-sm text-slate-500">
                                    No se encontraron movimientos para el filtro seleccionado.
                                </p>
                            )}

                            {pageData.items.map((item) => (
                                <button
                                    key={item.id}
                                    type="button"
                                    onClick={() => onSelect(item.id)}
                                    className={`rounded-lg border px-3 py-2 text-left transition ${
                                        selectedId === item.id
                                            ? 'border-brand-300 bg-brand-50'
                                            : 'border-slate-200 bg-white hover:border-brand-200 hover:bg-brand-50/40'
                                    }`}
                                >
                                    <div className="flex justify-between gap-2.5">
                                        <strong className="text-sm" style={{color: methodColor(item.method)}}>{item.method}</strong>
                                        <span className="text-xs text-slate-500">#{item.correlationId}</span>
                                    </div>
                                    <div className="mt-1.5 text-sm text-slate-900">{item.path}</div>
                                    <div className="mt-2 flex flex-wrap gap-3.5 text-xs text-slate-500">
                                        <span>{item.username} ({item.role})</span>
                                        <span>HTTP {item.status}</span>
                                        <span>{item.durationMs} ms</span>
                                        <span>{formatDateTime(item.occurredAt)}</span>
                                    </div>
                                </button>
                            ))}
                        </div>
                    )}

                    <div className="mt-3.5 flex items-center justify-between">
                        <Button
                            type="button"
                            variant="ghost"
                            disabled={loadingList || pageData.page <= 0}
                            onClick={() => setPageData((prev) => ({...prev, page: prev.page - 1}))}
                        >
                            Anterior
                        </Button>
                        <span className="text-xs text-slate-500">
                            Página {pageData.totalPages === 0 ? 0 : pageData.page + 1} de {pageData.totalPages}
                        </span>
                        <Button
                            type="button"
                            variant="ghost"
                            disabled={loadingList || pageData.page + 1 >= pageData.totalPages}
                            onClick={() => setPageData((prev) => ({...prev, page: prev.page + 1}))}
                        >
                            Siguiente
                        </Button>
                    </div>
                </Card>

                <Card as="article" className="p-4">
                    <h2 className="mb-3 text-lg font-semibold text-slate-900">Detalle</h2>

                    {!selectedId && (
                        <p className="m-0 text-sm text-slate-500">
                            Selecciona un movimiento para ver su informacion detallada.
                        </p>
                    )}

                    {selectedId && loadingDetail && <p className="text-sm text-slate-500">Cargando detalle...</p>}
                    {selectedId && !loadingDetail && detailError && <p className="m-0 text-sm text-red-700">{detailError}</p>}

                    {selectedId && !loadingDetail && !detailError && detail && (
                        <div className="grid gap-2.5 text-sm text-slate-700">
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
                </Card>
            </section>
        </div>
    )
}

