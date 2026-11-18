import React, {useContext} from 'react'
import {AuthContext} from '../context/AuthContext'
import PosCatalogPanel from '../components/pos/PosCatalogPanel'
import PosCartPanel from '../components/pos/PosCartPanel'
import PosCheckoutCard from '../components/pos/PosCheckoutCard'
import PosTicketsPanel from '../components/pos/PosTicketsPanel'
import {usePosPageState} from '../hooks/pos/usePosPageState'
import PageHeader from '../components/ui/PageHeader'

export default function POS() {
    const auth = useContext(AuthContext)
    const user = auth?.user
    const role = user?.role || ''
    const canAccessPos = role !== 'SUPPLIER'

    const {
        filteredCatalog,
        tickets,
        cart,
        totals,
        search,
        setSearch,
        description,
        setDescription,
        methodPayment,
        setMethodPayment,
        loadingData,
        listError,
        submitting,
        checkoutMessage,
        addToCart,
        updateCartItem,
        removeCartItem,
        clearCart,
        submitSale,
        reload,
    } = usePosPageState(user?.username || '')

    return (
        <div className="grid gap-4">
            <PageHeader
                title="Punto de venta"
                description="Registra ventas en tiempo real y sincroniza tickets con el backend."
            />

            {!canAccessPos && (
                <section className="ui-card p-4">
                    <p className="m-0 text-sm text-slate-500">
                        Acceso denegado. El rol SUPPLIER no tiene acceso al punto de venta.
                    </p>
                </section>
            )}

            {canAccessPos && (
                <section className="grid gap-4 xl:grid-cols-[minmax(320px,1.1fr)_minmax(360px,1fr)]">
                    <div className="grid gap-4">
                        <PosCatalogPanel
                            search={search}
                            onSearchChange={setSearch}
                            loading={loadingData}
                            error={listError}
                            products={filteredCatalog}
                            onAddProduct={addToCart}
                        />

                        <PosTicketsPanel tickets={tickets} onReload={reload} loading={loadingData} />
                    </div>

                    <div className="grid gap-4">
                        <PosCartPanel
                            items={cart}
                            onUpdateItem={updateCartItem}
                            onRemoveItem={removeCartItem}
                            onClear={clearCart}
                        />

                        <PosCheckoutCard
                            description={description}
                            onDescriptionChange={setDescription}
                            methodPayment={methodPayment}
                            onMethodPaymentChange={setMethodPayment}
                            totals={totals}
                            message={checkoutMessage}
                            submitting={submitting}
                            onSubmit={submitSale}
                        />
                    </div>
                </section>
            )}
        </div>
    )
}
