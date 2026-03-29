import {formatNumber} from './formatters'
import type {DashboardTopItem} from '../../types/dashboard'

interface TopListProps {
    title: string
    data: DashboardTopItem[]
    emptyText: string
}

export default function TopList({title, data, emptyText}: TopListProps) {
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

