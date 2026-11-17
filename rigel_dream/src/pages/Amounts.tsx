import {useContext, useMemo} from 'react'
import {AuthContext} from '../context/AuthContext'
import type {AuthContextValue} from '../context/AuthContext'
import AmountDetailPanel from '../components/amounts/AmountDetailPanel'
import AmountsFormCard from '../components/amounts/AmountsFormCard'
import AmountsListPanel from '../components/amounts/AmountsListPanel'
import {useAmountsPageState} from '../hooks/amounts/useAmountsPageState'
import PageHeader from '../components/ui/PageHeader'

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
        <div className="grid gap-4">
            <PageHeader
                title="Importes"
                description="Registra importes de envases no retornables, controla vencimiento y gestiona devoluciones o compras."
                aside={<>{allAmounts.length} registros</>}
            />

            <AmountsFormCard
                form={form}
                types={types}
                saving={saving}
                message={formMessage}
                onChange={onChangeForm}
                onSubmit={submitCreate}
            />

            <section className="grid grid-cols-1 gap-4 xl:grid-cols-[minmax(320px,1fr)_minmax(360px,1.1fr)]">
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
