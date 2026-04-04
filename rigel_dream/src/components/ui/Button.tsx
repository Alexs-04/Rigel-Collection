import type {ButtonHTMLAttributes} from 'react'

type Variant = 'primary' | 'ghost' | 'danger'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: Variant
}

const variantClasses: Record<Variant, string> = {
    primary: 'ui-btn-primary',
    ghost: 'ui-btn-ghost',
    danger: 'ui-btn-danger',
}

export default function Button({variant = 'primary', className = '', type = 'button', ...props}: ButtonProps) {
    return <button type={type} className={`${variantClasses[variant]} ${className}`.trim()} {...props} />
}

