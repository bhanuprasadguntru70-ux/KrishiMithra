package com.example.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Production-ready TextToSpeech Engine configured for Telugu and Indian regional languages.
 */
class TeluguTtsEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isInitialized by mutableStateOf(false)
    var isSpeaking by mutableStateOf(false)
    var currentLanguage by mutableStateOf("Telugu")

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.apply {
                val teluguLocale = Locale("te", "IN")
                val langResult = setLanguage(teluguLocale)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to general te or English if specific locale is missing
                    setLanguage(Locale("te"))
                }
                setPitch(1.0f)
                setSpeechRate(0.92f) // Slightly relaxed pace for natural Telugu cadence
                
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        this@TeluguTtsEngine.isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        this@TeluguTtsEngine.isSpeaking = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        this@TeluguTtsEngine.isSpeaking = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        this@TeluguTtsEngine.isSpeaking = false
                    }
                })
            }
            isInitialized = true
        } else {
            isInitialized = false
        }
    }

    fun speak(text: String, languageName: String = "Telugu", onCompleted: () -> Unit = {}) {
        if (!isInitialized || tts == null) return

        currentLanguage = languageName
        val cleanText = cleanMarkdownForSpeech(text)
        if (cleanText.isBlank()) return

        val targetLocale = when (languageName.lowercase(Locale.ROOT)) {
            "telugu", "తెలుగు" -> Locale("te", "IN")
            "hindi", "హిందీ" -> Locale("hi", "IN")
            "tamil", "తమిళ్" -> Locale("ta", "IN")
            "kannada", "కన్నడ" -> Locale("kn", "IN")
            "malayalam", "మలయాళం" -> Locale("ml", "IN")
            "marathi", "మరాఠీ" -> Locale("mr", "IN")
            "bengali", "బెంగాలీ" -> Locale("bn", "IN")
            "gujarati", "గుజరాతీ" -> Locale("gu", "IN")
            "punjabi", "పంజాబీ" -> Locale("pa", "IN")
            "urdu", "ఉర్దూ" -> Locale("ur", "IN")
            else -> Locale.US
        }

        try {
            val res = tts?.setLanguage(targetLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }
        } catch (e: Exception) {
            tts?.language = Locale.US
        }

        val utteranceId = "AgriAiTts_${System.currentTimeMillis()}"
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        isSpeaking = true
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignore
        }
        isSpeaking = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // Ignore
        }
        tts = null
        isInitialized = false
        isSpeaking = false
    }

    private fun cleanMarkdownForSpeech(raw: String): String {
        return raw
            .replace(Regex("[*#_`~]"), "") // Remove markdown syntax
            .replace(Regex("\\[(.*?)\\]\\(.*\\)"), "$1") // Links
            .replace(Regex("\\bhttps?://\\S+"), "") // URLs
            .replace(Regex("[\\r\\n]+"), " ") // Newlines to spaces
            .trim()
    }
}

/**
 * Production-ready Speech-to-Text Manager using Android SpeechRecognizer.
 */
class TeluguSpeechRecognizerManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit = {},
    private val onFinalResult: (String) -> Unit = {},
    private val onError: (String) -> Unit = {},
    private val onRmsChanged: (Float) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    var isListening by mutableStateOf(false)

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            setupListener()
        }
    }

    private fun setupListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                this@TeluguSpeechRecognizerManager.isListening = true
            }

            override fun onBeginningOfSpeech() {
                this@TeluguSpeechRecognizerManager.isListening = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                // RMS level usually ranges from 0 to ~12 dB
                val normalized = (rmsdB.coerceIn(0f, 10f))
                onRmsChanged(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                this@TeluguSpeechRecognizerManager.isListening = false
            }

            override fun onError(error: Int) {
                this@TeluguSpeechRecognizerManager.isListening = false
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "ఆడియో రికార్డింగ్ లోపం జరిగింది"
                    SpeechRecognizer.ERROR_CLIENT -> "క్లయింట్ లోపం"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "మైక్రోఫోన్ అనుమతి ఇవ్వబడలేదు"
                    SpeechRecognizer.ERROR_NETWORK -> "నెట్‌వర్క్ లోపం - ఇంటర్నెట్ సరిగ్గా ఉందో చూసుకోండి"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "నెట్‌వర్క్ సమయం ముగిసింది"
                    SpeechRecognizer.ERROR_NO_MATCH -> "సరిగ్గా వినిపించలేదు, మళ్ళీ మాట్లాడండి (No match)"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "వాయిస్ ప్రాసెస్ అవుతోంది..."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "మాట్లాడటం సమయం ముగిసింది"
                    else -> "వాయిస్ ఇన్పుట్ లోపం ($error)"
                }
                onError(msg)
            }

            override fun onResults(results: Bundle?) {
                this@TeluguSpeechRecognizerManager.isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull() ?: ""
                if (recognized.isNotBlank()) {
                    onFinalResult(recognized)
                } else {
                    onError("వాయిస్ వినిపించలేదు, దయచేసి మళ్ళీ మాట్లాడండి.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    onPartialResult(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening(languageName: String = "Telugu") {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("ఈ పరికరంలో వాయిస్ గుర్తింపు అందుబాటులో లేదు.")
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            setupListener()
        }

        val localeTag = when (languageName.lowercase(Locale.ROOT)) {
            "telugu", "తెలుగు" -> "te-IN"
            "hindi", "హిందీ" -> "hi-IN"
            "tamil", "తమిళ్" -> "ta-IN"
            "kannada", "కన్నడ" -> "kn-IN"
            "malayalam", "మలయాళం" -> "ml-IN"
            "marathi", "మరాఠీ" -> "mr-IN"
            "bengali", "బెంగాలీ" -> "bn-IN"
            "gujarati", "గుజరాతీ" -> "gu-IN"
            "punjabi", "పంజాబీ" -> "pa-IN"
            "urdu", "ఉర్దూ" -> "ur-IN"
            else -> "en-IN"
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            onError("వాయిస్ సర్వీస్ ప్రారంభించడంలో విఫలమైంది: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        isListening = false
    }

    fun destroy() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore
        }
        speechRecognizer = null
        isListening = false
    }

    companion object {
        fun createRecognizerIntent(context: Context, languageName: String = "Telugu"): Intent {
            val localeTag = when (languageName.lowercase(Locale.ROOT)) {
                "telugu", "తెలుగు" -> "te-IN"
                "hindi", "హిందీ" -> "hi-IN"
                "tamil", "తమిళ్" -> "ta-IN"
                "kannada", "కన్నడ" -> "kn-IN"
                "malayalam", "మలయాళం" -> "ml-IN"
                "marathi", "మరాఠీ" -> "mr-IN"
                "bengali", "బెంగాలీ" -> "bn-IN"
                "gujarati", "గుజరాతీ" -> "gu-IN"
                "punjabi", "పంజాబీ" -> "pa-IN"
                "urdu", "ఉర్దూ" -> "ur-IN"
                else -> "en-IN"
            }
            return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "మాట్లాడండి ($languageName)...")
            }
        }
    }
}

/**
 * Composite Voice Controller state class for Compose UI.
 */
class TeluguVoiceState(
    val ttsEngine: TeluguTtsEngine,
    val sttManager: TeluguSpeechRecognizerManager
) {
    var recognizedText by mutableStateOf("")
    var partialText by mutableStateOf("")
    var statusText by mutableStateOf("మాట్లాడటానికి మైక్రోఫోన్ నొక్కండి")
    var selectedLanguage by mutableStateOf("Telugu")
    var rmsLevel by mutableFloatStateOf(0f)

    fun startListening(onRecognized: (String) -> Unit = {}) {
        recognizedText = ""
        partialText = ""
        statusText = "వింటోంది ($selectedLanguage)... దయచేసి మాట్లాడండి"
        sttManager.startListening(selectedLanguage)
    }

    fun stopListening() {
        sttManager.stopListening()
        statusText = "వాయిస్ ఇన్పుట్ ఆగిపోయింది"
    }

    fun speak(text: String, language: String = selectedLanguage) {
        ttsEngine.speak(text, language)
    }

    fun stopSpeaking() {
        ttsEngine.stop()
    }
}

@Composable
fun rememberTeluguVoiceState(
    onFinalSpokenText: (String) -> Unit = {}
): TeluguVoiceState {
    val context = LocalContext.current.applicationContext

    val tts = remember { TeluguTtsEngine(context) }
    
    var tempState: TeluguVoiceState? by remember { mutableStateOf(null) }

    val stt = remember {
        TeluguSpeechRecognizerManager(
            context = context,
            onPartialResult = { partial ->
                tempState?.partialText = partial
            },
            onFinalResult = { final ->
                tempState?.recognizedText = final
                tempState?.partialText = ""
                tempState?.statusText = "గ్రహించబడింది: $final"
                onFinalSpokenText(final)
            },
            onError = { err ->
                tempState?.statusText = err
            },
            onRmsChanged = { rms ->
                tempState?.rmsLevel = rms
            }
        )
    }

    val state = remember(tts, stt) {
        TeluguVoiceState(tts, stt).also { tempState = it }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
            stt.destroy()
        }
    }

    return state
}
