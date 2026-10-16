import { useState, useEffect } from 'react'
import { api } from '../api'

const BADGE = { GENUINE: 'genuine', SUSPICIOUS: 'suspicious', FAKE: 'fake' }
const ICON  = { GENUINE: '✅', SUSPICIOUS: '⚠️', FAKE: '🚨' }

export default function HistoryPage({ t }) {
  const [items, setItems]   = useState(null)
  const [error, setError]   = useState(null)
  const [expanded, setExpanded] = useState(null)

  useEffect(() => {
    api.history()
      .then(setItems)
      .catch(() => setError(t.errors.network))
  }, [])

  if (error) return <div className="error-box">{error}</div>
  if (!items) return <p style={{ color: 'var(--muted)' }}>Loading…</p>

  return (
    <div>
      <h1>{t.history.title}</h1>
      {items.length === 0 && <p style={{ color: 'var(--muted)' }}>{t.history.empty}</p>}
      {items.map(item => (
        <div
          key={item.id}
          className="history-item"
          onClick={() => setExpanded(e => e === item.id ? null : item.id)}
          role="button"
          tabIndex={0}
          onKeyDown={e => e.key === 'Enter' && setExpanded(ex => ex === item.id ? null : item.id)}
          aria-expanded={expanded === item.id}
        >
          <span style={{ fontSize: '1.5rem' }} aria-hidden="true">{ICON[item.verdict] || '❓'}</span>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
              <span className={`badge badge-${BADGE[item.verdict] || ''}`}>{item.verdict}</span>
              {item.scamType && (
                <span style={{ fontSize: '0.78rem', color: 'var(--muted)' }}>
                  {item.scamType.replace(/_/g, ' ')}
                </span>
              )}
              <span style={{ fontSize: '0.75rem', color: 'var(--muted)', marginLeft: 'auto' }}>
                {new Date(item.createdAt).toLocaleString()}
              </span>
            </div>
            {expanded === item.id
              ? <p className="history-text">{item.maskedMessage}</p>
              : <p className="history-text">{t.history.expand}</p>
            }
          </div>
        </div>
      ))}
    </div>
  )
}
