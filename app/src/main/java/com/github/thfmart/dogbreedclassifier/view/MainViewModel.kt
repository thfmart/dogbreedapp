package com.github.thfmart.dogbreedclassifier.view

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.thfmart.dogbreedclassifier.R.*
import com.github.thfmart.dogbreedclassifier.classifier.Classifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val assets: AssetManager, private val applicationContext:Context) : ViewModel() {

    private lateinit var classifier: Classifier
    val predictionResult = MutableLiveData<String>()

    init {
        initClassifier()
    }

    private fun initClassifier() {
        classifier = Classifier(assets, applicationContext)
    }

    fun getRandomMessage(): String {
        val phrases = arrayOf(
            string.phrase_one, string.phrase_two, string.phrase_three, string.phrase_four,
            string.phrase_five, string.phrase_six, string.phrase_seven
        )
        return "$phrases[randomIndex]"
    }

    fun predictBreed(bitmap: Bitmap) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                predictionResult.value = classifier.recognizeImage(bitmap)
            }
        }
    }
}