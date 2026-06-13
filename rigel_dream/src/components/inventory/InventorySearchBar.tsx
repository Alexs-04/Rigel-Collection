interface InventorySearchBarProps {
    search: string
    onSearchChange: (value: string) => void
    onReload: () => void
    loading: boolean
}

export default function InventorySearchBar({search, onSearchChange, onReload, loading}: InventorySearchBarProps) {
    return (
        <section className="ui-card p-4">
            <div className="flex flex-col gap-3 md:flex-row md:items-center">
                <input
                    className="ui-input flex-1"
                    placeholder="Buscar por nombre, código, categoría o proveedor"
                    value={search}
                    onChange={(event) => onSearchChange(event.target.value)}
                />
                <button className="ui-btn-ghost md:w-auto" type="button" onClick={onReload} disabled={loading}>
                    {loading ? 'Cargando...' : 'Actualizar'}
                </button>
            </div>
        </section>
    )
}

