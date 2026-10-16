// Wrapper around fetch() for all backend API calls.
// Reads the base URL from the VITE_API_URL env var (defaults to '' = same origin).

const BASE = import.meta.env.VITE_API_URL || ''

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })

  if (res.status === 429) {
    throw new Error('rate_limit')
  }
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error(body.error || 'server_error')
  }
  return res.json()
}

export const api = {
  check: (body)       => request('/api/check', { method: 'POST', body: JSON.stringify(body) }),
  history: ()         => request('/api/history'),
  stats: ()           => request('/api/stats'),
  sender: (code)      => request(`/api/sender/${encodeURIComponent(code)}`),
  report: (body)      => request('/api/report', { method: 'POST', body: JSON.stringify(body) }),
  health: ()          => request('/api/health'),
}
