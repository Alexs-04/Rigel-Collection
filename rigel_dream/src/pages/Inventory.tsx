import InventoryDetailPanel from '../components/inventory/InventoryDetailPanel'
import InventoryHeader from '../components/inventory/InventoryHeader'
import InventoryListPanel from '../components/inventory/InventoryListPanel'
import InventorySearchBar from '../components/inventory/InventorySearchBar'
import {useInventoryPageState} from '../hooks/inventory/useInventoryPageState'

export default function Inventory() {
    const {
        productCount,
        filteredProducts,
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

    return (
        <div className="grid gap-4">
            <InventoryHeader productCount={productCount} totalStock={totalStock} totalValue={totalValue} />

            <InventorySearchBar
                search={search}
                onSearchChange={setSearch}
                onReload={reload}
                loading={loading}
            />

            <section className="grid gap-4 lg:grid-cols-[minmax(320px,1fr)_minmax(420px,1.25fr)]">
                <InventoryListPanel
                    loading={loading}
                    error={error}
                    products={filteredProducts}
                    selectedBarcode={selectedBarcode}
                    onSelectProduct={(product) => setSelectedBarcode(product.barcode)}
                />

                <InventoryDetailPanel product={selectedProduct} />
            </section>
        </div>
    )
}
