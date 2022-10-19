package com.simba_studio.mywork.act

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.simba_studio.mywork.R
import com.simba_studio.mywork.adapters.ImageAdapter
import com.simba_studio.mywork.databinding.ActivityDescriptionBinding
import com.simba_studio.mywork.model.Ad
import com.simba_studio.mywork.ultius.ImageManager


@Suppress("DEPRECATION")
class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbTel.setOnClickListener {call()}
        binding.fbEmail.setOnClickListener {sendEmail()}
    }

    private fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }

    private fun getIntentFromMainAct(){
        ad = intent.getSerializableExtra("AD") as Ad
        if(ad != null)updateUI(ad!!)
    }

    private fun updateUI(ad: Ad){
        ImageManager.fillImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad) = with(binding){
        tvTitle.text = ad.title
        tvDescription.text = ad.description
        tvEmail.text = ad.email
        tvPrice.text = ad.price
        tvTel.text = ad.tel
        tvSector.text = ad.work
        tvCity.text = ad.profile
        tvIndex.text = ad.alternativeCommunication
        tvWithSend.text = isWithSend(ad.buisnessTrip.toBoolean())
    }

    private fun isWithSend(withSend: Boolean): String{
        return if(withSend) "Да" else "Нет"
    }

    private fun call(){
        val callUri = "tel:${ad?.tel}"
        val intCall = Intent(Intent.ACTION_DIAL)
        intCall.data = callUri.toUri()
        startActivity(intCall)
    }

    private fun sendEmail(){
        val intSendEmail = Intent(Intent.ACTION_SEND)
        intSendEmail.type = "message/rfc822"
        intSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, "Объявление")
            putExtra(Intent.EXTRA_TEXT, "Здравствуйте! Меня заинтересовало Ваше объявление!")
        }
        try {
            startActivity(Intent.createChooser(intSendEmail, "Открыть с"))
        }catch (_: ActivityNotFoundException){

        }
    }

    private fun imageChangeCounter(){
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter
            }
        })
    }
}