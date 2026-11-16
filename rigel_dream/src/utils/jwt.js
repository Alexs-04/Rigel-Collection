/**
 * Decodes the payload of a JWT without verifying it (verification happens on the server).
 *
 * JWTs use base64url (RFC 4648 §5) and UTF-8, so `atob` alone is not enough: it fails on
 * `-`/`_` characters and garbles non-ASCII names (e.g. "José").
 *
 * @param {string | null | undefined} token
 * @returns {Record<string, any> | null}
 */
export function decodeJwtPayload(token) {
    if (!token || typeof token !== 'string') return null;
    const segment = token.split('.')[1];
    if (!segment) return null;

    try {
        const base64 = segment.replace(/-/g, '+').replace(/_/g, '/');
        const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
        const binary = atob(padded);
        const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
        return JSON.parse(new TextDecoder().decode(bytes));
    } catch {
        return null;
    }
}

/**
 * @param {string | null | undefined} token
 * @param {number} skewSeconds treat the token as expired this many seconds before `exp`
 * @returns {boolean} true when the token is missing, unreadable or expired
 */
export function isJwtExpired(token, skewSeconds = 10) {
    const payload = decodeJwtPayload(token);
    if (!payload?.exp) return true;
    return payload.exp * 1000 <= Date.now() + skewSeconds * 1000;
}
