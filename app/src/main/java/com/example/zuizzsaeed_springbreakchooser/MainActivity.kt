package com.example.zuizzsaeed_springbreakchooser

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.view.ContextThemeWrapper
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.media.MediaPlayer


class MainActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var languageSpinner: Spinner
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var editText: EditText
    private var selectedLanguage: String? = null
    private var lastShakeTime: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views in the layout
        languageSpinner = findViewById(R.id.languageSpinner)
        editText = findViewById(R.id.editText)

        // Example list of languages
        val languages = arrayOf("English", "Spanish", "French", "Chinese")

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        languageSpinner.adapter = adapter

        // Set up a listener for Spinner selection
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Store the selected language
                selectedLanguage = languages[position]

                // Prompt the user to speak a phrase in the selected language
                startSpeechRecognition()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected (optional)
            }
        }

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
    }

    private fun startSpeechRecognition() {
        selectedLanguage?.let { language ->
            val locale = when (language) {
                "English" -> Locale.ENGLISH
                "Spanish" -> Locale("es", "ES")
                "French" -> Locale.FRENCH
                "Chinese" -> Locale.CHINESE
                else -> Locale.getDefault()
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say a phrase in ${locale.displayLanguage}")

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: ActivityNotFoundException) {
                // Show error message or handle the case where speech recognition is not supported
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            editText.setText(result?.get(0) ?: "")
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) {}
    override fun onResults(results: Bundle?) {}

    override fun onResume() {
        super.onResume()
        // Register the sensor listener onResume
        shakeDetector.start()
    }

    override fun onPause() {
        // Unregister the sensor listener onPause
        shakeDetector.stop()
        super.onPause()
    }

    private val shakeDetector = ShakeDetector(this)
    private val mapLocationLanguages = mapOf(
        "English" to listOf("geo:40.7128,-74.0060", "geo:51.5074,-0.1278"), // New York, London
        "Spanish" to listOf("geo:21.1619,-86.8515", "geo:40.4168,-3.7038"), // Cancun, Madrid
        "French" to listOf("geo:48.8566,2.3522", "geo:45.5017,-73.5673"), // Paris, Montreal
        "Chinese" to listOf("geo:39.9042,116.4074", "geo:25.0330,121.5654") // Beijing, Taiwan
    )
    private inner class ShakeDetector(private val context: Context) : SensorEventListener {

        private val threshold = 500 // Adjust this value as needed
        private val timeThreshold = 500 // Adjust this value as needed
        private var lastShake: Long = 0
        private var axisLastX: Float = 0.0f
        private var axisLastY: Float = 0.0f
        private var axisLastZ: Float = 0.0f

        override fun onSensorChanged(event: SensorEvent) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastShake) > 100) {
                val diffTime = (currentTime - lastShake).toFloat()
                lastShake = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = Math.abs(x + y + z - axisLastX - axisLastY - axisLastZ) / diffTime * 10000

                if (speed > threshold) {
                    selectedLanguage?.let { language ->
                        mapLocationLanguages[language]?.random()?.also { uri ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            startActivity(intent)
                            playGreeting(language)
                        }
                    }
                }

                axisLastX = x
                axisLastY = y
                axisLastZ = z
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        fun start() {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        fun stop() {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.unregisterListener(this)
        }


        private fun launchGoogleMaps() {
            selectedLanguage?.let { language ->
                mapLocationLanguages[language]?.random()?.also { uri ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(intent)
                    playGreeting(language)
                }
            }
        }

        private fun playGreeting(languageCode: String?) {
            val resourceId = when (languageCode) {
                "English" -> R.raw.english_hello
                "Spanish" -> R.raw.spanish_hello
                "French" -> R.raw.french_hello
                "Chinese" -> R.raw.chinese_hello
                else -> null
            }

            resourceId?.let {
                val mediaPlayer = MediaPlayer.create(context, it)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.release()
                }
            }
        }



    }

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
    }


}