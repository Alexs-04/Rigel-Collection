interface InventoryHeaderProps {
    productCount: number
    totalStock: number
    totalValue: number
}

function formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', {
        style: 'currency',
        currency: 'MXN',
        minimumFractionDigits: 2,
    }).format(Number.isFinite(amount) ? amount : 0)
}

export default function InventoryHeader({productCount, totalStock, totalValue}: InventoryHeaderProps) {
    return (
        <section className="ui-card p-5">
            <h1 className="ui-title mb-1.5 mt-0 text-2xl font-semibold">Inventario</h1>
            <p className="ui-muted mt-0 text-sm">
                Consulta los productos registrados, revisa su imagen asociada a Cloudinary y analiza stock, precio e información detallada.
            </p>

            <div className="mt-4 grid gap-3 sm:grid-cols-3">
                <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                    <div className="text-xs uppercase tracking-wide text-slate-500">Productos</div>
                    <div className="mt-1 text-lg font-semibold text-slate-900 dark:text-slate-100">{productCount}</div>
                </div>
                <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                    <div className="text-xs uppercase tracking-wide text-slate-500">Stock total</div>
                    <div className="mt-1 text-lg font-semibold text-slate-900 dark:text-slate-100">{totalStock}</div>
                </div>
                <div className="rounded-xl border border-slate-200 p-3 dark:border-slate-700">
                    <div className="text-xs uppercase tracking-wide text-slate-500">Valor estimado</div>
                    <div className="mt-1 text-lg font-semibold text-slate-900 dark:text-slate-100">{formatCurrency(totalValue)}</div>
                </div>
            </div>
        </section>
    )
}

