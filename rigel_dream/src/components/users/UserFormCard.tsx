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
            <h2 className="mb-3 mt-0 text-lg font-semibold text-slate-900">Agregar usuario</h2>
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
            {formMessage && <p className="mb-0 text-sm text-slate-500">{formMessage}</p>}
        </section>
    )
}

