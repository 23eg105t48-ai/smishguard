# Seed Data – README

This folder contains CSV files that are loaded once on startup by `DataSeederService`.
Tables are only seeded when **empty**, so the import is idempotent (safe to restart).

---

## File Descriptions

### `bank_sender_ids.csv`
Columns: `bank_name`, `sender_code`

| Column | Description |
|--------|-------------|
| bank_name | Full bank name (e.g. "HDFC Bank") |
| sender_code | The code used in SMS headers after the hyphen (e.g. "HDFCBK" from "VK-HDFCBK") |

**To add a new bank sender code**: add a row like:
```
My New Bank,MYNWBK
```

---

### `trusted_domains.csv`
Columns: `bank_name`, `domain`

| Column | Description |
|--------|-------------|
| bank_name | Full bank name |
| domain | Root domain only, lowercase, no `www.` prefix (e.g. `hdfcbank.com`) |

Sub-domains are matched automatically (e.g. `netbanking.hdfcbank.com` is trusted if `hdfcbank.com` is in this list).

**To add a trusted domain**: add a row like:
```
My Bank,mybank.co.in
```

---

### `genuine_templates.csv`
Columns: `bank_name`, `type`, `template_text`

| Column | Description |
|--------|-------------|
| bank_name | Bank name |
| type | Message category: `debit`, `credit`, `otp`, `upi`, `balance`, `emi_reminder`, etc. |
| template_text | A Java regex pattern that matches real bank messages of this type |

A message that matches any template gets **−15 points** (reduces suspicion).
Use raw regex syntax. Avoid overly broad patterns that could match scam messages.

**To add a template**: add a row like:
```
My Bank,debit,Rs\.\d+ debited from My Bank A/c
```

---

### `reference_messages.csv`
Columns: `message_text`, `label`, `type`, `bank`, `sender`

| Column | Description |
|--------|-------------|
| message_text | The SMS text (use masked versions: XXXXXX1234, <OTP>, <PHONE>) |
| label | `genuine` or `fake` |
| type | Message/scam type: `debit`, `credit`, `otp`, `kyc_update`, `account_blocked`, etc. |
| bank | Bank name identifier (e.g. "HDFC", "SBI", "ICICI") |
| sender | Sender header (e.g. "VK-HDFCBK") or phone number for fake messages |

These messages are used as **few-shot examples** in the LLM prompt. They are NOT the same as `test_messages.csv`.

**How to add your own real (masked) messages**:
1. Mask all personal numbers: account numbers → `XXXXXX1234`, OTPs → `<OTP>`, phone numbers → `<PHONE>`
2. Determine the label (`genuine` or `fake`) and type
3. Add a row to this file
4. Restart the application – it will seed the new rows (only if the table is empty, so you may need to clear it first with H2 console at `http://localhost:8080/h2-console`)

> ⚠️ **Never** put real account numbers, OTPs, or phone numbers in these CSV files.

---

## Notes

- CSV files use comma as delimiter. If your text contains a comma, wrap the field in double quotes.
- Encoding: UTF-8.
- No header row – the first row is always data.
