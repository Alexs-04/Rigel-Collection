import type {UserDetail} from '../../types/users'
import UserFieldsGrid from './UserFieldsGrid'

type UserDetailField = 'name' | 'username' | 'email' | 'phoneNumber' | 'role' | 'password'

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
        <div className="ui-card supplier-detail-card p-4">
            <h2 className="ui-title mt-0 text-lg font-semibold">Detalle del usuario</h2>
            {!selectedId && !isCollapsingDetail && (
                <p className="ui-muted text-sm">Selecciona un usuario para ver su informacion.</p>
            )}

            <div className={`supplier-detail-body ${detailExpanded ? 'is-open' : ''}`}>
                {showDetailContent && (
                    <>
                        {loadingDetail && <p className="ui-muted text-sm">Cargando detalle...</p>}
                        {detailError && <p className="ui-muted text-sm">{detailError}</p>}

                        {detail && !loadingDetail && (
                            <>
                                <UserFieldsGrid
                                    value={detail}
                                    disabled={!editing}
                                    showPassword={editing}
                                    passwordPlaceholder="Nueva contrasena (opcional)"
                                    onChange={onChangeDetail}
                                />

                                <input className="ui-input" value={detail.active ? 'Activo' : 'Inactivo'} disabled />

                                <div className="my-3 flex flex-wrap gap-2">
                                    {!editing && (
                                        <button className="ui-btn-primary" type="button" onClick={onStartEdit}>
                                            Editar
                                        </button>
                                    )}
                                    {editing && (
                                        <button className="ui-btn-primary" type="button" disabled={saving} onClick={onSaveChanges}>
                                            Guardar cambios
                                        </button>
                                    )}
                                    {editing && (
                                        <button className="ui-btn-ghost" type="button" onClick={onCancelEdit}>
                                            Cancelar
                                        </button>
                                    )}
                                    <button className="ui-btn-ghost" type="button" disabled={saving} onClick={onToggleUserStatus}>
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

