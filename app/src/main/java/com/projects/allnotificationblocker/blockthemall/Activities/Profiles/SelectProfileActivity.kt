package com.projects.allnotificationblocker.blockthemall.Activities.Profiles

import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.lifecycle.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.*
import com.google.android.gms.ads.*
import com.google.android.material.floatingactionbutton.*
import com.pixplicity.easyprefs.library.*
import com.projects.allnotificationblocker.blockthemall.Activities.premium.PrefSub
import com.projects.allnotificationblocker.blockthemall.BuildConfig
import com.projects.allnotificationblocker.blockthemall.Dialogs.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import timber.log.*

// import android.support.v4.content.ContextCompat;
// import android.support.v7.widget.DividerItemDecoration;
class SelectProfileActivity: AppCompatActivity(), View.OnClickListener {
    private val profiles = ArrayList<Profile>()
    private var recyclerView: RecyclerView? = null
    private var progressBarLoading: ProgressBar? = null
    private var textViewLoading: TextView? = null
    private var linearLayoutLoading: LinearLayout? = null
    private var mAdapter: SelectProfileRecyclerViewAdapter? = null
    private var viewModel: ProfilesViewModel? = null
    private var mNewProfileFab: FloatingActionButton? = null
    private var mButtonBackImage: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_profile)
        initViews()
        setAdapter()
        showLoading()
        viewModel =
            ViewModelProviders.of(this).get<ProfilesViewModel>(ProfilesViewModel::class.java)
        viewModel!!.allRecords.observe(this, Observer { records: MutableList<Profile>? ->
            profiles.clear()
            profiles.addAll(records!!)
            if (profiles.isNotEmpty()) {
                hideLoading()
            } else {
                showNoItems()
            }
            mAdapter!!.notifyDataSetChanged()
        })


        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }


    private fun initViews() {
        initAds()

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        progressBarLoading = findViewById<ProgressBar>(R.id.progress_bar_loading)
        textViewLoading = findViewById<TextView>(R.id.text_view_loading)
        linearLayoutLoading = findViewById<LinearLayout>(R.id.linear_layout_loading)

        mNewProfileFab = findViewById<FloatingActionButton>(R.id.fab_new_profile)
        mNewProfileFab!!.setOnClickListener(this)

        mButtonBackImage = findViewById<ImageButton>(R.id.image_button_back)
        mButtonBackImage!!.setOnClickListener(this)
    }


    private fun showNoItems() {
        recyclerView!!.visibility = View.GONE
        linearLayoutLoading!!.visibility = View.VISIBLE
        progressBarLoading!!.visibility = View.GONE
        textViewLoading!!.visibility = View.VISIBLE
        textViewLoading!!.setText(R.string.no_profiles)
    }

    private fun showLoading() {
        recyclerView!!.visibility = View.GONE
        linearLayoutLoading!!.visibility = View.VISIBLE
        progressBarLoading!!.visibility = View.VISIBLE
        textViewLoading!!.visibility = View.VISIBLE
        textViewLoading!!.setText(R.string.loading)

        object: CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (!profiles.isEmpty()) {
                    hideLoading()
                } else {
                    showNoItems()
                }
            }
        }.start()
    }

    private fun initAds() {
        Timber.tag("AppInfo").d("initAds")
        val llAds = findViewById<LinearLayout>(R.id.llAds)

        // If user is premium, hide the ads layout
        if (PrefSub.isPremium(this)) {
            llAds.visibility = View.GONE
            return
        }

        val adView = AdView(this)
        adView.adUnitId = getString(R.string.ad_view_unit_id)
        adView.setAdSize(AdSize.SMART_BANNER)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Timber.tag("AppInfo").d("onAdLoaded")
                llAds.addView(adView)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                Timber.tag("AppInfo").e("Ad failed to load: $error")
            }
        }
    }



    private fun hideLoading() {
        linearLayoutLoading!!.visibility = View.GONE
        recyclerView!!.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { //finish();
            onBackPressed()
        }
        return true
    }


    override fun onBackPressed() {
        //Execute your code here
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }


    private fun setAdapter() {
        mAdapter = SelectProfileRecyclerViewAdapter(profiles)

        recyclerView!!.setHasFixedSize(true)

        // use a linear layout manager
        val layoutManager = LinearLayoutManager(this)

        recyclerView!!.layoutManager = layoutManager


        recyclerView!!.adapter = mAdapter


        mAdapter!!.SetOnItemClickListener(object:
            SelectProfileRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int, model: Profile?) {
                //handle item click events here
                //Toast.makeText(SelectProfileActivity.this, "Hey " + model.getTitle(), Toast.LENGTH_SHORT).show();

                val dialog = ConfirmDialog(
                    this@SelectProfileActivity,
                    getString(
                        R.string.are_you_sure_apply_profile,
                        profiles[position].name
                    )
                )
                dialog.show()
                dialog.setCancelable(true)
                dialog.setOnDismissListener(DialogInterface.OnDismissListener { dialogInterface: DialogInterface? ->
                    if (dialog.result) {
                        val intent = Intent()
                        intent.putExtra(
                            Constants.PARAM_SELECTED_PROFILE_NAME,
                            profiles[position].name
                        )
                        intent.putExtra(
                            Constants.PARAM_SELECTED_PROFILE,
                            profiles[position].toJson()
                        )

                        setResult(RESULT_OK, intent)
                        finish()
                    }
                })
            }

            override fun deleteProfile(profile: Profile?) {
                profile!!
                val dialog = ConfirmDialog(
                    this@SelectProfileActivity,
                    getString(R.string.are_you_sure_delete_profile, profile.name)
                )
                dialog.show()
                dialog.setCancelable(true)
                dialog.setOnDismissListener(DialogInterface.OnDismissListener { dialogInterface: DialogInterface? ->
                    if (dialog.result) {
                        val selectedProfileName =
                            Prefs.getString(Constants.PARAM_SELECTED_PROFILE_NAME, "")
                        if (selectedProfileName == profile.name) {
                            Prefs.putString(Constants.PARAM_SELECTED_PROFILE_NAME, "")
                        }
                        lifecycleScope.launch {
                        viewModel!!.delete(profile)
                        }
                    }
                })
            }

            override fun editProfile(profile: Profile?) {
                profile!!
                val intent = Intent(applicationContext, AddEditProfileActivity::class.java)
                intent.putExtra(Constants.PARAM_SELECTED_PROFILE_NAME, profile.name)
                intent.putExtra(Constants.PARAM_SELECTED_PROFILE, profile.toJson())
                startActivity(intent)
            }
        })
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_new_profile -> {
                val intent = Intent(applicationContext, AddEditProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.image_button_back -> onBackPressed()
            else -> {}
        }
    }
}
