import {formatPriceMxn} from '../../utils/productPresentation'
import type {SupplierProduct} from '../../types/suppliers'

interface SupplierProductsListProps {
    products: SupplierProduct[]
}

export default function SupplierProductsList({products}: SupplierProductsListProps) {
    if (products.length === 0) {
        return (
            <p className="ui-muted mt-0 text-sm">
                Este proveedor no tiene productos asociados.
            </p>
        )
    }

    return (
        <div className="grid gap-2">
            {products.map((product) => (
                <div key={product.name} className="ui-subcard">
                    <div className="ui-title font-semibold">{product.name}</div>
                    <div className="ui-muted text-xs">
                        {formatPriceMxn(product.price)} - Stock: {product.stock}
                    </div>
                </div>
            ))}
        </div>
    )
}

