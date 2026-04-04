import React, {useContext, useMemo} from 'react'
import {AuthContext} from '../context/AuthContext'
import ProductDetailPanel from '../components/products/ProductDetailPanel.jsx'
import ProductFormCard from '../components/products/ProductFormCard.jsx'
import ProductsListPanel from '../components/products/ProductsListPanel.jsx'
import {useProductsPageState} from '../hooks/products/useProductsPageState.js'

export default function Products() {
    const {user} = useContext(AuthContext)
    const isAdmin = useMemo(() => ['ADMIN', 'ROOT'].includes(user?.role || ''), [user])

    const {
        suppliers,
        search,
        setSearch,
        loadingList,
        listError,
        filteredProducts,
        form,
        formMessage,
        saving,
        selectedName,
        detail,
        loadingDetail,
        detailError,
        editing,
        isCollapsingDetail,
        detailExpanded,
        showDetailContent,
        setEditing,
        onChangeForm,
        onChangeDetail,
        addProduct,
        saveChanges,
        deleteProduct,
        handleProductClick,
        loadDetail,
    } = useProductsPageState({isAdmin})

    return (
        <div className="grid gap-4">
            <section className="ui-card p-5">
                <h1 className="mb-1.5 mt-0 text-2xl font-semibold text-slate-900">Productos</h1>
                <p className="mt-0 text-sm text-slate-500">
                    Consulta el catalogo y administra productos con proveedores previamente registrados. El inventario y
                    los lotes se registran desde Compras.
                </p>
            </section>

            <ProductFormCard
                isAdmin={isAdmin}
                userRole={user?.role}
                form={form}
                suppliers={suppliers}
                saving={saving}
                formMessage={formMessage}
                onChangeForm={onChangeForm}
                onSubmit={addProduct}
            />

            <section className="ui-card p-4">
                <h2 className="mb-3 mt-0 text-lg font-semibold text-slate-900">Busqueda</h2>
                <input
                    className="ui-input"
                    placeholder="Buscar por nombre, código o categoría"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </section>

            <section className="grid gap-4 lg:grid-cols-[minmax(280px,1fr)_minmax(380px,1.2fr)]">
                <ProductsListPanel
                    loadingList={loadingList}
                    listError={listError}
                    products={filteredProducts}
                    selectedName={selectedName}
                    onProductClick={handleProductClick}
                />

                <ProductDetailPanel
                    selectedName={selectedName}
                    isCollapsingDetail={isCollapsingDetail}
                    detailExpanded={detailExpanded}
                    showDetailContent={showDetailContent}
                    loadingDetail={loadingDetail}
                    detailError={detailError}
                    detail={detail}
                    editing={editing}
                    saving={saving}
                    isAdmin={isAdmin}
                    suppliers={suppliers}
                    onChangeDetail={onChangeDetail}
                    onStartEdit={() => setEditing(true)}
                    onCancelEdit={() => {
                        setEditing(false)
                        if (selectedName) loadDetail(selectedName)
                    }}
                    onSaveChanges={saveChanges}
                    onDeleteProduct={deleteProduct}
                />
            </section>
        </div>
    )
}

