package com.simba_studio.mywork.ultius

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import com.simba_studio.mywork.R
import com.simba_studio.mywork.act.EditAdsAct
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePicker {
    const val MAX_IMAGE_COUNT = 3
    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "Pix/Camera"
            ratio = Ratio.RATIO_AUTO

        }
        return options
    }

    fun getMultiImages(edAct: EditAdsAct, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectedImages(edAct, result.data)
                }
                PixEventCallback.Status.BACK_PRESSED -> TODO()
            }
        }
    }

    fun addImages(edAct: EditAdsAct, imageCounter: Int) {
        val f = edAct.chooseImageFrag
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    edAct.chooseImageFrag = f
                    openChooseImageFrag(edAct, f!!)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }
                PixEventCallback.Status.BACK_PRESSED -> TODO()
            }
        }
    }

    fun getSingleImage(edAct: EditAdsAct) {
        val f = edAct.chooseImageFrag
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    edAct.chooseImageFrag = f
                    openChooseImageFrag(edAct, f!!)
                    singleImage(edAct, result.data[0])
                }
                PixEventCallback.Status.BACK_PRESSED -> TODO()
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsAct, f: Fragment) {
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, f).commit()
    }

    private fun closePixFrag(edAct: EditAdsAct) {
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }
    private fun getMultiSelectedImages(edAct: EditAdsAct, uris: List<Uri>) {
        if (uris.size > 1 && edAct.chooseImageFrag == null) {
            edAct.openChooseImageFrag(uris as ArrayList<Uri>)
        }  else if (uris.size == 1 && edAct.chooseImageFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoad.visibility = View.VISIBLE
                val bitmapArray = ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap>
                edAct.binding.pBarLoad.visibility = View.GONE
                edAct.imageAdapter.update(bitmapArray)
                closePixFrag(edAct)
            }
        }
    }

    private fun singleImage(edAct: EditAdsAct, uri: Uri) {

        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)
    }
}

