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
        <div className="card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Lista de usuarios</h2>
            <input
                className="input"
                placeholder="Buscar por nombre, usuario o correo"
                value={search}
                onChange={(e) => onSearchChange(e.target.value)}
                style={{marginBottom: 12}}
            />

            {loadingList && <p className="text-muted">Cargando usuarios...</p>}
            {!loadingList && listError && <p className="text-muted">{listError}</p>}
            {!loadingList && !listError && users.length === 0 && (
                <p className="text-muted">No hay usuarios registrados.</p>
            )}

            <div style={{display: 'grid', gap: 8}}>
                {users.map((item) => (
                    <button
                        key={item.id}
                        type="button"
                        onClick={() => onUserClick(item)}
                        style={{
                            textAlign: 'left',
                            border: selectedId === item.id ? '1px solid var(--accent)' : '1px solid var(--border)',
                            borderRadius: 8,
                            background: selectedId === item.id ? 'var(--active-bg)' : 'white',
                            padding: 12,
                            cursor: 'pointer',
                            color: 'inherit',
                        }}
                    >
                        <div style={{display: 'flex', justifyContent: 'space-between', gap: 8}}>
                            <strong>{item.username}</strong>
                            <span className="text-muted" style={{fontSize: 12}}>{item.active ? 'Activo' : 'Inactivo'}</span>
                        </div>
                        <div className="text-muted" style={{fontSize: 13}}>{item.email}</div>
                    </button>
                ))}
            </div>
        </div>
    )
}

