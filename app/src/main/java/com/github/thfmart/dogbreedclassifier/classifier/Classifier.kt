package com.github.thfmart.dogbreedclassifier.classifier

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.github.thfmart.dogbreedclassifier.ml.Effb1Quantized
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.model.Model.Options.Builder
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Classifier(assets: AssetManager, applicationContext: Context) {
    private val inputSize = 240
    private val labelPath = "labels.txt"
    private val imageMean = 0
    private val imageStd = 255.0f
    private val imageChannels = 3
    private val model: Effb1Quantized
    private var labelList: List<String>

    init {
        val compatList = CompatibilityList()
        val options = if(compatList.isDelegateSupportedOnThisDevice) {
            // if the device has a supported GPU, add the GPU delegate
            Builder().setDevice(Model.Device.GPU).build()
        } else {
            // if the GPU is not supported, run on 4 threads
            Builder().setNumThreads(4).build()
        }
        //options.setNumThreads(5)
        //options.setUseNNAPI(true)
        model = Effb1Quantized.newInstance(applicationContext,options)
        labelList = loadLabelList(assets, labelPath)

    }

    @Suppress("SameParameterValue")
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    fun recognizeImage(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBufferEfficientNet(scaledBitmap)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, inputSize, inputSize, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val index = outputFeature0.floatArray.indexOfLast { it== outputFeature0.floatArray.maxOrNull() }
        return labelList[index]
    }

    private fun convertBitmapToByteBufferEfficientNet(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * imageChannels)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val input = intValues[pixel++]

                byteBuffer.putFloat((input.shr(16) and 0xFF).toFloat())
                byteBuffer.putFloat((input.shr(8) and 0xFF).toFloat())
                byteBuffer.putFloat(((input and 0xFF).toFloat()))
            }
        }
        return byteBuffer
    }
}