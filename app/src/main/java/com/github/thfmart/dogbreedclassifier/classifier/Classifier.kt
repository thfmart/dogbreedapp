package com.github.thfmart.dogbreedclassifier.classifier


import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.github.thfmart.dogbreedclassifier.ml.ConvertedModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.model.Model.Options.Builder
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter as Interpreter


class Classifier(private val assets: AssetManager, private val applicationContext: Context) {
    private val inputSize = 299
    private val modelPath = "converted_model.tflite"
    private val labelPath = "labels.txt"
    private val imageMean = 0
    private val imageStd = 255.0f
    private val imageChannels = 3
    private val model = ConvertedModel.newInstance(applicationContext)
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

        labelList = loadLabelList(assets, labelPath)
    }


    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    fun recognizeImage(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val outputMap = createHashMapOutput()
        val bufferArray = Array(1){ byteBuffer}

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 299, 299, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        return getSortedResult(outputMap[0] as Array<FloatArray>, labelList)
    }

    private fun createHashMapOutput(): HashMap<Int,Any>{
        val outputs:HashMap<Int, Any> = HashMap<Int,Any>()
        val out = Array(1) { FloatArray(labelList.size)}
        outputs.put(0,out)
        return outputs
    }


    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * imageChannels)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val input = intValues[pixel++]

                byteBuffer.putFloat((((input.shr(16)  and 0xFF) - imageMean) / imageStd))
                byteBuffer.putFloat((((input.shr(8) and 0xFF) - imageMean) / imageStd))
                byteBuffer.putFloat((((input and 0xFF) - imageMean) / imageStd))
            }
        }
        return byteBuffer
    }

    /**
     * dfdadf
     * dfdfd
     */
    private fun getSortedResult(labelProbArray: Array<FloatArray>, classList:List<String>): String {
        val indexOfDog = labelProbArray[0].max()?.let { labelProbArray[0].indexOf(it) }
        return classList[indexOfDog!!]
    }
}