package com.projects.allnotificationblocker.blockthemall.Activities.premium

import com.projects.allnotificationblocker.blockthemall.R
import com.projects.allnotificationblocker.blockthemall.databinding.ActivitySubscriptionBinding


import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var billingClient: BillingClient



    private var selectedPlanId = 1
    private var selectedProductId = "one_month_subscription"
    private var productDetailsList: List<ProductDetails>? = null

    private lateinit var subscriptionCards: List<LinearLayout>
    private lateinit var radioIcons: List<ImageView>
    private lateinit var titleTexts: List<TextView>
    private lateinit var descTexts: List<TextView>
    private lateinit var priceTexts: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI elements
        subscriptionCards = listOf(
            findViewById(R.id.subCardMonthly),
            findViewById(R.id.subCardHalfYearly),
            findViewById(R.id.subCardYearly)
        )
        radioIcons = listOf(
            findViewById(R.id.radioMonthly),
            findViewById(R.id.radioHalfYearly),
            findViewById(R.id.radioYearly)
        )
        titleTexts = listOf(
            findViewById(R.id.titleMonthly),
            findViewById(R.id.titleHalfYearly),
            findViewById(R.id.titleYearly)
        )
        descTexts = listOf(
            findViewById(R.id.descMonthly),
            findViewById(R.id.descHalfYearly),
            findViewById(R.id.descYearly)
        )
        priceTexts = listOf(
            findViewById(R.id.priceMonthly),
            findViewById(R.id.priceHalfYearly),
            findViewById(R.id.priceYearly)
        )

        // Handle clicks on subscription cards
        subscriptionCards.forEachIndexed { index, card ->
            card.setOnClickListener { selectSubscription(index) }
        }

        billingClient = BillingClient.newBuilder(this)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    handlePurchases(purchases)
                }
            }.enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

        establishConnection()

        // Buy button click
        binding.btnSubscribe.setOnClickListener {
            launchPurchaseFlowIfAvailable(selectedProductId)
        }
    }

    private fun selectSubscription(index: Int) {
        val productIds = listOf("one_month_subscription", "six_month_subscriptions", "12_month_subscriptions")
        selectedProductId = productIds[index]
        selectedPlanId = when (index) {
            0 -> 1
            1 -> 6
            else -> 12
        }

        for (i in subscriptionCards.indices) {
            if (i == index) {
                subscriptionCards[i].background = ContextCompat.getDrawable(this, R.drawable.bg_subscription_selected)
                radioIcons[i].setImageResource(R.drawable.ic_radio_checked)
                titleTexts[i].setTextColor(Color.WHITE)
                descTexts[i].setTextColor(Color.WHITE)
                priceTexts[i].setTextColor(Color.WHITE)
            } else {
                subscriptionCards[i].background = ContextCompat.getDrawable(this, R.drawable.bg_subscription_normal)
                radioIcons[i].setImageResource(R.drawable.ic_radio_unchecked)
                titleTexts[i].setTextColor("#000000".toColorInt())
                descTexts[i].setTextColor("#000000".toColorInt())
                priceTexts[i].setTextColor("#000000".toColorInt())
            }
        }
    }

    // ---------- BILLING ----------

    private fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                establishConnection()
            }
        })
    }

    private fun showProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("one_month_subscription")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("six_month_subscriptions")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("12_month_subscriptions")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList = productDetailsResult.productDetailsList
                Log.d("Billing", "Products loaded successfully: ${productDetailsList?.size}")
            } else {
                Log.e("Billing", "Failed to load products: ${billingResult.debugMessage}")
            }
        }
    }


    private fun launchPurchaseFlowIfAvailable(productId: String) {
        val details = productDetailsList?.find { it.productId == productId }
        if (details != null) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
            val paramsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offerToken)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(paramsList)
                .build()
            billingClient.launchBillingFlow(this, billingFlowParams)
        } else {
            Toast.makeText(this, "Product not ready. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                verifySubPurchase(purchase)
            }
        }
    }


    private fun verifySubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Handler(Looper.getMainLooper()).postDelayed({
                    // Use StateFlow to notify the payment success
                    Log.d("PremiumLogs", "payment success")
                    PrefSub.setPremium(this,true)
                    onBackPressedDispatcher.onBackPressed()
                }, 500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifySubPurchase(purchase)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::billingClient.isInitialized) billingClient.endConnection()
    }



}
