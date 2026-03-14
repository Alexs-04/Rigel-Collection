import {useContext, useMemo} from 'react'
// @ts-ignore
import {AuthContext} from '../context/AuthContext'
import SupplierDetailPanel from '../components/suppliers/SupplierDetailPanel'
import SupplierFormCard from '../components/suppliers/SupplierFormCard'
import SuppliersListPanel from '../components/suppliers/SuppliersListPanel'
import {useSuppliersPageState} from '../hooks/suppliers/useSuppliersPageState'

interface AuthUser {
    role?: string
}

interface AuthContextValue {
    user?: AuthUser | null
}

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
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Proveedores</h1>
                <p className="text-muted" style={{marginTop: 0}}>
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

            <section style={{display: 'grid', gridTemplateColumns: 'minmax(280px, 1fr) minmax(360px, 1.2fr)', gap: 16}}>
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

