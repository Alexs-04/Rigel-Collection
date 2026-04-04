import {useContext, useMemo} from 'react'
import {AuthContext} from '../context/AuthContext'
import type {AuthContextValue} from '../context/AuthContext'
import SupplierDetailPanel from '../components/suppliers/SupplierDetailPanel'
import SupplierFormCard from '../components/suppliers/SupplierFormCard'
import SuppliersListPanel from '../components/suppliers/SuppliersListPanel'
import {useSuppliersPageState} from '../hooks/suppliers/useSuppliersPageState'


export default function Suppliers() {
    const {user} = useContext(AuthContext) as AuthContextValue
    const isAdmin = useMemo(() => ['ADMIN', 'ROOT'].includes(user?.role || ''), [user])

    const {
        suppliers,
        loadingList,
        listError,
        form,
        formMessage,
        saving,
        selectedName,
        detail,
        products,
        loadingDetail,
        detailError,
        editing,
        isCollapsingDetail,
        detailExpanded,
        showDetailContent,
        setEditing,
        onChangeForm,
        onChangeDetail,
        addSupplier,
        saveChanges,
        deleteSupplier,
        handleSupplierClick,
        loadDetail,
    } = useSuppliersPageState({isAdmin})

    return (
        <div className="grid gap-4">
            <section className="ui-card p-5">
                <h1 className="mb-1.5 mt-0 text-2xl font-semibold text-slate-900">Proveedores</h1>
                <p className="mt-0 text-sm text-slate-500">
                    Consulta proveedores disponibles y revisa sus productos asociados.
                </p>
            </section>

            <SupplierFormCard
                isAdmin={isAdmin}
                userRole={user?.role}
                form={form}
                saving={saving}
                formMessage={formMessage}
                onChangeForm={onChangeForm}
                onSubmit={addSupplier}
            />

            <section className="grid gap-4 lg:grid-cols-[minmax(280px,1fr)_minmax(360px,1.2fr)]">
                <SuppliersListPanel
                    loadingList={loadingList}
                    listError={listError}
                    suppliers={suppliers}
                    selectedName={selectedName}
                    onSupplierClick={handleSupplierClick}
                />

                <SupplierDetailPanel
                    selectedName={selectedName}
                    isCollapsingDetail={isCollapsingDetail}
                    detailExpanded={detailExpanded}
                    showDetailContent={showDetailContent}
                    loadingDetail={loadingDetail}
                    detailError={detailError}
                    detail={detail}
                    products={products}
                    editing={editing}
                    saving={saving}
                    isAdmin={isAdmin}
                    onChangeDetail={onChangeDetail}
                    onStartEdit={() => setEditing(true)}
                    onCancelEdit={() => {
                        setEditing(false)
                        if (selectedName) loadDetail(selectedName)
                    }}
                    onSaveChanges={saveChanges}
                    onDeleteSupplier={deleteSupplier}
                />
            </section>
        </div>
    )
}

