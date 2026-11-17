import type {ReactNode} from 'react'
import {useLocation} from 'react-router-dom'
import {findNavItem} from '../../layout/navigation'

interface PageHeaderProps {
    title: string
    description?: ReactNode
    /** Right-aligned slot for a counter or actions. */
    aside?: ReactNode
    /** Extra content under the title (e.g. summary figures). */
    children?: ReactNode
}

export default function PageHeader({title, description, aside, children}: PageHeaderProps) {
    const {pathname} = useLocation()
    const index = findNavItem(pathname)?.index

    return (
        <header className="page-header">
            <div className="flex flex-wrap items-end justify-between gap-x-8 gap-y-3">
                <div className="min-w-0">
                    <h1>
                        {title}
                        {index && <sup>{index}</sup>}
                    </h1>
                    {description && <p className="mb-0 mt-3 max-w-2xl text-sm text-app-muted">{description}</p>}
                </div>
                {aside && <div className="ui-eyebrow shrink-0 pb-0.5">{aside}</div>}
            </div>
            {children}
        </header>
    )
}
