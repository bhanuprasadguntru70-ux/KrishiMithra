package com.example.ui.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.datastore.LanguagePreferencesDataStore

/**
 * Scalable Language System for Krishi Mithra.
 * - Main navigation & technical UI remain in English by default.
 * - Farmer-facing portal content switches between English, Telugu, and other Indian languages.
 */
object FarmerTranslations {

    private val translations = mapOf(
        // MANDI / MARKET
        "mandi_title" to mapOf(
            "en" to "Live Mandi Rates",
            "te" to "లైవ్ మార్కెట్ ధరలు",
            "hi" to "लाइव मंडी भाव",
            "ta" to "நேரடி சந்தை விலைகள்",
            "kn" to "ನೇರ ಮಾರುಕಟ್ಟೆ ದರಗಳು"
        ),
        "crop_name" to mapOf(
            "en" to "Crop Name",
            "te" to "పంట పేరు",
            "hi" to "फसल का नाम",
            "ta" to "பயிர் பெயர்",
            "kn" to "ಬೆಳೆಯ ಹೆಸರು"
        ),
        "market" to mapOf(
            "en" to "Market / Mandi",
            "te" to "మార్కెట్ / మండి",
            "hi" to "मंडी",
            "ta" to "சந்தை",
            "kn" to "ಮಾರುಕಟ್ಟೆ"
        ),
        "min_price" to mapOf(
            "en" to "Minimum Price",
            "te" to "కనిష్ట ధర",
            "hi" to "न्यूनतम मूल्य",
            "ta" to "குறைந்தபட்ச விலை",
            "kn" to "ಕನಿಷ್ಠ ಬೆಲೆ"
        ),
        "max_price" to mapOf(
            "en" to "Maximum Price",
            "te" to "గరిష్ట ధర",
            "hi" to "अधिकतम मूल्य",
            "ta" to "அதிகபட்ச விலை",
            "kn" to "ಗರಿಷ್ಠ ಬೆಲೆ"
        ),
        "modal_price" to mapOf(
            "en" to "Modal Price",
            "te" to "మోడల్ ధర",
            "hi" to "औसत (मॉडल) भाव",
            "ta" to "சராசரி விலை",
            "kn" to "ಮಾದರಿ ಬೆಲೆ"
        ),
        "arrival_qty" to mapOf(
            "en" to "Arrival Quantity",
            "te" to "వచ్చిన సరుకు పరిమాణం",
            "hi" to "आवक मात्रा",
            "ta" to "வந்த அளவு",
            "kn" to "ಆಗಮಿಸಿದ ಪ್ರಮಾಣ"
        ),
        "last_updated" to mapOf(
            "en" to "Last Updated Time",
            "te" to "చివరిగా నవీకరించిన సమయం",
            "hi" to "अंतिम अपडेट समय",
            "ta" to "கடைசியாக புதுப்பிக்கப்பட்ட நேரம்",
            "kn" to "ಕೊನೆಯದಾಗಿ ನವೀಕರಿಸಿದ ಸಮಯ"
        ),
        "verified_data" to mapOf(
            "en" to "Verified Market Data",
            "te" to "ధృవీకరించబడిన మార్కెట్ సమాచారం",
            "hi" to "सत्यापित मंडी डेटा",
            "ta" to "சரிபார்க்கப்பட்ட சந்தைத் தரவு",
            "kn" to "ಪರಿಶೀಲಿಸಿದ ಮಾರುಕಟ್ಟೆ ಮಾಹಿತಿ"
        ),
        "user_reported_data" to mapOf(
            "en" to "User Reported Data",
            "te" to "రైతులు తెలిపిన సమాచారం",
            "hi" to "उपयोगकर्ता रिपोर्ट डेटा",
            "ta" to "பயனர் தெரிவித்த தரவு",
            "kn" to "ಬಳಕೆದಾರರು ವರದಿ ಮಾಡಿದ ಮಾಹಿತಿ"
        ),
        "unverified_data" to mapOf(
            "en" to "Unverified Data",
            "te" to "ధృవీకరించబడని సమాచారం",
            "hi" to "असत्यापित डेटा",
            "ta" to "சரிபார்க்கப்படாத தரவு",
            "kn" to "ಪರಿಶೀಲಿಸದ ಮಾಹಿತಿ"
        ),
        "data_unavailable" to mapOf(
            "en" to "Live market data temporarily unavailable.",
            "te" to "లైవ్ మార్కెట్ సమాచారం ప్రస్తుతం అందుబాటులో లేదు.",
            "hi" to "लाइव मंडी डेटा फिलहाल उपलब्ध नहीं है।",
            "ta" to "நேரடி சந்தைத் தரவு தற்காலிகமாக கிடைக்கவில்லை.",
            "kn" to "ನೇರ ಮಾರುಕಟ್ಟೆ ಮಾಹಿತಿ ಪ್ರಸ್ತುತ ಲಭ್ಯವಿಲ್ಲ."
        ),
        "best_verified_price" to mapOf(
            "en" to "BEST AVAILABLE VERIFIED PRICE",
            "te" to "అత్యుత్తమ ధృవీకరించబడిన మార్కెట్ ధర",
            "hi" to "सर्वश्रेष्ठ उपलब्ध सत्यापित भाव",
            "ta" to "சிறந்த கிடைக்கூடிய சரிபார்க்கப்பட்ட விலை",
            "kn" to "ಅತ್ಯುತ್ತಮ ಪರಿಶೀಲಿಸಿದ ಮಾರುಕಟ್ಟೆ ದರ"
        ),
        "price_disclaimer" to mapOf(
            "en" to "Market prices can change during the day. The displayed price is the latest available verified market reference. The final buying/selling price may depend on quality, grade, quantity, demand, transport, and negotiation.",
            "te" to "మార్కెట్ ధరలు రోజంతా మారవచ్చు. చూపబడిన ధర సమీప మార్కెట్ సమాచారం మాత్రమే. నాణ్యత, రకం, పరిమాణం, రవాణా మరియు చర్చల ఆధారంగా అంతిమ అమ్మకం ధర నిర్ణయించబడుతుంది.",
            "hi" to "मंडी के भाव दिन के दौरान बदल सकते हैं। दिखाया गया भाव निकटतम सत्यापित मंडी संदर्भ है। अंतिम मूल्य गुणवत्ता, मात्रा और बातचीत पर निर्भर करता है।",
            "ta" to "சந்தை விலைகள் மாறக்கூடும். காட்டப்படும் விலை சமீபத்திய சரிபார்க்கப்பட்ட சந்தை தகவல் மட்டுமே. இறுதி விலை தரம், அளவு மற்றும் பேச்சுவார்த்தையைப் பொறுத்தது.",
            "kn" to "ಮಾರುಕಟ್ಟೆ ದರಗಳು ದಿನದಲ್ಲಿ ಬದಲಾಗಬಹುದು. ತೋರಿಸಿದ ಬೆಲೆ ಪರಿಶೀಲಿಸಿದ ಮಾಹಿತಿ ಮಾತ್ರ. ಅಂತಿಮ ಬೆಲೆ ಗುಣಮಟ್ಟ ಮತ್ತು ಪ್ರಮಾಣವನ್ನು ಅವಲಂಬಿಸಿರುತ್ತದೆ."
        ),

        // CROP SELLING & BUYING
        "sell_crop" to mapOf(
            "en" to "Sell My Crop",
            "te" to "నా పంటను అమ్మండి",
            "hi" to "मेरी फसल बेचें",
            "ta" to "என் பயிரை விற்கவும்",
            "kn" to "ನನ್ನ ಬೆಳೆಯನ್ನು ಮಾರಿ"
        ),
        "buy_crop" to mapOf(
            "en" to "Buy Crops",
            "te" to "పంటలు కొనండి",
            "hi" to "फसलें खरीदें",
            "ta" to "பயிர்களை வாங்கவும்",
            "kn" to "ಬೆಳೆಗಳನ್ನು ಕೊಳ್ಳಿ"
        ),
        "post_crop_for_sale" to mapOf(
            "en" to "Post Crop For Sale",
            "te" to "అమ్మకానికి పంట పెట్టండి",
            "hi" to "बिक्री के लिए फसल पोस्ट करें",
            "ta" to "விற்பனைக்கு பயிர் பதிவிடவும்",
            "kn" to "ಮಾರಾಟಕ್ಕೆ ಬೆಳೆ ಸೇರಿಸಿ"
        ),
        "verified_buyer" to mapOf(
            "en" to "Verified Buyer",
            "te" to "ధృవీకరించబడిన కొనుగోలుదారు",
            "hi" to "सत्यापित खरीदार",
            "ta" to "சரிபார்க்கப்பட்ட வாங்குபவர்",
            "kn" to "ಪರಿಶೀಲಿಸಿದ ಖರೀದಿದಾರ"
        ),
        "expected_price" to mapOf(
            "en" to "Expected Price",
            "te" to "ఆశిస్తున్న ధర",
            "hi" to "अपेक्षित मूल्य",
            "ta" to "எதிர்பார்க்கும் விலை",
            "kn" to "ನಿರೀಕ್ಷಿತ ಬೆಲೆ"
        ),
        "harvest_date" to mapOf(
            "en" to "Harvest Date",
            "te" to "కోత తేదీ",
            "hi" to "कटाई की तारीख",
            "ta" to "அறுவடை தேதி",
            "kn" to "ಕೊಯ್ಲು ದಿನಾಂಕ"
        ),
        "quantity" to mapOf(
            "en" to "Quantity",
            "te" to "పరిమాణం",
            "hi" to "मात्रा",
            "ta" to "அளவு",
            "kn" to "ಪ್ರಮಾಣ"
        ),
        "quality_grade" to mapOf(
            "en" to "Quality / Grade",
            "te" to "నాణ్యత / గ్రేడ్",
            "hi" to "गुणवत्ता / ग्रेड",
            "ta" to "தரம்",
            "kn" to "ಗುಣಮಟ್ಟ / ಗ್ರೇಡ್"
        ),

        // WEATHER
        "weather_forecast" to mapOf(
            "en" to "Weather Forecast",
            "te" to "వాతావరణ సమాచారం",
            "hi" to "मौसम का पूर्वानुमान",
            "ta" to "வானிலை முன்னறிவிப்பு",
            "kn" to "ಹವಾಮಾನ ಮುನ್ಸೂಚನೆ"
        ),
        "rain_probability" to mapOf(
            "en" to "Rain Probability",
            "te" to "వర్షం పడే అవకాశం",
            "hi" to "बारिश की संभावना",
            "ta" to "மழை வாய்ப்பு",
            "kn" to "ಮಳೆ ಸಾಧ್ಯತೆ"
        ),
        "humidity" to mapOf(
            "en" to "Humidity",
            "te" to "తేమ శాతం",
            "hi" to "नमी",
            "ta" to "ஈரப்பதம்",
            "kn" to "ತೇವಾಂಶ"
        ),
        "wind_speed" to mapOf(
            "en" to "Wind Speed",
            "te" to "గాలి వేగం",
            "hi" to "हवा की गति",
            "ta" to "காற்றின் வேகம்",
            "kn" to "ಗಾಳಿಯ ವೇಗ"
        ),

        // DISEASE & AI
        "disease_detection" to mapOf(
            "en" to "Crop Disease Detection",
            "te" to "పంట తెగుళ్ళ గుర్తింపు",
            "hi" to "फसल बीमारी पहचान",
            "ta" to "பயிர் நோய் கண்டறிதல்",
            "kn" to "ಬೆಳೆ ರೋಗ ಪತ್ತೆ"
        ),
        "symptoms" to mapOf(
            "en" to "Symptoms",
            "te" to "తెగులు లక్షణాలు",
            "hi" to "लक्षण",
            "ta" to "அறிகுறிகள்",
            "kn" to "ಲಕ್ಷಣಗಳು"
        ),
        "treatment" to mapOf(
            "en" to "Recommended Treatment",
            "te" to "నివారణ చర్యలు",
            "hi" to "अनुशंसित उपचार",
            "ta" to "பரிந்துரைக்கப்பட்ட சிகிச்சை",
            "kn" to "ಶಿಫಾರಸು ಮಾಡಿದ ಚಿಕಿತ್ಸೆ"
        ),
        "organic_solution" to mapOf(
            "en" to "Organic Solution",
            "te" to "సేంద్రీయ నివారణ చర్యలు",
            "hi" to "जैविक समाधान",
            "ta" to "இயற்கை தீர்வு",
            "kn" to "ಸಾವಯವ ಪರಿಹಾರ"
        ),
        "chemical_solution" to mapOf(
            "en" to "Chemical Solution",
            "te" to "రసాయన నివారణ చర్యలు",
            "hi" to "रासायनिक समाधान",
            "ta" to "இரசாயன தீர்வு",
            "kn" to "ರಾಸಾಯನಿಕ ಪರಿಹಾರ"
        ),

        // PRICE ALERTS
        "price_alerts" to mapOf(
            "en" to "My Crop Price Alerts",
            "te" to "నా పంట ధర హెచ్చరికలు",
            "hi" to "फसल भाव अलर्ट",
            "ta" to "விலை எச்சரிக்கைகள்",
            "kn" to "ಬೆಲೆ ಎಚ್ಚರಿಕೆಗಳು"
        ),
        "target_price" to mapOf(
            "en" to "Target Price",
            "te" to "లక్ష్య ధర (టార్గెట్ ప్రైస్)",
            "hi" to "लक्ष्य मूल्य",
            "ta" to "இலக்கு விலை",
            "kn" to "ಗುರಿ ಬೆಲೆ"
        ),

        // FARMER STORIES
        "farmer_stories" to mapOf(
            "en" to "Women Farmer Stories",
            "te" to "మా మహిళా రైతుల స్ఫూర్తి కథలు",
            "hi" to "महिला किसान कहानियां",
            "ta" to "பெண் விவசாயி கதைகள்",
            "kn" to "ಮಹಿಳಾ ರೈತರ ಕಥೆಗಳು"
        ),
        "inspiring_story" to mapOf(
            "en" to "Inspiring Farmer Story",
            "te" to "స్ఫూర్తిదాయక రైతు కథనం",
            "hi" to "प्रेरणादायक कहानी",
            "ta" to "ஊக்கமளிக்கும் கதை",
            "kn" to "ಸ್ಪೂರ್ತಿದಾಯಕ ಕಥೆ"
        )
    )

