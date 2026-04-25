import type {FormEvent} from 'react'
import type {UserFormValues} from '../../types/users'
import UserFieldsGrid from './UserFieldsGrid'

interface UserFormCardProps {
    form: UserFormValues
    saving: boolean
    formMessage: string
    onChangeForm: (key: keyof UserFormValues, value: string) => void
    onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export default function UserFormCard({form, saving, formMessage, onChangeForm, onSubmit}: UserFormCardProps) {
    return (
        <section className="ui-card p-5">
            <h2 className="ui-title mb-3 mt-0 text-lg font-semibold">Agregar usuario</h2>
            <form onSubmit={onSubmit} className="grid gap-3">
                <UserFieldsGrid
                    value={form}
                    disabled={false}
                    isRequired={true}
                    showPassword={true}
                    passwordPlaceholder="Contrasena"
                    onChange={onChangeForm}
                />

                <div>
                    <button className="ui-btn-primary" type="submit" disabled={saving}>
                        {saving ? 'Guardando...' : 'Agregar usuario'}
                    </button>
                </div>
            </form>
            {formMessage && <p className="ui-muted mb-0 text-sm">{formMessage}</p>}
        </section>
    )
}

