import { useState } from 'react'
import { api } from '../api'

export default function SenderPage({ t }) {
  const [code, setCode]   = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleCheck = async () => {
    const trimmed = code.trim().toUpperCase()
    if (!trimmed) return
    setLoading(true); setError(null); setResult(null)
    try {
      const data = await api.sender(trimmed)
      setResult(data)
    } catch {
      setError(t.errors.network)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>{t.sender.title}</h1>
      <div className="card">
        <p style={{ marginBottom: '0.75rem', color: 'var(--muted)', fontSize: '0.88rem' }}>
          Enter the code after the hyphen in a sender header.<br />
          Example: from <strong>VK-HDFCBK</strong>, enter <strong>HDFCBK</strong>
        </p>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            type="text"
            value={code}
            onChange={e => setCode(e.target.value.toUpperCase())}
            placeholder={t.sender.placeholder}
            aria-label={t.sender.placeholder}
            onKeyDown={e => e.key === 'Enter' && handleCheck()}
            style={{ flex: 1 }}
          />
          <button className="btn btn-primary" onClick={handleCheck} disabled={loading}>
            {loading ? '…' : t.sender.check}
          </button>
        </div>

        {error && <div className="error-box">{error}</div>}

        {result && (
          <div className={`sender-result ${result.known ? 'sender-known' : 'sender-unknown'}`}>
            {result.known ? (
              <>
                <strong>✅ {t.sender.known}</strong><br />
                <span>Bank: <strong>{result.bank}</strong></span><br />
                <span>Code: {result.senderCode}</span>
              </>
            ) : (
              <>
                <strong>⚠️ {t.sender.unknown}</strong><br />
                <span>"{code}" is not in our database of known bank sender codes.</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* Quick reference table */}
      <div className="card">
        <h2 style={{ marginBottom: '0.75rem' }}>Common bank sender codes</h2>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
            <thead>
              <tr style={{ background: 'var(--bg)' }}>
                <th style={{ padding: '0.5rem', textAlign: 'left', borderBottom: '1px solid var(--border)' }}>Bank</th>
                <th style={{ padding: '0.5rem', textAlign: 'left', borderBottom: '1px solid var(--border)' }}>Code</th>
                <th style={{ padding: '0.5rem', textAlign: 'left', borderBottom: '1px solid var(--border)' }}>Example header</th>
              </tr>
            </thead>
            <tbody>
              {[
                ['HDFC Bank',     'HDFCBK',  'VK-HDFCBK'],
                ['SBI',          'SBIINB',  'AX-SBIINB'],
                ['ICICI Bank',   'ICICIB',  'JD-ICICIB'],
                ['Axis Bank',    'AXISBK',  'BT-AXISBK'],
                ['Kotak Bank',   'KOTAKB',  'MN-KOTAKB'],
                ['PNB',          'PNBSMS',  'GP-PNBSMS'],
                ['Bank of Baroda','BOBSMS', 'RA-BOBSMS'],
                ['Paytm',        'PYTMBK',  'AD-PYTMBK'],
              ].map(([bank, code, ex]) => (
                <tr key={code}>
                  <td style={{ padding: '0.5rem', borderBottom: '1px solid var(--border)' }}>{bank}</td>
                  <td style={{ padding: '0.5rem', borderBottom: '1px solid var(--border)', fontFamily: 'monospace' }}>{code}</td>
                  <td style={{ padding: '0.5rem', borderBottom: '1px solid var(--border)', color: 'var(--muted)', fontFamily: 'monospace' }}>{ex}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
