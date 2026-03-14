export interface LoadDetailSafelyConfig<T> {
	requestRef: {current: number}
	fetchDetail: (requestId: number) => Promise<T>
	onSuccess?: (result: T, requestId: number) => void
	onError?: (error: unknown, requestId: number) => void
	onFinally?: (requestId: number) => void
}

export async function loadDetailSafely<T>({
	requestRef,
	fetchDetail,
	onSuccess,
	onError,
	onFinally,
}: LoadDetailSafelyConfig<T>): Promise<void> {
	const requestId = ++requestRef.current

	try {
		const result = await fetchDetail(requestId)
		if (requestId !== requestRef.current) return
		onSuccess?.(result, requestId)
	} catch (error) {
		if (requestId !== requestRef.current) return
		onError?.(error, requestId)
	} finally {
		if (requestId === requestRef.current) {
			onFinally?.(requestId)
		}
	}
}

