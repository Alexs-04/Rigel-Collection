import type {SVGAttributes} from 'react'

/** Four-point star used as the Rigel brand mark. Inherits `currentColor`. */
export default function RigelMark({className = 'h-5 w-5', ...props}: SVGAttributes<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" className={className} {...props}>
            <path d="M12 0c.8 7 5 11.2 12 12-7 .8-11.2 5-12 12-.8-7-5-11.2-12-12 7-.8 11.2-5 12-12Z"/>
        </svg>
    )
}
