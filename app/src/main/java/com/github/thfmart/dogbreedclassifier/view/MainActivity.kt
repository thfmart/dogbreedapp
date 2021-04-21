package com.github.thfmart.dogbreedclassifier.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.thfmart.dogbreedclassifier.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


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
        binding.captureFile.setOnClickListener {
            onClickCaptureFile()
        }
        binding.captureCamera.setOnClickListener{
            onClickCaptureCamera()
        }
        viewModel.predictionResult.observe(this, Observer { breed ->
            updateDogText(breed)
        })
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
            binding.textView.text = getString(viewModel.calculatingMessage())
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
            bitmap?.let{
                viewModel.predictBreed(bitmap)
                updateImage(bitmap)}
                    ?: run {
                        val errorFileMessage = Snackbar.make(binding.root, "Unable to read file format",
                                Snackbar.LENGTH_LONG)
                        errorFileMessage.show() }
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_PICK_CODE){
            binding.textView.text = getString(viewModel.calculatingMessage())
            val bitmap: Bitmap? = data!!.extras?.get("data") as Bitmap?
            bitmap?.let{
                viewModel.predictBreed(bitmap)
                updateImage(bitmap)}
                    ?: run {
                        val errorFileMessage = Snackbar.make(binding.root, "Unable to read file format",
                                Snackbar.LENGTH_LONG)
                        errorFileMessage.show() }
        }
    }

    private fun updateImage(bitmap: Bitmap){
        binding.img1.setImageBitmap(bitmap)
    }

    private fun updateDogText(dogName: String){
        val randomMessage = getString(viewModel.getRandomMessage())
        binding.textView.text = randomMessage.plus(" ").plus(dogName)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val CAMERA_PICK_CODE = 1001
    }
}