package com.simba_studio.mywork.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.simba_studio.mywork.R
import com.simba_studio.mywork.databinding.ActivityFilterBinding
import com.simba_studio.mywork.dialogs.DialogSpinnerHelper
import com.simba_studio.mywork.ultius.ProfileHelper


class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickSelectSector()
        onClickSelectCities()
        onClickDone()
        onClickClear()
        actionBarSettings()
        getFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }

    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null && filter != "empty"){
            val filterArray = filter.split("_")
            if(filterArray[0] != "empty") tvSector.text = filterArray[0]
            if(filterArray[1] != "empty") tvCity.text = filterArray[1]
            checkBoxWithSend.isChecked = filterArray[2].toBoolean()
        }
    }

    private fun onClickSelectSector()  = with(binding){
        tvSector.setOnClickListener {
            val listSectors = ProfileHelper.getAllSectors(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listSectors, tvSector)
            if(binding.tvCity.text.toString() != getString(R.string.select_profile)){
                tvCity.text = getString(R.string.select_profile)
            }
        }
    }

    private fun onClickSelectCities() = with(binding){
        tvCity.setOnClickListener {
            val selectedSector = tvSector.text.toString()
            if(selectedSector != getString(R.string.select_works)){
                val listCities = ProfileHelper.getAllCities(selectedSector,this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCities, tvCity)
            }else {
                Toast.makeText(this@FilterActivity, "No sector selected", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onClickDone() = with(binding){
        btDone.setOnClickListener {
            val i = Intent().apply {
                putExtra(FILTER_KEY, createFilter())
            }
            setResult(RESULT_OK, i)
            finish()
        }
    }

    private fun onClickClear() = with(binding){
        btClear.setOnClickListener {
            tvSector.text = getString(R.string.select_works)
            tvCity.text = getString(R.string.select_profile)
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun createFilter(): String = with(binding){
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(tvSector.text,
            tvCity.text,
            checkBoxWithSend.isChecked.toString()
        )
        for((i, s) in arrayTempFilter.withIndex()){
            if(s != getString(R.string.select_works) && s != getString(R.string.select_profile) && s.isNotEmpty()){
                sBuilder.append(s)
                if(i != arrayTempFilter.size - 1) sBuilder.append("_")
            }else {
                sBuilder.append("empty")
                if(i != arrayTempFilter.size - 1) sBuilder.append("_")
            }
        }
        return sBuilder.toString()
    }

    fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    companion object{
        const val FILTER_KEY = "filter_key"
    }
}