    fun get(key: String, languageCode: String): String {
        val langMap = translations[key] ?: return key
        return langMap[languageCode] ?: langMap["en"] ?: key
    }
}

/**
 * Reusable Portal Language Selector Bar (English | తెలుగు).
 * Displayed inside each farmer portal so the farmer can switch content language immediately.
 */
@Composable
fun PortalLanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("portal_language_selector"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // English Option
            val isEnSelected = currentLanguage == LanguagePreferencesDataStore.LANGUAGE_ENGLISH
            val enBgColor by animateColorAsState(
                targetValue = if (isEnSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "en_bg"
            )
            val enTextColor by animateColorAsState(
                targetValue = if (isEnSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "en_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(enBgColor)
                    .clickable { onLanguageSelected(LanguagePreferencesDataStore.LANGUAGE_ENGLISH) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "English",
                    fontSize = 12.sp,
                    fontWeight = if (isEnSelected) FontWeight.Bold else FontWeight.Medium,
                    color = enTextColor
                )
            }

            Text(
                text = "|",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            // Telugu Option
            val isTeSelected = currentLanguage == LanguagePreferencesDataStore.LANGUAGE_TELUGU
            val teBgColor by animateColorAsState(
                targetValue = if (isTeSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "te_bg"
            )
            val teTextColor by animateColorAsState(
                targetValue = if (isTeSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "te_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(teBgColor)
                    .clickable { onLanguageSelected(LanguagePreferencesDataStore.LANGUAGE_TELUGU) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "తెలుగు",
                    fontSize = 12.sp,
                    fontWeight = if (isTeSelected) FontWeight.Bold else FontWeight.Medium,
                    color = teTextColor
                )
            }

            // More Languages Indicator for future expansion (Hindi, Tamil, Kannada)
            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = "▾",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    LanguagePreferencesDataStore.SUPPORTED_LANGUAGES.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${lang.displayName} (${lang.nativeName})",
                                    fontSize = 13.sp,
                                    fontWeight = if (currentLanguage == lang.code) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onLanguageSelected(lang.code)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
