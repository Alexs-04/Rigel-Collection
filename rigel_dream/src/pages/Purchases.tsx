import PurchaseDetailPanel from '../components/purchases/PurchaseDetailPanel'
import PurchaseFormCard from '../components/purchases/PurchaseFormCard'
import PurchasesListPanel from '../components/purchases/PurchasesListPanel'
import {usePurchasesPageState} from '../hooks/purchases/usePurchasesPageState'

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
            <section className="ui-card p-5">
                <h1 className="mb-1.5 mt-0 text-2xl font-semibold text-slate-900">Compras</h1>
                <p className="mt-0 text-sm text-slate-500">
                    Registra entradas al inventario por producto-proveedor y lote.
                </p>
            </section>

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
