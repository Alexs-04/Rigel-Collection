import PurchaseDetailPanel from '../components/purchases/PurchaseDetailPanel'
import PurchaseFormCard from '../components/purchases/PurchaseFormCard'
import PurchasesListPanel from '../components/purchases/PurchasesListPanel'
import {usePurchasesPageState} from '../hooks/purchases/usePurchasesPageState'
import PageHeader from '../components/ui/PageHeader'

export default function Purchases() {
    const {
        products,
        search,
        setSearch,
        loadingList,
        listError,
        filteredPurchases,
        form,
        formMessage,
        saving,
        selectedId,
        selectedPurchase,
        supplierOptions,
        batchOptions,
        onChangeForm,
        createPurchase,
        handleSelectPurchase,
    } = usePurchasesPageState()

    return (
        <div className="grid gap-4">
            <PageHeader
                title="Compras"
                description="Registra entradas al inventario por producto-proveedor y lote."
            />

            <PurchaseFormCard
                form={form}
                products={products}
                suppliers={supplierOptions}
                batches={batchOptions}
                saving={saving}
                formMessage={formMessage}
                onChange={onChangeForm}
                onSubmit={createPurchase}
            />

            <section className="grid gap-4 lg:grid-cols-[minmax(300px,1fr)_minmax(380px,1.2fr)]">
                <PurchasesListPanel
                    search={search}
                    onSearchChange={setSearch}
                    loadingList={loadingList}
                    listError={listError}
                    purchases={filteredPurchases}
                    selectedId={selectedId}
                    onPurchaseClick={handleSelectPurchase}
                />

                <PurchaseDetailPanel purchase={selectedPurchase} />
            </section>
        </div>
    )
}
