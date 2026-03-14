export interface LoadDetailSafelyConfig<T> {
    requestRef: {current: number}
    fetchDetail: (requestId: number) => Promise<T>
    onSuccess?: (result: T, requestId: number) => void
    onError?: (error: unknown, requestId: number) => void
    onFinally?: (requestId: number) => void
}

export function loadDetailSafely<T>(config: LoadDetailSafelyConfig<T>): Promise<void>

