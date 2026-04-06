import {useContext, useMemo} from 'react'
import {AuthContext} from '../context/AuthContext'
import UserDetailPanel from '../components/users/UserDetailPanel'
import UserFormCard from '../components/users/UserFormCard'
import UsersListPanel from '../components/users/UsersListPanel'
import {useUsersPageState} from '../hooks/users/useUsersPageState'

export default function Users() {
    const auth = useContext(AuthContext)
    const user = auth?.user
    const isRoot = useMemo(() => user?.role === 'ROOT', [user])

    const {
        users,
        loadingList,
        listError,
        search,
        setSearch,
        form,
        formMessage,
        saving,
        selectedId,
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
        addUser,
        saveChanges,
        toggleUserStatus,
        handleUserClick,
        loadDetail,
    } = useUsersPageState({isRoot})

    return (
        <div className="grid gap-4">
            <section className="ui-card p-5">
                <h1 className="mb-1.5 mt-0 text-2xl font-semibold text-slate-900">Usuarios</h1>
                <p className="mt-0 text-sm text-slate-500">
                    Gestiona cuentas registradas. Solo rol ROOT puede acceder y administrar usuarios.
                </p>
            </section>

            {!isRoot && (
                <section className="ui-card p-4">
                    <p className="m-0 text-sm text-slate-500">
                        Acceso denegado. Esta seccion es exclusiva para usuarios ROOT.
                    </p>
                </section>
            )}

            {isRoot && (
                <>
                    <UserFormCard
                        form={form}
                        saving={saving}
                        formMessage={formMessage}
                        onChangeForm={onChangeForm}
                        onSubmit={addUser}
                    />

                    <section className="grid gap-4 lg:grid-cols-[minmax(300px,1fr)_minmax(380px,1.2fr)]">
                        <UsersListPanel
                            search={search}
                            onSearchChange={setSearch}
                            loadingList={loadingList}
                            listError={listError}
                            users={users}
                            selectedId={selectedId}
                            onUserClick={handleUserClick}
                        />

                        <UserDetailPanel
                            selectedId={selectedId}
                            isCollapsingDetail={isCollapsingDetail}
                            detailExpanded={detailExpanded}
                            showDetailContent={showDetailContent}
                            loadingDetail={loadingDetail}
                            detailError={detailError}
                            detail={detail}
                            editing={editing}
                            saving={saving}
                            onChangeDetail={onChangeDetail}
                            onStartEdit={() => setEditing(true)}
                            onCancelEdit={() => {
                                setEditing(false)
                                if (detail) loadDetail(detail.id)
                            }}
                            onSaveChanges={saveChanges}
                            onToggleUserStatus={toggleUserStatus}
                        />
                    </section>
                </>
            )}
        </div>
    )
}

