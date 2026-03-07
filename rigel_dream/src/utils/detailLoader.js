export async function loadDetailSafely({
                                           requestRef,
                                           fetchDetail,
                                           onSuccess,
                                           onError,
                                           onFinally,
                                       }) {
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

