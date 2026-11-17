import type {ReactNode} from 'react'
import RigelMark from './RigelMark'

interface AuthShellProps {
    children: ReactNode
    center?: boolean
}

/* Schematic Orion. Rigel carries the brand blue; Betelgeuse is the one warm note. */
const STARS = {
    meissa: [200, 62],
    betelgeuse: [118, 128],
    bellatrix: [272, 112],
    alnitak: [168, 272],
    alnilam: [200, 256],
    mintaka: [232, 240],
    saiph: [142, 420],
    rigel: [292, 404],
} as const

const LINES: Array<[keyof typeof STARS, keyof typeof STARS]> = [
    ['meissa', 'betelgeuse'],
    ['meissa', 'bellatrix'],
    ['betelgeuse', 'bellatrix'],
    ['betelgeuse', 'alnitak'],
    ['bellatrix', 'mintaka'],
    ['alnitak', 'alnilam'],
    ['alnilam', 'mintaka'],
    ['alnitak', 'saiph'],
    ['mintaka', 'rigel'],
    ['saiph', 'rigel'],
]

function Orion() {
    return (
        <svg viewBox="0 0 400 480" className="h-full max-h-[440px] w-full overflow-visible" role="img" aria-label="Constelación de Orión con Rigel resaltada">
            <defs>
                <radialGradient id="orion-rigel-halo">
                    <stop offset="0" stopColor="#2f6bff" stopOpacity="0.45"/>
                    <stop offset="1" stopColor="#2f6bff" stopOpacity="0"/>
                </radialGradient>
                <radialGradient id="orion-betelgeuse-halo">
                    <stop offset="0" stopColor="#ff9466" stopOpacity="0.4"/>
                    <stop offset="1" stopColor="#ff9466" stopOpacity="0"/>
                </radialGradient>
            </defs>

            {LINES.map(([a, b]) => (
                <line
                    key={`${a}-${b}`}
                    x1={STARS[a][0]} y1={STARS[a][1]} x2={STARS[b][0]} y2={STARS[b][1]}
                    stroke="#5f6b85" strokeWidth="1" strokeDasharray="2 5"
                />
            ))}

            {(['meissa', 'bellatrix', 'alnitak', 'alnilam', 'mintaka', 'saiph'] as const).map((name) => (
                <circle key={name} cx={STARS[name][0]} cy={STARS[name][1]} r="3" fill="#dbe2ee"/>
            ))}

            <circle cx={STARS.betelgeuse[0]} cy={STARS.betelgeuse[1]} r="26" fill="url(#orion-betelgeuse-halo)"/>
            <circle cx={STARS.betelgeuse[0]} cy={STARS.betelgeuse[1]} r="5.5" fill="#ff9466"/>
            <text x={STARS.betelgeuse[0] - 16} y={STARS.betelgeuse[1] + 4} textAnchor="end" className="fill-ink-400 font-mono text-[10px] tracking-[0.12em]">
                α BETELGEUSE
            </text>

            <g transform={`translate(${STARS.rigel[0]} ${STARS.rigel[1]})`}>
                <circle r="90" fill="url(#orion-rigel-halo)"/>
                <circle r="26" fill="none" stroke="#5f8fff" strokeWidth="1" opacity="0.55"/>
                <circle r="44" fill="none" stroke="#5f8fff" strokeWidth="1" opacity="0.22"/>
                <path className="star-twinkle" transform="translate(-14 -14) scale(1.1667)" fill="#bfd4ff"
                      d="M12 0c.8 7 5 11.2 12 12-7 .8-11.2 5-12 12-.8-7-5-11.2-12-12 7-.8 11.2-5 12-12Z"/>
            </g>
            <text x={STARS.rigel[0] + 34} y={STARS.rigel[1] - 30} className="fill-ink-100 font-mono text-[10px] tracking-[0.12em]">
                β RIGEL
            </text>
        </svg>
    )
}

export default function AuthShell({children, center = false}: AuthShellProps) {
    return (
        <div className="grid min-h-screen bg-app-bg lg:grid-cols-[1.05fr_1fr]">
            <aside
                className="relative hidden flex-col justify-between overflow-hidden bg-ink-950 p-10 text-ink-100 lg:flex"
                style={{
                    backgroundImage:
                        'radial-gradient(60% 45% at 78% 72%, rgb(47 107 255 / 0.22), transparent 70%), ' +
                        'radial-gradient(50% 40% at 12% 0%, rgb(43 179 230 / 0.10), transparent 70%), ' +
                        'linear-gradient(160deg, #0a0f1e 0%, #0c1530 60%, #0a0f1e 100%)',
                }}
            >
                <div className="flex items-center gap-2.5">
                    <RigelMark className="h-5 w-5 text-brand-400"/>
                    <span className="font-display text-xl font-semibold tracking-tight">Rigel</span>
                </div>

                <div className="flex min-h-0 flex-1 items-center justify-center py-6">
                    <Orion/>
                </div>

                <div>
                    <p className="m-0 font-display text-5xl font-semibold leading-[0.95] tracking-[-0.035em] xl:text-6xl">
                        Ventas, inventario y compras en una sola órbita.
                    </p>
                    <p className="mb-0 mt-5 font-mono text-[0.68rem] tracking-[0.14em] text-ink-400">
                        β ORIONIS · PUNTO DE VENTA
                    </p>
                </div>
            </aside>

            <main className="bg-chart grid place-items-center p-6">
                <div className="w-full max-w-sm">
                    <div className="mb-8 flex items-center gap-2.5 lg:hidden">
                        <RigelMark className="h-5 w-5 text-brand-600 dark:text-brand-400"/>
                        <span className="font-display text-xl font-semibold tracking-tight text-app-text">Rigel</span>
                    </div>

                    <div className={`ui-card p-7 shadow-raised ${center ? 'text-center' : ''}`}>
                        {children}
                    </div>

                    <div className="ui-eyebrow mt-5">© {new Date().getFullYear()} Rigel</div>
                </div>
            </main>
        </div>
    )
}
