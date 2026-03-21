import React, {useContext} from 'react'
import {AuthContext} from '../context/AuthContext'
import PosCatalogPanel from '../components/pos/PosCatalogPanel'
import PosCartPanel from '../components/pos/PosCartPanel'
import PosCheckoutCard from '../components/pos/PosCheckoutCard'
import PosTicketsPanel from '../components/pos/PosTicketsPanel'
import {usePosPageState} from '../hooks/pos/usePosPageState'

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
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Punto de venta</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Registra ventas en tiempo real y sincroniza tickets con el backend.
                </p>
            </section>

            {!canAccessPos && (
                <section className="card" style={{padding: 16}}>
                    <p className="text-muted" style={{margin: 0}}>
                        Acceso denegado. El rol SUPPLIER no tiene acceso al punto de venta.
                    </p>
                </section>
            )}

            {canAccessPos && (
                <section style={{display: 'grid', gridTemplateColumns: 'minmax(320px, 1.1fr) minmax(360px, 1fr)', gap: 16}}>
                    <div style={{display: 'grid', gap: 16}}>
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

                    <div style={{display: 'grid', gap: 16}}>
                        <PosCartPanel
                            items={cart}
                            onUpdateItem={updateCartItem}
                            onRemoveItem={removeCartItem}
                            onClear={clearCart}
                        />

                        <PosCheckoutCard
                            description={description}
                            onDescriptionChange={setDescription}
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
