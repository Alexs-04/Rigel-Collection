const CATEGORY_LABELS = {
    SODA: 'Refrescos',
    BEER: 'Cervezas',
    COOKIES: 'Galletas',
    CRISPY_POTATOES: 'Papas fritas',
    CHOCOLATE: 'Chocolate',
    CANDY: 'Dulces',
    ALCOHOL: 'Alcohol',
    WATER: 'Agua',
    ENERGY_DRINK: 'Bebidas energéticas',
    SUGAR_DRINK: 'Bebidas azucaradas',
    HYGIENE: 'Higiene',
    MEDICAL: 'Medico',
    DAIRY: 'Lácteos',
    CANNED: 'Enlatados',
    SWEET_BREAD : 'Pan dulce',
    OTHERS: 'Otros',
}

export const CATEGORY_OPTIONS_ES = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({
    value,
    label,
}))

export function toCategoryLabel(categoryValue) {
    return CATEGORY_LABELS[categoryValue] || CATEGORY_LABELS.OTHERS
}

export function formatPriceMxn(amount) {
    const numericAmount = Number(amount)
    if (!Number.isFinite(numericAmount)) return 'MXN $0.00'
    return `MXN $${numericAmount.toFixed(2)}`
}

