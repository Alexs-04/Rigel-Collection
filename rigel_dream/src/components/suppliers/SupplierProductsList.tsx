import {formatPriceMxn} from '../../utils/productPresentation'
import type {SupplierProduct} from '../../types/suppliers'

interface SupplierProductsListProps {
    products: SupplierProduct[]
}

export default function SupplierProductsList({products}: SupplierProductsListProps) {
    if (products.length === 0) {
        return (
            <p className="mt-0 text-sm text-slate-500">
                Este proveedor no tiene productos asociados.
            </p>
        )
    }

    return (
        <div className="grid gap-2">
            {products.map((product) => (
                <div key={product.name} className="rounded-lg border border-slate-200 p-2.5">
                    <div className="font-semibold text-slate-900">{product.name}</div>
                    <div className="text-xs text-slate-500">
                        {formatPriceMxn(product.price)} - Stock: {product.stock}
                    </div>
                </div>
            ))}
        </div>
    )
}

