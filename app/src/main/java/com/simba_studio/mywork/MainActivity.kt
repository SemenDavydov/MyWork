package com.simba_studio.mywork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.simba_studio.mywork.accounthelper.AccountHelper
import com.simba_studio.mywork.act.DescriptionActivity
import com.simba_studio.mywork.act.EditAdsAct
import com.simba_studio.mywork.act.FilterActivity
import com.simba_studio.mywork.act.showToast
import com.simba_studio.mywork.adapters.AdsRcAdapter
import com.simba_studio.mywork.adapters.RoundCornersTransform
import com.simba_studio.mywork.databinding.ActivityMainBinding
import com.simba_studio.mywork.dialoghelper.DialogConst
import com.simba_studio.mywork.dialoghelper.DialogHelper
import com.simba_studio.mywork.model.Ad
import com.simba_studio.mywork.ultius.FilterManager
import com.simba_studio.mywork.viewModel.FirebaseViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {

    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val myAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    lateinit var filterLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filter: String = "empty"
    private var filterDb: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
        super.onCreate(savedInstanceState)
        setContentView(view)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.id_filter){
            val i = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(i)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home

    }

    private fun onActivityResult() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }

                } catch (e: ApiException) {
                    Log.d("MyLog", "API Error : ${e.message}")
                }
            }
    }

    private fun onActivityResultFilter(){
        filterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                filterDb = FilterManager.getFilter(filter)
            } else if(it.resultCode == RESULT_CANCELED){
                filterDb = ""
                filter = "empty"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        userInterfaceUpdate(myAuth.currentUser)
    }

    private fun initViewModel(){
        firebaseViewModel.liveAdsData.observe(this) {
            val list = getAdsByCategory(it)
            if (!clearUpdate) {
                adapter.updateAdapter(list)
            } else {
                adapter.updateAdapterWithClear(list)
            }
            binding.mainContent.tvEmpty.visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad>{
        val temList = ArrayList<Ad>()
        temList.addAll(list)
        if(currentCategory != getString(R.string.def)){
            temList.clear()
            list.forEach{
                if(currentCategory == it.category) temList.add(it)
            }
        }
        temList.reverse()
        return temList
    }

    private fun init(){
        currentCategory = getString(R.string.def)
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        navViewSettings()
        //////////Отсюда начинается код кнопки/////////////////////////////
        val toggle  = ActionBarDrawerToggle(this, binding.drawlerLayout, binding.mainContent.toolbar, R.string.open, R.string.close)
        binding.drawlerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)
        //Здесь мы создаём кнопку по нажатию на котороую выходит боковое меню
    }

    private fun bottomMenuOnClick() = with(binding){
        mainContent.bNavView.setOnItemSelectedListener { item ->
            clearUpdate = true
            when(item.itemId){
                R.id.id_new_ad -> {
                    if (myAuth.currentUser != null) {
                        if (!myAuth.currentUser?.isAnonymous!!) {
                            val i = Intent(this@MainActivity, EditAdsAct::class.java)
                            startActivity(i)
                        } else {
                            showToast("Гость не может публиковать объявления!")
                        }
                    } else {
                        showToast("Ошибка регистрации")
                    }
                }

                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }

                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                    mainContent.toolbar.title = getString((R.string.ad_my_fav))
                }

                R.id.id_home -> {
                    currentCategory = getString(R.string.def)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb)
                    mainContent.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecyclerView(){
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when(item.itemId){

            R.id.id_my_ads -> {
                firebaseViewModel.loadMyAds()
            }

            R.id.id_cars -> {
                getAdsFromCat(getString(R.string.in_search_of_job))
            }

            R.id.id_pc -> {
                getAdsFromCat(getString(R.string.in_search_employee))
            }

            R.id.id_smart -> {
                getAdsFromCat(getString(R.string.looking_help))
            }

            R.id.id_apps -> {
                getAdsFromCat(getString(R.string.typing_command))
            }

            R.id.sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }

            R.id.sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }

            R.id.out -> {
                if(myAuth.currentUser?.isAnonymous == true) {
                    binding.drawlerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                userInterfaceUpdate(null)
                myAuth.signOut()
                dialogHelper.accHelper.signOutG()
            }
        }
        binding.drawlerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String){
        currentCategory = cat
        firebaseViewModel.loadAllAdsFromCat(cat, filterDb)
    }

    fun userInterfaceUpdate(user:FirebaseUser?){
        if(user == null){
            dialogHelper.accHelper.signInAnonymously(object: AccountHelper.Listener{
                override fun onComplete() {
                    tvAccount.setText(R.string.gost)
                    imAccount.setImageResource(R.drawable.ic_account_def)
                }
            })
        } else if(user.isAnonymous){
            tvAccount.setText(R.string.gost)
            imAccount.setImageResource(R.drawable.ic_account_def)
        } else if(!user.isAnonymous){
            tvAccount.text = user.email

            //Делаем аватар профиля правильной овальной формы:
            Picasso.get().load(user.photoUrl).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.ic_default_image).transform(RoundCornersTransform(45.0f)).into(imAccount)
        }
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val int = Intent(this, DescriptionActivity::class.java)
        int.putExtra("AD", ad)
        startActivity(int)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding){
        val menu = navView.menu

        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.blue_light)), 0, adsCat.title!!.length, 0)
        adsCat.title = spanAdsCat

        val accCat = menu.findItem(R.id.accCat)
        val spanAccCat = SpannableString(accCat.title)
        spanAccCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.blue_light)),0, accCat.title!!.length, 0)
        accCat.title = spanAccCat
    }

    private fun scrollListener() = with(binding.mainContent){
        rcView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recView, newState)
                if(!recView.canScrollVertically(SCROLL_DOWN)
                    && newState == RecyclerView.SCROLL_STATE_IDLE){
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if(adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {
            if (currentCategory == getString(R.string.def)) {
                firebaseViewModel.loadAllAdsNextPage(it.time, filterDb)
            } else {
                firebaseViewModel.loadAllAdsFromCatNextPage(it.category!!, it.time, filterDb)
            }
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }

}