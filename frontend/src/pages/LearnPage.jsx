const SCAM_TYPES = [
  {
    icon: '🪪',
    name: 'KYC Update Scam',
    look: 'An SMS saying your KYC is expired or your account will be blocked, with a link to update.',
    redFlags: ['Urgency words like "blocked", "24 hours"', 'Link to an unknown/look-alike site', 'Asks for Aadhaar/PAN via link'],
    whatToDo: 'Log in directly to your official bank app or website to check KYC status. Never click links in SMS.',
  },
  {
    icon: '🔒',
    name: 'Account Blocked Scam',
    look: 'A message saying your account is suspended due to suspicious activity.',
    redFlags: ['Comes from a mobile number, not a bank header', 'Asks you to call a number and share OTP or password', 'Creates panic'],
    whatToDo: 'Call your bank\'s official customer care number (printed on card/website). Never share OTP or password.',
  },
  {
    icon: '💸',
    name: 'Fake Debit Alert',
    look: 'An alert saying money was debited from your account – asking you to call a number to reverse it.',
    redFlags: ['Asks you to "call to reverse"', 'Asks for OTP or card details to "secure" account', 'Sender is a mobile number'],
    whatToDo: 'Check your actual bank app for real transactions. Call only the official bank helpline on the card.',
  },
  {
    icon: '🎁',
    name: 'Lottery / Prize Scam',
    look: 'You "won" a lottery or reward points. Asks for details or a fee to claim.',
    redFlags: ['You never entered a lottery', 'Asks for bank account details', 'Links to suspicious website'],
    whatToDo: 'Ignore and delete. Banks never run SMS lotteries. Report to cybercrime.gov.in.',
  },
  {
    icon: '📲',
    name: 'OTP Request Scam',
    look: 'Asks you to share your OTP with a "bank executive" to secure your account.',
    redFlags: ['Any request for OTP is a red flag', 'Creates urgency', 'Real banks never ask for OTP'],
    whatToDo: 'Never share OTP with anyone. Hang up. Call your bank directly.',
  },
  {
    icon: '📦',
    name: 'APK / App Download Scam',
    look: 'Asks you to download an APK or app from an SMS link to "protect" your account.',
    redFlags: ['Link to download an APK file', 'Not from official app stores', 'Claims to be a "security update"'],
    whatToDo: 'Never install apps from SMS links. Only use official Google Play or Apple App Store.',
  },
  {
    icon: '🏦',
    name: 'Fake Loan Scam',
    look: 'Pre-approved loan at 0% interest. No documents required. Click to disburse.',
    redFlags: ['No document verification is impossible for a real loan', 'Asks for processing fee upfront', 'Link goes to unknown site'],
    whatToDo: 'Ignore. Real banks verify income. Report to cybercrime.gov.in.',
  },
  {
    icon: '↩️',
    name: 'UPI Refund Scam',
    look: 'A message claiming a UPI refund is pending and needs your verification.',
    redFlags: ['Asks you to click a link and enter OTP/PIN to "receive" money', 'Entering OTP sends money OUT, not in'],
    whatToDo: 'To receive money on UPI you do NOT need to enter a PIN. Never enter PIN for incoming transfers.',
  },
]

const GOLDEN_RULES = [
  'Banks NEVER ask for OTP, PIN, CVV or password via SMS or phone call.',
  'Never click links in bank SMS messages. Type the URL directly in your browser.',
  'Verify any debit alert in your official bank app – not by calling numbers from the SMS.',
  'A sender header (like VK-HDFCBK) can be spoofed. Always double-check in the app.',
  'If in doubt, call the official bank helpline printed on your debit/credit card.',
]

export default function LearnPage({ t }) {
  return (
    <div>
      <h1>{t.learn.title}</h1>

      {/* 5 Golden Rules */}
      <div className="card">
        <h2>🏅 {t.learn.goldenRules}</h2>
        <ol className="rules-list">
          {GOLDEN_RULES.map((rule, i) => <li key={i}>{rule}</li>)}
        </ol>
      </div>

      {/* Scam type cards */}
      <h2 style={{ marginBottom: '0.75rem' }}>🔍 {t.learn.scamTypes}</h2>
      {SCAM_TYPES.map(s => (
        <div key={s.name} className="card learn-card">
          <h3>{s.icon} {s.name}</h3>
          <p><strong>What it looks like:</strong> {s.look}</p>
          <p style={{ marginTop: '0.4rem' }}><strong>Red flags:</strong></p>
          <ul style={{ paddingLeft: '1.2rem', color: 'var(--muted)', fontSize: '0.88rem', lineHeight: 1.7 }}>
            {s.redFlags.map((f, i) => <li key={i}>{f}</li>)}
          </ul>
          <p style={{ marginTop: '0.4rem' }}><strong>What to do:</strong> {s.whatToDo}</p>
        </div>
      ))}

      {/* Report fraud */}
      <div className="fraud-box">
        <h2 style={{ color: '#fff', marginBottom: '0.5rem' }}>🚨 {t.learn.reportFraud}</h2>
        <p>Website: <a href="https://cybercrime.gov.in" target="_blank" rel="noopener noreferrer">cybercrime.gov.in</a></p>
        <p style={{ marginTop: '0.4rem', fontSize: '1.3rem', fontWeight: 700 }}>Helpline: 1930</p>
        <p style={{ fontSize: '0.85rem', marginTop: '0.4rem', opacity: 0.85 }}>
          Available 24×7. Keep your transaction details ready.
        </p>
      </div>
    </div>
  )
}
