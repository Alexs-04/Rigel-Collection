import {formatNumber} from './formatters'
import type {DashboardTopItem} from '../../types/dashboard'
import Card from '../ui/Card'

interface TopListProps {
    title: string
    data: DashboardTopItem[]
    emptyText: string
}

export default function TopList({title, data, emptyText}: TopListProps) {
    const maxTotal = Math.max(...data.map((item) => Number(item.total || 0)), 0)

    return (
        <Card className="p-4">
            <h2 className="mb-3 text-lg font-semibold text-slate-900">{title}</h2>
            {!data.length && <p className="m-0 text-sm text-slate-500">{emptyText}</p>}
            {!!data.length && (
                <div className="grid gap-3">
                    {data.map((item, index) => (
                        <div key={`${item.name}-${index}`}>
                            <div className="flex items-baseline justify-between gap-3">
                                <span className="text-sm text-slate-700"><span className="num mr-2.5 text-xs text-app-muted">{String(index + 1).padStart(2, '0')}</span>{item.name}</span>
                                <strong className="num text-sm text-slate-900">{formatNumber(item.total)}</strong>
                            </div>
                            <div className="mt-1.5 h-1 overflow-hidden bg-app-text/10">
                                <div
                                    style={{
                                        width: `${maxTotal > 0 ? (Number(item.total || 0) / maxTotal) * 100 : 0}%`,
                                        background: 'var(--viz-1)',
                                    }}
                                    className="h-full"
                                />
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </Card>
    )
}

