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
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Compras</h1>
                <p className="text-muted" style={{marginTop: 0}}>
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

            <section style={{display: 'grid', gridTemplateColumns: 'minmax(300px, 1fr) minmax(380px, 1.2fr)', gap: 16}}>
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
