import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import axios from 'axios';
import cors from 'cors';

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

const corsHandler = cors({ origin: true });
const db = admin.firestore();

// Government Agmarknet API details
const AGMARKNET_RESOURCE_ID = '9efc1e83-5cba-4274-910e-9411d31cf407';
const BASE_URL = `https://api.data.gov.in/resource/${AGMARKNET_RESOURCE_ID}`;
const CACHE_TTL_MINUTES = 30; // 30-minute cache window to avoid excessive API calls

export interface MandiRateRecord {
  commodity: string;
  variety: string;
  state: string;
  district: string;
  market: string;
  min_price: string;
  max_price: string;
  modal_price: string;
  arrival_quantity: string;
  unit: string;
  market_date: string;
  fetched_at: string;
  source: string;
  verification_status: string;
  is_cached: boolean;
}

/**
 * Helper to verify Firebase Auth Bearer token from authorization header.
 */
async function verifyAuth(req: functions.https.Request): Promise<admin.auth.DecodedIdToken | null> {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  const idToken = authHeader.split('Bearer ')[1];
  try {
    return await admin.auth().verifyIdToken(idToken);
  } catch (err) {
    functions.logger.warn('Authentication token verification failed:', err);
    return null;
  }
}

/**
 * Firebase Cloud Function: getLiveMandiRates
 * Secure Proxy for Agmarknet API with authentication and Firestore caching.
 */
export const getLiveMandiRates = functions.https.onRequest((req, res) => {
  return corsHandler(req, res, async () => {
    try {
      // 1. Authentication Check
      const decodedToken = await verifyAuth(req);
      const requireAuth = process.env.REQUIRE_AUTH === 'true';
      if (requireAuth && !decodedToken) {
        res.status(401).json({
          error: 'Unauthorized',
          message: 'Valid Firebase Authentication token is required to access Mandi Rates.',
        });
        return;
      }

      // 2. Extract Query Parameters
      const state = (req.query.state as string) || '';
      const district = (req.query.district as string) || '';
      const commodity = (req.query.commodity as string) || '';
      const limit = parseInt((req.query.limit as string) || '50', 10);
      const forceRefresh = req.query.forceRefresh === 'true';

      const cacheKey = `rates_${state}_${district}_${commodity}_${limit}`.toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const cacheRef = db.collection('mandi_rates_cache').doc(cacheKey);

      // 3. Check Firestore Cache
      if (!forceRefresh) {
        const cacheDoc = await cacheRef.get();
        if (cacheDoc.exists) {
          const cacheData = cacheDoc.data();
          const cachedTime = cacheData?.fetched_at ? new Date(cacheData.fetched_at).getTime() : 0;
          const now = Date.now();
          const ageInMinutes = (now - cachedTime) / (1000 * 60);

          if (ageInMinutes < CACHE_TTL_MINUTES && cacheData?.records) {
            functions.logger.info(`Serving Mandi rates from Firestore cache (Age: ${Math.round(ageInMinutes)} mins)`);
            res.status(200).json({
              status: 'success',
              source: 'Firestore Cache (Agmarknet Proxy)',
              cached_at: cacheData.fetched_at,
              is_cached: true,
              total: cacheData.records.length,
              records: cacheData.records,
            });
            return;
          }
        }
      }

      // 4. Retrieve Agmarknet API Key from environment or Firebase Config
      const apiKey = process.env.AGMARKNET_API_KEY || functions.config().agmarknet?.key;

      if (!apiKey) {
        functions.logger.error('AGMARKNET_API_KEY is not configured on the backend cloud function.');
        res.status(503).json({
          error: 'Live market data connection not configured',
          message: 'AGMARKNET_API_KEY is missing in Firebase Cloud Function environment variables.',
          verification_status: 'UNCONFIGURED_BACKEND',
          instructions: 'Set AGMARKNET_API_KEY in backend environment secrets.',
        });
        return;
      }

      // 5. Build Upstream API URL & Query Parameters
      const apiParams: Record<string, string | number> = {
        'api-key': apiKey,
        format: 'json',
        limit: limit,
      };

      if (state) apiParams['filters[state]'] = state;
      if (district) apiParams['filters[district]'] = district;
      if (commodity) apiParams['filters[commodity]'] = commodity;

      functions.logger.info(`Fetching live records from Agmarknet API for state=${state}, district=${district}, commodity=${commodity}`);
      const apiResponse = await axios.get(BASE_URL, { params: apiParams, timeout: 10000 });

      if (!apiResponse.data || !apiResponse.data.records) {
        throw new Error('Invalid response structure from Agmarknet API.');
      }

      const rawRecords = apiResponse.data.records;
      const fetchTimestamp = new Date().toISOString();

      // 6. Normalize Records
      const normalizedRecords: MandiRateRecord[] = rawRecords.map((item: any) => ({
        commodity: item.commodity || 'N/A',
        variety: item.variety || 'Standard',
        state: item.state || 'N/A',
        district: item.district || 'N/A',
        market: item.market || 'N/A',
        min_price: item.min_price || 'Not available',
        max_price: item.max_price || 'Not available',
        modal_price: item.modal_price || 'Not available',
        arrival_quantity: item.arrival_qtl || item.arrival_quantity || 'N/A',
        unit: '₹/quintal',
        market_date: item.arrival_date || item.market_date || new Date().toISOString().split('T')[0],
        fetched_at: fetchTimestamp,
        source: 'AGMARKNET / Government Open Data Platform (data.gov.in)',
        verification_status: '🟢 Verified Government Source',
        is_cached: false,
      }));

      // 7. Update Firestore Cache asynchronously
      await cacheRef.set({
        fetched_at: fetchTimestamp,
        params: { state, district, commodity, limit },
        records: normalizedRecords,
      });

      // 8. Return Fresh Response
      res.status(200).json({
        status: 'success',
        source: 'AGMARKNET / data.gov.in',
        fetched_at: fetchTimestamp,
        is_cached: false,
        total: normalizedRecords.length,
        records: normalizedRecords,
      });
    } catch (error: any) {
      functions.logger.error('Error fetching from Agmarknet API:', error.message || error);

      // Attempt stale cache fallback if available
      try {
        const state = (req.query.state as string) || '';
        const district = (req.query.district as string) || '';
        const commodity = (req.query.commodity as string) || '';
        const limit = parseInt((req.query.limit as string) || '50', 10);
        const cacheKey = `rates_${state}_${district}_${commodity}_${limit}`.toLowerCase().replace(/[^a-z0-9_]/g, '_');
        const cacheDoc = await db.collection('mandi_rates_cache').doc(cacheKey).get();

        if (cacheDoc.exists && cacheDoc.data()?.records) {
          const cacheData = cacheDoc.data();
          res.status(200).json({
            status: 'warning',
            message: 'Unable to reach upstream Agmarknet server; returning cached data.',
            cached_at: cacheData?.fetched_at,
            is_cached: true,
            total: cacheData?.records.length,
            records: cacheData?.records,
          });
          return;
        }
      } catch (cacheErr) {
        functions.logger.error('Failed to retrieve stale cache fallback:', cacheErr);
      }

      res.status(502).json({
        error: 'Unable to fetch the latest market data',
        details: error.message || 'Upstream API error',
        verification_status: 'FETCH_ERROR',
      });
    }
  });
});
