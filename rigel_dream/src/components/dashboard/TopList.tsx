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
                            <div className="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-2">
                                <span className="text-sm text-slate-700">{item.name}</span>
                                <strong className="text-sm text-slate-900">{formatNumber(item.total)}</strong>
                            </div>
                            <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-slate-200">
                                <div
                                    style={{
                                        width: `${maxTotal > 0 ? (Number(item.total || 0) / maxTotal) * 100 : 0}%`,
                                        background: '#6366f1',
                                    }}
                                    className="h-full rounded-full"
                                />
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </Card>
    )
}

