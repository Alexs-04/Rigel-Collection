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
        <div style={{display: 'grid', gap: 16}}>
            <section className="card" style={{padding: 20}}>
                <h1 style={{marginTop: 0, marginBottom: 6}}>Usuarios</h1>
                <p className="text-muted" style={{marginTop: 0}}>
                    Gestiona cuentas registradas. Solo rol ROOT puede acceder y administrar usuarios.
                </p>
            </section>

            {!isRoot && (
                <section className="card" style={{padding: 16}}>
                    <p className="text-muted" style={{margin: 0}}>
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

                    <section
                        style={{
                            display: 'grid',
                            gridTemplateColumns: 'minmax(300px, 1fr) minmax(380px, 1.2fr)',
                            gap: 16
                        }}>
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

