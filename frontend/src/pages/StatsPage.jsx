import { useState, useEffect } from 'react'
import { api } from '../api'

/** Simple horizontal bar drawn with CSS */
function Bar({ value, max }) {
  const pct = max > 0 ? Math.round((value / max) * 100) : 0
  return (
    <div className="bar-track">
      <div className="bar-fill" style={{ width: `${pct}%` }} />
    </div>
  )
}

export default function StatsPage({ t }) {
  const [stats, setStats] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.stats()
      .then(setStats)
      .catch(() => setError(t.errors.network))
  }, [])

  if (error) return <div className="error-box">{error}</div>
  if (!stats) return <p style={{ color: 'var(--muted)' }}>Loading…</p>

  const maxScam = Math.max(...(stats.topScamTypes || []).map(s => Number(s.count) || 0), 1)

  return (
    <div>
      <h1>{t.stats.title}</h1>

      {/* Overview boxes */}
      <div className="stat-grid">
        <div className="stat-box">
          <div className="stat-number">{stats.total}</div>
          <div className="stat-label">{t.stats.total}</div>
        </div>
        <div className="stat-box">
          <div className="stat-number stat-number-genuine">{stats.genuine}</div>
          <div className="stat-label">{t.stats.genuine}</div>
        </div>
        <div className="stat-box">
          <div className="stat-number stat-number-suspicious">{stats.suspicious}</div>
          <div className="stat-label">{t.stats.suspicious}</div>
        </div>
        <div className="stat-box">
          <div className="stat-number stat-number-fake">{stats.fake}</div>
          <div className="stat-label">{t.stats.fake}</div>
        </div>
      </div>

      {/* Top scam types */}
      {stats.topScamTypes && stats.topScamTypes.length > 0 && (
        <div className="card">
          <h2>{t.stats.topScams}</h2>
          <div className="bar-chart">
            {stats.topScamTypes.map(s => (
              <div key={s.type} className="bar-row">
                <span className="bar-label">{String(s.type).replace(/_/g, ' ')}</span>
                <Bar value={Number(s.count)} max={maxScam} />
                <span className="bar-count">{s.count}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Last 7 days */}
      {stats.last7Days && Object.keys(stats.last7Days).length > 0 && (
        <div className="card">
          <h2>{t.stats.last7}</h2>
          <div className="bar-chart">
            {Object.entries(stats.last7Days).map(([verdict, count]) => (
              <div key={verdict} className="bar-row">
                <span className="bar-label">{verdict}</span>
                <Bar value={Number(count)} max={Math.max(...Object.values(stats.last7Days).map(Number), 1)} />
                <span className="bar-count">{count}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
