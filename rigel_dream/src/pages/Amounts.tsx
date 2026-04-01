import {useContext, useMemo} from 'react'
import {AuthContext} from '../context/AuthContext'
import type {AuthContextValue} from '../context/AuthContext'
import AmountDetailPanel from '../components/amounts/AmountDetailPanel'
import AmountsFormCard from '../components/amounts/AmountsFormCard'
import AmountsListPanel from '../components/amounts/AmountsListPanel'
import {useAmountsPageState} from '../hooks/amounts/useAmountsPageState'

export default function Amounts() {
    const {user} = useContext(AuthContext) as AuthContextValue
    const isAdmin = useMemo(() => ['ADMIN', 'ROOT'].includes(user?.role || ''), [user])

    const {
        amounts,
        allAmounts,
        types,
        search,
        setSearch,
        loading,
        error,
        form,
        saving,
        formMessage,
        selectedFolio,
        selectedAmount,
        editing,
        buyoutPrice,
        setBuyoutPrice,
        onChangeForm,
        onSelectAmount,
        submitCreate,
        startEdit,
        cancelEdit,
        submitEdit,
        submitReturn,
        submitBuyout,
        submitDelete,
    } = useAmountsPageState(isAdmin)

    return (
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Importes</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Registra importes de envases no retornables, controla vencimiento y gestiona devoluciones o compras.
                </p>
                <p className="text-muted" style={{marginBottom: 0}}>
                    Total de registros: {allAmounts.length}
                </p>
            </section>

            <AmountsFormCard
                form={form}
                types={types}
                saving={saving}
                message={formMessage}
                onChange={onChangeForm}
                onSubmit={submitCreate}
            />

            <section style={{display: 'grid', gridTemplateColumns: 'minmax(320px, 1fr) minmax(360px, 1.1fr)', gap: 16}}>
                <AmountsListPanel
                    loading={loading}
                    error={error}
                    search={search}
                    onSearchChange={setSearch}
                    amounts={amounts}
                    selectedFolio={selectedFolio}
                    onSelect={onSelectAmount}
                />

                <AmountDetailPanel
                    amount={selectedAmount}
                    isAdmin={isAdmin}
                    editing={editing}
                    saving={saving}
                    form={form}
                    buyoutPrice={buyoutPrice}
                    onChangeForm={onChangeForm}
                    onChangeBuyoutPrice={setBuyoutPrice}
                    onStartEdit={startEdit}
                    onCancelEdit={cancelEdit}
                    onSaveEdit={submitEdit}
                    onMarkReturned={submitReturn}
                    onMarkBoughtOut={submitBuyout}
                    onDelete={submitDelete}
                />
            </section>
        </div>
    )
}
