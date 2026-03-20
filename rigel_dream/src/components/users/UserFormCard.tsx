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
        <section className="card" style={{padding: 20}}>
            <h2 style={{marginTop: 0, marginBottom: 12, fontSize: 18}}>Agregar usuario</h2>
            <form onSubmit={onSubmit} style={{display: 'grid', gap: 12}}>
                <UserFieldsGrid
                    value={form}
                    disabled={false}
                    isRequired={true}
                    showPassword={true}
                    passwordPlaceholder="Contrasena"
                    onChange={onChangeForm}
                />

                <div>
                    <button className="btn-primary" type="submit" disabled={saving}>
                        {saving ? 'Guardando...' : 'Agregar usuario'}
                    </button>
                </div>
            </form>
            {formMessage && <p className="text-muted" style={{marginBottom: 0}}>{formMessage}</p>}
        </section>
    )
}

