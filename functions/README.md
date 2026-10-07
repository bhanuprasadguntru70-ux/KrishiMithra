# Agmarknet Mandi Rates Firebase Cloud Function Proxy

This directory contains the Firebase Cloud Function (`getLiveMandiRates`) that acts as a secure backend proxy between the **Agmarknet / data.gov.in API** and the mobile app.

---

## 🔒 Architecture & Features

1. **API Key Protection**:
   - The Agmarknet API key (`AGMARKNET_API_KEY`) is stored securely on the server-side in Firebase environment secrets.
   - It is never exposed in client-side mobile code or public Android APK binaries.

2. **Authentication Verification**:
   - Accepts Bearer token headers (`Authorization: Bearer <ID_TOKEN>`) issued by Firebase Authentication.
   - Verifies user identities before routing requests to upstream data sources.

3. **Smart Firestore Caching**:
   - Caches responses in the Cloud Firestore collection `mandi_rates_cache` with a 30-minute Time-To-Live (TTL).
   - Reduces external API calls to avoid rate-limiting and minimize data usage costs.
   - Falls back gracefully to stale cache if data.gov.in is unreachable.

4. **Data Normalization & Verification**:
   - Normalizes raw Agmarknet JSON payload into structured objects containing:
     - `commodity`, `variety`, `state`, `district`, `market`
     - `min_price`, `max_price`, `modal_price` (in ₹/quintal)
     - `arrival_quantity`, `market_date`, `fetched_at`
     - `source`: `"AGMARKNET / Government Open Data Platform (data.gov.in)"`
     - `verification_status`: `"🟢 Verified Government Source"`

---

## 🛠 Setup & Deployment

### 1. Prerequisites
- Node.js 18+ installed
- Firebase CLI installed (`npm install -g firebase-tools`)
- Firebase project linked (`firebase use --add`)

### 2. Configure API Secret
Set your `AGMARKNET_API_KEY` using Firebase Secrets Manager:
```bash
firebase functions:secrets:set AGMARKNET_API_KEY
```

Alternatively, set environment variables locally in `functions/.env`:
```env
AGMARKNET_API_KEY=579b464db66ec2...
REQUIRE_AUTH=true
```

### 3. Build & Test Locally
```bash
cd functions
npm install
npm run build
npm run serve
```

### 4. Deploy to Firebase
```bash
firebase deploy --only functions
```

---

## 📡 API Endpoint Usage

### Request
```http
GET /getLiveMandiRates?state=Andhra%20Pradesh&district=Eluru&commodity=Tomato&limit=50
Authorization: Bearer <FIREBASE_USER_ID_TOKEN>
```

### Response Example
```json
{
  "status": "success",
  "source": "AGMARKNET / data.gov.in",
  "fetched_at": "2026-08-05T04:30:00.000Z",
  "is_cached": false,
  "total": 1,
  "records": [
    {
      "commodity": "Tomato",
      "variety": "Local",
      "state": "Andhra Pradesh",
      "district": "Eluru",
      "market": "Eluru",
      "min_price": "2200",
      "max_price": "2800",
      "modal_price": "2500",
      "arrival_quantity": "45",
      "unit": "₹/quintal",
      "market_date": "05/08/2026",
      "fetched_at": "2026-08-05T04:30:00.000Z",
      "source": "AGMARKNET / Government Open Data Platform (data.gov.in)",
      "verification_status": "🟢 Verified Government Source",
      "is_cached": false
    }
  ]
}
```
