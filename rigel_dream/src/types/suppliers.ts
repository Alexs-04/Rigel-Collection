export interface SupplierProduct {
    name: string
    price: number | string
    stock: number | string
}

export interface SupplierBase {
    name: string
    contactEmail: string
    phoneNumber: string
    address: string
}

export interface SupplierSummary extends SupplierBase {}

export interface SupplierDetail extends SupplierBase {
    products?: SupplierProduct[]
}

export interface SupplierFormValues extends SupplierBase {
    products: SupplierProduct[]
}

