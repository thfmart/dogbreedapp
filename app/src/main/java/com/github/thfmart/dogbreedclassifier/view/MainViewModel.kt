package com.github.thfmart.dogbreedclassifier.view

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.thfmart.dogbreedclassifier.R.string.*
import com.github.thfmart.dogbreedclassifier.classifier.Classifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainViewModel(private val assets: AssetManager, private val applicationContext:Context) : ViewModel() {

    private lateinit var classifier: Classifier
    val predictionResult = MutableLiveData<String>()

    init {
        initClassifier()
    }

    private fun initClassifier() {
        classifier = Classifier(assets, applicationContext)
    }

    fun getRandomMessage(): Int {
        val phrases = arrayOf(
            phrase_one, phrase_two, phrase_three, phrase_four,
            phrase_five, phrase_six, phrase_seven
        )
        val randomIndex = Random.nextInt(0,7)
        return phrases[randomIndex]
    }

    fun getInitialMessage(): Int {
        return text_view_first_message
    }

    fun predictBreed(bitmap: Bitmap) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val result = classifier.recognizeImage(bitmap)
                withContext(Dispatchers.Main){
                    predictionResult.value = result
                }
            }
        }
    }
}