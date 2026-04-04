import type {HTMLAttributes} from 'react'

interface CardProps extends HTMLAttributes<HTMLElement> {
    as?: 'section' | 'article' | 'div'
    compact?: boolean
}

export default function Card({as = 'section', compact = false, className = '', ...props}: CardProps) {
    const Element = as
    const spacing = compact ? 'p-4' : 'p-5'

    return <Element className={`ui-card ${spacing} ${className}`.trim()} {...props} />
}

