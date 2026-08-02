import InventoryAlertsPanel from '../components/inventory/InventoryAlertsPanel'
import InventoryDetailPanel from '../components/inventory/InventoryDetailPanel'
import InventoryHeader from '../components/inventory/InventoryHeader'
import InventoryListPanel from '../components/inventory/InventoryListPanel'
import InventorySearchBar from '../components/inventory/InventorySearchBar'
import {useInventoryPageState} from '../hooks/inventory/useInventoryPageState'
import {isLowStock, isOutOfStock} from '../types/products'

export default function Inventory() {
    const {
        productCount,
        filteredProducts,
        allProducts,
        selectedProduct,
        selectedBarcode,
        search,
        loading,
        error,
        totalStock,
        totalValue,
        setSearch,
        setSelectedBarcode,
        reload,
    } = useInventoryPageState()

    // Products that need attention: out of stock OR below their minStock threshold
    const alertProducts = allProducts.filter((p) => isOutOfStock(p) || isLowStock(p))

    return (
        <div className="grid gap-4">
            <InventoryHeader productCount={productCount} totalStock={totalStock} totalValue={totalValue} />

            <InventorySearchBar
                search={search}
                onSearchChange={setSearch}
                onReload={reload}
                loading={loading}
            />

            {/* Alert banner — only renders when there are products needing attention */}
            <InventoryAlertsPanel
                products={alertProducts}
                onSelectProduct={(barcode) => setSelectedBarcode(barcode)}
            />

            <section className="grid gap-4 lg:grid-cols-[minmax(320px,1fr)_minmax(420px,1.25fr)]">
                <InventoryListPanel
                    loading={loading}
                    error={error}
                    products={filteredProducts}
                    selectedBarcode={selectedBarcode}
                    onSelectProduct={(product) => setSelectedBarcode(product.barcode)}
                />

                <InventoryDetailPanel
                    product={selectedProduct}
                    onMinStockUpdated={reload}
                />
            </section>
        </div>
    )
}