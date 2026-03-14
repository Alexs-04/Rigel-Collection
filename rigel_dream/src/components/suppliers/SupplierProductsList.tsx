import {formatPriceMxn} from '../../utils/productPresentation'
import type {SupplierProduct} from '../../types/suppliers'

interface SupplierProductsListProps {
    products: SupplierProduct[]
}

export default function SupplierProductsList({products}: SupplierProductsListProps) {
    if (products.length === 0) {
        return (
            <p className="text-muted" style={{marginTop: 0}}>
                Este proveedor no tiene productos asociados.
            </p>
        )
    }

    return (
        <div style={{display: 'grid', gap: 8}}>
            {products.map((product) => (
                <div key={product.name} style={{border: '1px solid var(--border)', borderRadius: 8, padding: 10}}>
                    <div style={{fontWeight: 600}}>{product.name}</div>
                    <div className="text-muted" style={{fontSize: 13}}>
                        {formatPriceMxn(product.price)} - Stock: {product.stock}
                    </div>
                </div>
            ))}
        </div>
    )
}

