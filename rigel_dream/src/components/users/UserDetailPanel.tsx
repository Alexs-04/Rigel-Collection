import type {UserDetail, UserFormValues} from '../../types/users'
import UserFieldsGrid from './UserFieldsGrid'

type UserDetailField = keyof Pick<UserFormValues, 'name' | 'username' | 'email' | 'phoneNumber' | 'role' | 'password'>

interface UserDetailPanelProps {
    selectedId: number | null
    isCollapsingDetail: boolean
    detailExpanded: boolean
    showDetailContent: boolean
    loadingDetail: boolean
    detailError: string
    detail: UserDetail | null
    editing: boolean
    saving: boolean
    onChangeDetail: (key: UserDetailField, value: string) => void
    onStartEdit: () => void
    onCancelEdit: () => void
    onSaveChanges: () => void
    onToggleUserStatus: () => void
}

export default function UserDetailPanel({
    selectedId,
    isCollapsingDetail,
    detailExpanded,
    showDetailContent,
    loadingDetail,
    detailError,
    detail,
    editing,
    saving,
    onChangeDetail,
    onStartEdit,
    onCancelEdit,
    onSaveChanges,
    onToggleUserStatus,
}: UserDetailPanelProps) {
    return (
        <div className="card supplier-detail-card" style={{padding: 16}}>
            <h2 style={{marginTop: 0, fontSize: 18}}>Detalle del usuario</h2>
            {!selectedId && !isCollapsingDetail && (
                <p className="text-muted">Selecciona un usuario para ver su informacion.</p>
            )}

            <div className={`supplier-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                {showDetailContent && (
                    <>
                        {loadingDetail && <p className="text-muted">Cargando detalle...</p>}
                        {detailError && <p className="text-muted">{detailError}</p>}

                        {detail && !loadingDetail && (
                            <>
                                <UserFieldsGrid
                                    value={detail}
                                    disabled={!editing}
                                    showPassword={editing}
                                    passwordPlaceholder="Nueva contrasena (opcional)"
                                    onChange={onChangeDetail}
                                />

                                <input className="input" value={detail.active ? 'Activo' : 'Inactivo'} disabled />

                                <div style={{display: 'flex', gap: 8, marginTop: 12, marginBottom: 12}}>
                                    {!editing && (
                                        <button className="btn-primary" type="button" onClick={onStartEdit}>
                                            Editar
                                        </button>
                                    )}
                                    {editing && (
                                        <button className="btn-primary" type="button" disabled={saving} onClick={onSaveChanges}>
                                            Guardar cambios
                                        </button>
                                    )}
                                    {editing && (
                                        <button className="btn-ghost" type="button" onClick={onCancelEdit}>
                                            Cancelar
                                        </button>
                                    )}
                                    <button className="btn-ghost" type="button" disabled={saving} onClick={onToggleUserStatus}>
                                        {detail.active ? 'Desactivar' : 'Activar'}
                                    </button>
                                </div>
                            </>
                        )}
                    </>
                )}
            </div>
        </div>
    )
}

