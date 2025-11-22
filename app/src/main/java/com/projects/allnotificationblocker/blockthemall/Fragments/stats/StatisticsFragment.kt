package com.projects.allnotificationblocker.blockthemall.Fragments.stats

import android.content.*
import android.os.*
import android.text.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.projects.allnotificationblocker.blockthemall.*
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*
import com.projects.allnotificationblocker.blockthemall.domain.*
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass.
 * Use the [StatisticsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatisticsFragment: Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var statAdapter: AppStatAdapter
    private lateinit var progressBarLoading: ProgressBar
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun initView(itemView: View) {
        recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_view)
        progressBarLoading = itemView.findViewById<ProgressBar>(R.id.progress_bar_loading)
        setAdapter()
        val editTextSearch = itemView.findViewById<EditText>(R.id.edit_text_search)
        editTextSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                statAdapter.userFilter = editable.toString()
                statAdapter.notifyDataSetChanged()
            }
        })
        progressBarLoading.visibility = View.VISIBLE
        loadData()

    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allNots = MyApplication.notificationRepo.getAllRecords()
            resetList(allNots)
            withContext(Dispatchers.Main) {
            notifiyAdapter()
            }
        }
        MyApplication.notificationRepo.getAllRecordsLive().observe(viewLifecycleOwner)
        {
            resetList(it)
            notifiyAdapter()

        }
    }
   private lateinit var filtered: MutableList<AppStat>
    private fun resetList(allNots: MutableList<NotificationInfo>) {
        val apps = MyApplication.appInfos
        val appStat = apps.map { app ->
            val notificationNumber = allNots.count { app.packageName == it.packageName }
            AppStat(
                app.packageName,
                app.appName,
                notificationNumber,
                if (allNots.isEmpty()) 0f else (notificationNumber.toFloat() / allNots.size * 100)
            )
        }

         filtered = appStat.filter { it.notificationBlockedCount != 0 }.toMutableList()
        filtered.sortWith { it, it2 -> it2.notificationBlockedCount - it.notificationBlockedCount }


    }

    private fun notifiyAdapter() {
        statAdapter.appStat = filtered
        statAdapter.notifyDataSetChanged()
        progressBarLoading.visibility = View.INVISIBLE
    }


    private fun setAdapter() {
        statAdapter = AppStatAdapter(this, mutableListOf())
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = statAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stat, container, false)
        initView(view)
        return view
    }

    companion object {
        fun newInstance(mode: Int): StatisticsFragment {
            val fragment = StatisticsFragment()
            val args = Bundle()
            args.putInt("mode", mode)
            fragment.arguments = args
            return fragment
        }
    }
}
