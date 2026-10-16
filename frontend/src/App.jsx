import { useState } from 'react'
import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom'
import translations from './i18n'
import CheckPage    from './pages/CheckPage'
import HistoryPage  from './pages/HistoryPage'
import StatsPage    from './pages/StatsPage'
import LearnPage    from './pages/LearnPage'
import SenderPage   from './pages/SenderPage'

/** Shield SVG logo inline – no external asset needed */
function ShieldLogo() {
  return (
    <svg width="32" height="36" viewBox="0 0 32 36" fill="none" aria-hidden="true">
      <path d="M16 2L2 8v10c0 9 7 16 14 18 7-2 14-9 14-18V8L16 2Z"
            fill="var(--brand)" opacity="0.9"/>
      <path d="M10 18l4 4 8-8" stroke="#fff" strokeWidth="2.5"
            strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  )
}

export default function App() {
  const [lang, setLang] = useState('en')
  const t = translations[lang]

  const toggleLang = () => setLang(l => l === 'en' ? 'te' : 'en')

  return (
    <BrowserRouter>
      {/* Top nav for desktop */}
      <header className="top-nav" role="banner">
        <div className="nav-brand">
          <ShieldLogo />
          <span className="brand-name">{t.appName}</span>
        </div>
        <nav className="nav-links" role="navigation" aria-label="Main navigation">
          <NavLink to="/"        end>{t.nav.check}</NavLink>
          <NavLink to="/history"    >{t.nav.history}</NavLink>
          <NavLink to="/stats"      >{t.nav.stats}</NavLink>
          <NavLink to="/learn"      >{t.nav.learn}</NavLink>
          <NavLink to="/sender"     >{t.nav.sender}</NavLink>
        </nav>
        <button className="lang-btn" onClick={toggleLang} aria-label="Switch language">
          {lang === 'en' ? 'తెలుగు' : 'English'}
        </button>
      </header>

      <main className="main-content">
        <Routes>
          <Route path="/"        element={<CheckPage   t={t} />} />
          <Route path="/history" element={<HistoryPage t={t} />} />
          <Route path="/stats"   element={<StatsPage   t={t} />} />
          <Route path="/learn"   element={<LearnPage   t={t} />} />
          <Route path="/sender"  element={<SenderPage  t={t} />} />
        </Routes>
      </main>

      {/* Bottom tab bar for mobile */}
      <nav className="bottom-nav" role="navigation" aria-label="Mobile navigation">
        <NavLink to="/"        end><span>🛡️</span><span>{t.nav.check}</span></NavLink>
        <NavLink to="/history"    ><span>📋</span><span>{t.nav.history}</span></NavLink>
        <NavLink to="/stats"      ><span>📊</span><span>{t.nav.stats}</span></NavLink>
        <NavLink to="/learn"      ><span>📖</span><span>{t.nav.learn}</span></NavLink>
        <NavLink to="/sender"     ><span>🔍</span><span>{t.nav.sender}</span></NavLink>
      </nav>
    </BrowserRouter>
  )
}
