package com.github.thfmart.dogbreedclassifier.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.thfmart.dogbreedclassifier.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.github.thfmart.dogbreedclassifier.R.string.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this, MainViewModelFactory(assets,applicationContext)).get(MainViewModel::class.java)
        val view = binding.root
        setContentView(view)
        initViews()
    }

    private fun initViews() {
        binding.attachFile.setOnClickListener {
            onClickCaptureFile()
        }
        binding.openCamera.setOnClickListener{
            onClickCaptureCamera()
        }
        viewModel.predictionResult.observe(this, Observer { breed ->
            updateDogText(breed)
        })
        binding.instructions.text =  getString(viewModel.getInitialMessage())
    }

    private fun onClickCaptureCamera() {
        openCamera()
    }

    private fun onClickCaptureFile() {
        getImageFromAlbum()
    }

    private fun getImageFromAlbum() {
        try {
            val i = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, IMAGE_PICK_CODE)
        } catch (exp: Exception) {

        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
        startActivityForResult(cameraIntent, CAMERA_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
            bitmap?.let{
                viewModel.predictBreed(bitmap)
                binding.imageView.visibility = View.VISIBLE
                updateImage(bitmap)}
                    ?: run {
                        val errorFileMessage = Snackbar.make(binding.root, "Unable to read file format",
                                Snackbar.LENGTH_LONG)
                        errorFileMessage.show() }
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_PICK_CODE){
            val bitmap: Bitmap? = data!!.extras?.get("data") as Bitmap?
            bitmap?.let{
                viewModel.predictBreed(bitmap)
                binding.imageView.visibility = View.VISIBLE   
                updateImage(bitmap)}
                    ?: run {
                        val errorFileMessage = Snackbar.make(binding.root, "Unable to read file format",
                                Snackbar.LENGTH_LONG)
                        errorFileMessage.show() }
        }
    }

    private fun updateAnswerWaitMessage(){
        binding.textAnswer.text = loading_message.toString()
    }

    private fun updateImage(bitmap: Bitmap){
        binding.imageView.setImageBitmap(bitmap)
    }

    private fun updateDogText(dogName: String){
        val randomMessage = getString(viewModel.getRandomMessage())
        binding.textAnswer.visibility = View.VISIBLE
        binding.textAnswer.text = randomMessage.plus(" ").plus(dogName)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val CAMERA_PICK_CODE = 1001
    }
}