package com.example.zuizzsaeed_springbreakchooser

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the Spinner in the layout
        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)

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
                // Retrieve the selected language
                val selectedLanguage = languages[position]

                // Perform actions based on the selected language
                // For example, you could update UI elements or trigger other functionality
                // Here you might prompt the user to speak a phrase in the selected language
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected (optional)
            }
        }
    }
}
