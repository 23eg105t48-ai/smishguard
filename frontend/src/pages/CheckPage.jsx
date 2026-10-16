import { useState } from 'react'
import { api } from '../api'

// Sample messages to fill the form quickly
const SAMPLES = {
  fakeKyc: {
    message: 'URGENT!!! Your SBI Account has been BLOCKED due to KYC not updated. Update now at http://sbi-kyc-update.xyz or account will be closed permanently',
    sender: '9876543210',
  },
  fakeDebit: {
    message: 'HDFC Bank: Unrecognized debit of Rs.9,800 from your A/c. If not done by you, call IMMEDIATELY: 9988776655 to reverse. Share your OTP to verify.',
    sender: '9988776655',
  },
  genuineDebit: {
    message: 'Rs.500.00 debited from A/c XXXXXX1234 on 05-Oct-26. Avl Bal Rs.9,500.00. Not you? Call HDFC 18002586161',
    sender: 'VK-HDFCBK',
  },
  genuineOtp: {
    message: '483921 is your OTP for HDFC Bank NetBanking txn of Rs.2000. Valid 10 min. Do not share it with anyone. -HDFCBK',
    sender: 'VK-HDFCBK',
  },
}

/** Renders the masked message with highlights wrapped in <mark> tags. */
function HighlightedMessage({ text, highlights }) {
  if (!highlights || highlights.length === 0) return <p>{text}</p>

  // Build an array of segments: {text, cat or null}
  let segments = [{ text, cat: null }]

  for (const h of highlights) {
    const phrase = h.phrase
    const newSegments = []
    for (const seg of segments) {
      if (seg.cat !== null) { newSegments.push(seg); continue }
      const idx = seg.text.indexOf(phrase)
      if (idx === -1) { newSegments.push(seg); continue }
      if (idx > 0) newSegments.push({ text: seg.text.slice(0, idx), cat: null })
      newSegments.push({ text: phrase, cat: h.category })
      const after = seg.text.slice(idx + phrase.length)
      if (after) newSegments.push({ text: after, cat: null })
    }
    segments = newSegments
  }

  return (
    <p>
      {segments.map((s, i) =>
        s.cat
          ? <mark key={i} data-cat={s.cat}>{s.text}</mark>
          : <span key={i}>{s.text}</span>
      )}
    </p>
  )
}

/** Animated risk score bar */
function ScoreBar({ score, verdict }) {
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.25rem' }}>
        <span>0</span>
        <strong>{score}/100</strong>
        <span>100</span>
      </div>
      <div className={`score-bar-outer score-bar-${verdict}`}>
        <div className="score-bar-inner" style={{ width: `${score}%` }} />
      </div>
    </div>
  )
}

const VERDICT_ICONS = { GENUINE: '✅', SUSPICIOUS: '⚠️', FAKE: '🚨' }

