import type {UserSummary} from '../../types/users'

interface UsersListPanelProps {
    search: string
    onSearchChange: (value: string) => void
    loadingList: boolean
    listError: string
    users: UserSummary[]
    selectedId: number | null
    onUserClick: (user: UserSummary) => void
}

export default function UsersListPanel({
    search,
    onSearchChange,
    loadingList,
    listError,
    users,
    selectedId,
    onUserClick,
}: UsersListPanelProps) {
    return (
        <div className="ui-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Lista de usuarios</h2>
            <input
                className="ui-input mb-3"
                placeholder="Buscar por nombre, usuario o correo"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
            />

            {loadingList && <p className="ui-muted text-sm">Cargando usuarios...</p>}
            {!loadingList && listError && <p className="ui-muted text-sm">{listError}</p>}
            {!loadingList && !listError && users.length === 0 && (
                <p className="ui-muted text-sm">No hay usuarios registrados.</p>
            )}

            <div className="grid gap-2">
                {users.map((item) => (
                    <button
                        key={item.id}
                        type="button"
                        onClick={() => onUserClick(item)}
                        className={`ui-list-item ${selectedId === item.id ? 'ui-list-item-active' : ''}`}
                    >
                        <div className="flex justify-between gap-2">
                            <strong className="ui-title">{item.username}</strong>
                            <span className="ui-muted text-xs">{item.active ? 'Activo' : 'Inactivo'}</span>
                        </div>
                        <div className="ui-muted text-xs">{item.email}</div>
                    </button>
                ))}
            </div>
        </div>
    )
}

