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
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Productos</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Consulta el catalogo y administra productos con proveedores previamente registrados.
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

            <section className="card" style={{padding: 16}}>
                <h2 style={{marginTop: 0, fontSize: 18}}> Búsqueda</h2>
                <input
                    className="input"
                    placeholder="Buscar por nombre, código o categoría"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </section>

            <section style={{display: 'grid', gridTemplateColumns: 'minmax(280px, 1fr) minmax(380px, 1.2fr)', gap: 16}}>
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