export default function CheckPage({ t }) {
  const [message, setMessage]     = useState('')
  const [sender, setSender]       = useState('')
  const [scanLinks, setScanLinks] = useState(true)
  const [useAi, setUseAi]         = useState(true)
  const [loading, setLoading]     = useState(false)
  const [result, setResult]       = useState(null)
  const [error, setError]         = useState(null)
  const [reported, setReported]   = useState(false)

  const fillSample = (key) => {
    setMessage(SAMPLES[key].message)
    setSender(SAMPLES[key].sender)
    setResult(null)
    setError(null)
  }

  const handleCheck = async () => {
    if (!message.trim()) { setError(t.errors.empty); return }
    setLoading(true); setError(null); setResult(null)
    try {
      const data = await api.check({ message, sender: sender || undefined, scanLinks, useAi })
      setResult(data)
    } catch (e) {
      if (e.message === 'rate_limit') setError(t.errors.rateLimit)
      else if (e.message === 'Failed to fetch') setError(t.errors.network)
      else setError(t.errors.generic)
    } finally {
      setLoading(false)
    }
  }

  const handleReport = async () => {
    if (!result) return
    try {
      await api.report({ message, userSaysIs: result.verdict === 'GENUINE' ? 'fake' : 'genuine', systemVerdict: result.verdict })
      setReported(true)
    } catch { /* ignore */ }
  }

  const handleShare = () => {
    if (!result) return
    const text = `SmishGuard detected this SMS as *${result.verdict}* (risk score: ${result.riskScore}/100). ${result.advice} Check your SMS at smishguard.app`
    window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, '_blank')
  }

  const handleCopy = () => {
    if (!result) return
    const text = `Verdict: ${result.verdict}\nRisk score: ${result.riskScore}/100\nScam type: ${result.scamType || 'N/A'}\nAdvice: ${result.advice}`
    navigator.clipboard.writeText(text)
  }

  const verdictLabel = result
    ? (result.verdict === 'GENUINE'    ? t.check.verdictGenuine
       : result.verdict === 'SUSPICIOUS' ? t.check.verdictSuspicious
       : t.check.verdictFake)
    : ''

  const catLabel = (cat) => t.check.categories[cat] || cat

  // Unique categories in highlights
  const usedCats = result
    ? [...new Set((result.highlights || []).map(h => h.category))]
    : []

  return (
    <div>
      <h1>{t.appName}</h1>
      <p style={{ color: 'var(--muted)', marginBottom: '1rem' }}>{t.subtitle}</p>

      {/* Input form */}
      <div className="card">
        <label htmlFor="msg-area" style={{ fontWeight: 600, display: 'block', marginBottom: '0.5rem' }}>
          {t.check.title}
        </label>
        <textarea
          id="msg-area"
          rows={5}
          maxLength={1000}
          value={message}
          onChange={e => setMessage(e.target.value)}
          placeholder={t.check.messagePlaceholder}
          aria-label={t.check.messagePlaceholder}
        />
        <input
          type="text"
          style={{ marginTop: '0.5rem' }}
          value={sender}
          onChange={e => setSender(e.target.value)}
          placeholder={t.check.senderPlaceholder}
          aria-label={t.check.senderPlaceholder}
        />

        <div style={{ marginTop: '0.75rem' }}>
          <label className="toggle-row">
            <input type="checkbox" checked={scanLinks} onChange={e => setScanLinks(e.target.checked)} />
            {t.check.scanLinks}
          </label>
          <label className="toggle-row">
            <input type="checkbox" checked={useAi} onChange={e => setUseAi(e.target.checked)} />
            {t.check.useAi}
          </label>
        </div>

        <div style={{ marginTop: '1rem', display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>
          <button className="btn btn-primary" onClick={handleCheck} disabled={loading} aria-busy={loading}>
            {loading ? <><span className="spinner" />{t.check.checking}</> : t.check.checkButton}
          </button>
        </div>

        {/* Sample buttons */}
        <p className="section-title" style={{ marginTop: '1rem' }}>{t.check.trySample}</p>
        <div className="sample-row">
          <button className="btn btn-outline" onClick={() => fillSample('fakeKyc')}>{t.check.sampleFakeKyc}</button>
          <button className="btn btn-outline" onClick={() => fillSample('fakeDebit')}>{t.check.sampleFakeDebit}</button>
          <button className="btn btn-outline" onClick={() => fillSample('genuineDebit')}>{t.check.sampleGenuineDebit}</button>
          <button className="btn btn-outline" onClick={() => fillSample('genuineOtp')}>{t.check.sampleGenuineOtp}</button>
        </div>

        <p className="privacy-note" aria-live="polite">
          🔒 {t.check.privacyNote}
        </p>
      </div>

      {/* Error */}
      {error && <div className="error-box" role="alert">{error}</div>}

      {/* Result card */}
      {result && (
        <div className={`card result-card result-${result.verdict}`} role="region" aria-label="Result">
          <div className={`verdict-header verdict-${result.verdict}`}>
            <span className="verdict-icon" aria-hidden="true">{VERDICT_ICONS[result.verdict]}</span>
            <div>
              <div className="verdict-title">{verdictLabel}</div>
              {result.scamType && (
                <span className={`badge badge-${result.verdict.toLowerCase()}`}>
                  {result.scamType.replace(/_/g, ' ')}
                </span>
              )}
            </div>
          </div>

          <p className="section-title">{t.check.riskScore}</p>
          <ScoreBar score={result.riskScore} verdict={result.verdict} />

          {/* Highlighted message */}
          {result.highlights && result.highlights.length > 0 && (
            <>
              <p className="section-title">{t.check.highlightLegend}</p>
              <div className="highlight-box">
                <HighlightedMessage text={message} highlights={result.highlights} />
              </div>
              {/* Legend */}
              {usedCats.length > 0 && (
                <div className="legend">
                  {usedCats.map(cat => (
                    <span key={cat} className="legend-item">
                      <span className={`legend-dot dot-${cat}`} />
                      {catLabel(cat)}
                    </span>
                  ))}
                </div>
              )}
            </>
          )}

          {/* Reasons */}
          {result.reasons && result.reasons.length > 0 && (
            <>
              <p className="section-title">{t.check.why}</p>
              <ul className="reasons-list">
                {result.reasons.map((r, i) => <li key={i}>{r}</li>)}
              </ul>
            </>
          )}

          {/* Link status */}
          {result.linkStatus && result.linkStatus !== 'no links' && (
            <p style={{ fontSize: '0.85rem', color: 'var(--muted)', marginTop: '0.5rem' }}>
              🔗 {t.check.linkStatus}: {result.linkStatus}
            </p>
          )}

          {/* Advice */}
          <div className="advice-box">ℹ️ {result.advice}</div>

          {/* Action buttons */}
          <div className="action-row">
            <button className="btn btn-outline" onClick={handleReport} disabled={reported}>
              {reported ? '✓ Reported' : t.check.reportWrong}
            </button>
            <button className="btn btn-outline" onClick={handleShare}>{t.check.shareWarning} 📲</button>
            <button className="btn btn-outline" onClick={handleCopy}>{t.check.copyResult} 📋</button>
          </div>
        </div>
      )}
    </div>
  )
}
