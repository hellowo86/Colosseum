package com.hellowo.colosseum.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.firebase.firestore.FirebaseFirestore
import com.hellowo.colosseum.R
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.inAppKey
import com.hellowo.colosseum.utils.toast
import kotlinx.android.synthetic.main.activity_coin.*

class CoinActivity : BaseActivity(), BillingProcessor.IBillingHandler {
    var bp: BillingProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin)
        bp = BillingProcessor(this, inAppKey, this)
        initLayout()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { finish() }
        Me.value?.let { currentCoinText.text = it.coin.toString() }
    }

    private fun chargeCoin(productId: String) {
        showProgressDialog()
        var coinNum = getCoinNum(productId)
        Me.value?.let {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("users").document(it.id!!)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                coinNum = (snapshot.getDouble("coin") + coinNum).toInt()
                transaction.update(ref, "coin", coinNum)
            }
            .addOnSuccessListener { result ->
                currentCoinText.text = coinNum.toString()
                consumePurchase(productId)
                toast(this@CoinActivity, R.string.charged_coin)
                hideProgressDialog()
            }
            .addOnFailureListener { e -> toast(this@CoinActivity, R.string.retry_again) }
        }
    }

    private fun getCoinNum(productId: String): Int {
        return when (productId) {
            "coin_10" -> 10
            "coin_50" -> 50
            "coin_100" -> 100
            else -> 0
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun consumePurchase(productId: String) {
        object : AsyncTask<Void, Void, Boolean>(){
            var result = false
            override fun doInBackground(vararg p0: Void?): Boolean {
                result = bp?.consumePurchase(productId)!!
                return true
            }
            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                if(!(result as Boolean)){ // 소비 실패

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    override fun onBillingInitialized() {
        object : AsyncTask<Void, Void, Boolean>() {
            var price10 : String? = null
            var price50 : String? = null
            var price100 : String? = null

            override fun doInBackground(vararg p0: Void?): Boolean {
                price10 = bp?.getPurchaseListingDetails("coin_10")?.priceText
                price50 = bp?.getPurchaseListingDetails("coin_50")?.priceText
                price100 = bp?.getPurchaseListingDetails("coin_100")?.priceText
                return true
            }

            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                coin10Price.text = price10
                coin50Price.text = price50
                coin100Price.text = price100
                coin10Btn.setOnClickListener { bp?.purchase(this@CoinActivity, "coin_10") }
                coin50Btn.setOnClickListener { bp?.purchase(this@CoinActivity, "coin_50") }
                coin100Btn.setOnClickListener { bp?.purchase(this@CoinActivity, "coin_100") }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onPurchaseHistoryRestored() {}

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        chargeCoin(productId)
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp?.handleActivityResult(requestCode, resultCode, data)!!) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}