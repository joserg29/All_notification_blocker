package com.projects.allnotificationblocker.blockthemall.Activities.Splash

import android.content.*
import android.content.pm.*
import android.os.*
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.*
import androidx.core.app.*
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryPurchasesParams
import com.projects.allnotificationblocker.blockthemall.Activities.Home.*
import com.projects.allnotificationblocker.blockthemall.Activities.premium.PrefSub
import com.projects.allnotificationblocker.blockthemall.Application.*
import com.projects.allnotificationblocker.blockthemall.Application.MyApplication.Companion.addAppInfo
import com.projects.allnotificationblocker.blockthemall.Dialogs.*
import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.Utilities.Util.enableEdgeToEdge16
import com.projects.allnotificationblocker.blockthemall.domain.*
import timber.log.*

class SplashActivity: AppCompatActivity() {
    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        enableEdgeToEdge16(findViewById(R.id.main))
        checkSubscription()
    }

    override fun onResume() {
        super.onResume()
        checkNotificationAccess()
    }

    private lateinit var billingClient: BillingClient

    private fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts() // optional if you have one-time purchases
                    .build()
            )
            .setListener { billingResult, purchases ->
                // Optional: handle real-time purchase updates here
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Retry connection on disconnect
                Timber.tag("SubscriptionCheck").w("Billing service disconnected. Retrying...")
                billingClient.startConnection(this)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing service ready, query active subscriptions
                    queryActiveSubscriptions()
                } else {
                    Timber.tag("SubscriptionCheck")
                        .e("Billing setup failed: ${billingResult.debugMessage}")
                }
            }
        })
    }

    private fun queryActiveSubscriptions() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchasesList.isNotEmpty()) {
                    Timber.tag("SubscriptionCheck")
                        .d("Active subscriptions found: ${purchasesList.size}")
                    PrefSub.setPremium(this, true) // User is premium

                    // Log subscription details
                    purchasesList.forEachIndexed { index, purchase ->
                        Timber.tag("SubscriptionCheck").d("Purchase index: $index")
                        Timber.tag("SubscriptionCheck").d("Purchase JSON: ${purchase.originalJson}")
                    }
                } else {
                    Timber.tag("SubscriptionCheck").d("No active subscriptions.")
                    PrefSub.setPremium(this, false) // User is not premium
                }
            } else {
                Timber.tag("SubscriptionCheck")
                    .e("Failed to query purchases: ${billingResult.debugMessage}")
            }
        }
    }


    private fun checkNotificationAccess() {
        if (!NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(applicationContext.packageName)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
            if (!isOpen) {
                val dialog = InfoDialog(
                    this,
                    getString(R.string.notifications_access),
                    getString(R.string.notification_access_permission_is_required)
                )
                dialog.show()
                isOpen = true
                dialog.setOnDismissListener(DialogInterface.OnDismissListener { dialogInterface: DialogInterface? ->
                    if (dialog.result) {
                        applicationContext.startActivity(
                            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        )
                    }
                    isOpen = false
                })
            }
        } else {
            //startService(new Intent(getApplicationContext(), MainService.class));
            if (MyApplication.Companion.USE_MY_APPLICATION) {
                if (MyApplication.appInfos.isEmpty()) {
//                    new CountDownTimer(1000, 500) {
//
//                        public void onTick(long millisUntilFinished) {
//                        }
//
//                        public void onFinish() {
//                            new LoadAppInfoAsyncTask().execute();
//                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                            startActivity(intent);
//                        }
//                    }.start();
                    LoadAppInfoAsyncTask().execute()
                } else {
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                object: CountDownTimer(1000, 500) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.start()
            }
        }
    }


    private inner class LoadAppInfoAsyncTask: AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg args: Void?): Void? {
            // TimingLogger timings = new TimingLogger("MyTag", "Loading Apps");


            val packs = packageManager.getInstalledPackages(0)

            //timings.addSplit("getInstalledPackages");
            Timber.tag("AppInfo").d("packs.size(): %d", packs.size)
            for (i in packs.indices) {
                val p = packs[i]

                if (packageManager.getLaunchIntentForPackage(p.packageName) != null) {
                    val newInfo = AppInfo()
                    val name = p.applicationInfo!!.loadLabel(packageManager).toString()
                    newInfo.appName = name
                    newInfo.packageName = p.packageName
                    addAppInfo(p.packageName, newInfo)
                }
            }


            return null
        }

        override fun onPostExecute(res: Void?) {
            super.onPostExecute(res)
            val intent = Intent(applicationContext, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
