import type {FormEvent} from 'react'
import type {UserFormValues} from '../../types/users'
import UserFieldsGrid from './UserFieldsGrid'

interface UserFormCardProps {
    form: UserFormValues
    saving: boolean
    formMessage: string
    onChangeForm: (key: keyof UserFormValues | 'password', value: string) => void
    onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

export default function UserFormCard({form, saving, formMessage, onChangeForm, onSubmit}: UserFormCardProps) {
    return (
        <section className="ui-card p-5">
            <h2 className="ui-title mb-1 mt-0 text-lg font-semibold">Invitar usuario</h2>
            <p className="ui-muted mt-0 mb-3 text-sm">
                Se enviara un correo al usuario para que active su cuenta y establezca su contrasena.
            </p>
            <form onSubmit={onSubmit} className="grid gap-3">
                <UserFieldsGrid
                    value={form}
                    disabled={false}
                    isRequired={true}
                    showPassword={false}
                    onChange={onChangeForm}
                />

                <div>
                    <button className="ui-btn-primary" type="submit" disabled={saving}>
                        {saving ? 'Enviando invitacion...' : 'Enviar invitacion'}
                    </button>
                </div>
            </form>
            {formMessage && (
                <p className={`mb-0 mt-2 text-sm ${formMessage.toLowerCase().includes('error') ? 'text-red-600' : 'ui-muted'}`}>
                    {formMessage}
                </p>
            )}
        </section>
    )
}