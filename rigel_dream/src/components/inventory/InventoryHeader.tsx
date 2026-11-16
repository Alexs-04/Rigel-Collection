import PageHeader from '../ui/PageHeader'

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
    const figures = [
        {label: 'Productos', value: productCount},
        {label: 'Stock total', value: totalStock},
        {label: 'Valor estimado', value: formatCurrency(totalValue)},
    ]

    return (
        <PageHeader
            title="Inventario"
            description="Consulta los productos registrados, revisa su imagen asociada a Cloudinary y analiza stock, precio e información detallada."
        >
            <div className="mt-6 grid gap-x-10 gap-y-4 sm:grid-cols-3">
                {figures.map((item) => (
                    <div key={item.label} className="border-l border-app-text/25 pl-4">
                        <p className="ui-figure-label">{item.label}</p>
                        <p className="ui-figure-value">{item.value}</p>
                    </div>
                ))}
            </div>
        </PageHeader>
    )
}
