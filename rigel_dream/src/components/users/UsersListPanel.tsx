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
            <h2 className="mt-0 text-lg font-semibold text-slate-900">Lista de usuarios</h2>
            <input
                className="ui-input mb-3"
                placeholder="Buscar por nombre, usuario o correo"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
            />

            {loadingList && <p className="text-sm text-slate-500">Cargando usuarios...</p>}
            {!loadingList && listError && <p className="text-sm text-slate-500">{listError}</p>}
            {!loadingList && !listError && users.length === 0 && (
                <p className="text-sm text-slate-500">No hay usuarios registrados.</p>
            )}

            <div className="grid gap-2">
                {users.map((item) => (
                    <button
                        key={item.id}
                        type="button"
                        onClick={() => onUserClick(item)}
                        className={`rounded-lg border p-3 text-left text-inherit transition ${
                            selectedId === item.id
                                ? 'border-brand-500 bg-brand-50'
                                : 'border-slate-200 bg-white hover:border-brand-200'
                        }`}
                    >
                        <div className="flex justify-between gap-2">
                            <strong className="text-slate-900">{item.username}</strong>
                            <span className="text-xs text-slate-500">{item.active ? 'Activo' : 'Inactivo'}</span>
                        </div>
                        <div className="text-xs text-slate-500">{item.email}</div>
                    </button>
                ))}
            </div>
        </div>
    )
}

