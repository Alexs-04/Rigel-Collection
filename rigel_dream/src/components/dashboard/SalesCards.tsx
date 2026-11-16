import type {DashboardSalesSummary} from '../../types/dashboard'
import {formatCurrency} from './formatters'

interface SalesCardsProps {
    sales?: DashboardSalesSummary
}

export default function SalesCards({sales}: SalesCardsProps) {
    return (
        <section className="grid gap-4 md:grid-cols-[1.4fr_1fr_1fr]">
            {/* The day's takings is the figure people open the dashboard for. */}
            <article className="rounded-app border border-app-text bg-app-text p-5 text-app-bg shadow-[4px_4px_0_theme(colors.brand.500)]">
                <p className="ui-figure-label !text-app-bg/60">Ventas del dia</p>
                <h3 className="m-0 mt-3 font-display text-5xl font-semibold leading-none tracking-[-0.035em] !text-app-bg">
                    {formatCurrency(sales?.day)}
                </h3>
            </article>
            {[
                {label: 'Ventas del mes', value: sales?.month},
                {label: 'Ventas del año', value: sales?.year},
            ].map((item) => (
                <article key={item.label} className="ui-card p-5">
                    <p className="ui-figure-label">{item.label}</p>
                    <h3 className="ui-figure-value">{formatCurrency(item.value)}</h3>
                </article>
            ))}
        </section>
    )
}
