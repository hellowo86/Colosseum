package com.hellowo.colosseum.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hellowo.colosseum.data.Me
import com.hellowo.colosseum.fcm.MessagingService
import com.hellowo.colosseum.model.BadgeData
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.model.SearchedUser
import com.hellowo.colosseum.model.User
import com.hellowo.colosseum.utils.distFrom
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm


class InterestViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    val db = FirebaseFirestore.getInstance()
    var choiceList = MutableLiveData<ArrayList<User>>()
    var interestMeList = MutableLiveData<ArrayList<User>>()
    var loading = MutableLiveData<Boolean>()
    var viewMode = MutableLiveData<Int>()
    var interestCompleted = MutableLiveData<User>()

    init {
        choiceList.value = ArrayList()
        interestMeList.value = ArrayList()
        loading.value = false
        viewMode.value = 0
        loadInterestMeList()
    }

    fun loadInterestMeList() {
        loading.value = true
        interestMeList.value?.clear()
        val myPrefix = Couple.getGenderKey(Me.value?.gender as Int)
        val yourPrefix = Couple.getGenderKey(Me.value?.yourGender() as Int)
        db.collection("couples").whereEqualTo("level", 1).whereEqualTo("${myPrefix}Id", Me.value?.id)
                .whereEqualTo("${yourPrefix}Interest", 1).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val totalCount = task.result.size()
                        if(totalCount > 0) {
                            var count = 0
                            task.result.forEach {
                                        db.collection("users").document(it.getString("${yourPrefix}Id")).get().addOnCompleteListener { userTask ->
                                            count++
                                            if (userTask.isSuccessful) {
                                                interestMeList.value?.add(userTask.result.toObject(User::class.java))
                                            }
                                            if(count == totalCount) {
                                                interestMeList.value = interestMeList.value
                                                loading.value = false
                                            }
                                        }
                                    }
                        }else {
                            interestMeList.value = interestMeList.value
                            loading.value = false
                        }
                    }else {
                        interestMeList.value = interestMeList.value
                        loading.value = false
                    }
                }
    }

    fun startNewSearch(lastVisible: DocumentSnapshot?) {
        var query = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("gender", if(Me.value?.gender == 0) 1 else 0)
                .orderBy("dtConnected", Query.Direction.DESCENDING)
                .limit(100)

        if(lastVisible == null) {
            choiceList.value?.clear()
            viewMode.value = 1
        }else {
            query = query.startAfter(lastVisible)
        }

        val maxResult = 5
        val myLat = Me.value?.lat!!
        val myLng = Me.value?.lng!!
        val ageMin = Prefs.getInt("ageMin", 0) + 18
        var ageMax = Prefs.getInt("ageMax", 22) + 18
        if(ageMax == 40) {
            ageMax = Int.MAX_VALUE
        }
        val distanceMin = Prefs.getInt("distanceMin", 0) * 10
        var distanceMax = Prefs.getInt("distanceMax", 21) * 10
        if(distanceMax == 210) {
            distanceMax = Int.MAX_VALUE
        }

        query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result
                                .map { it.toObject(User::class.java) }
                                .forEach { user ->
                                    val age = User.getAge(user.birth)
                                    val distance = distFrom(user.lat, user.lng, myLat, myLng).toInt()

                                    if(choiceList.value?.size!! < maxResult && age in ageMin..ageMax && distance in distanceMin..distanceMax) {
                                        realm.executeTransaction { _ ->
                                            if(realm.where(SearchedUser::class.java).equalTo("id", user.id).count() == 0L) {
                                                choiceList.value?.add(user)
                                            }
                                        }
                                    }
                                }

                        if(task.result.size() > 0 && choiceList.value?.size!! < maxResult) {
                            startNewSearch(task.result.last())
                        }else {
                            viewMode.value = 2
                        }
                    }else {
                        viewMode.value = 2
                    }
                }
    }

    fun interest(user: User, interest: Int) {
        val key = Couple.makeCoupleKey(Me.value!!, user)
        val ref = db.collection("couples").document(key)
        ref.get().addOnCompleteListener { task ->
            val doc = task.result
            val data = HashMap<String, Any?>()
            val yourPrefix = Couple.getGenderKey(user.gender)
            val myPrefix = Couple.getGenderKey(Me.value?.gender!!)
            data.put("${myPrefix}Interest", interest)
            data.put("dtUpdated", FieldValue.serverTimestamp())
            if (doc.exists()) {
                if(interest == 1 && doc.get("${yourPrefix}Interest").toString() == "1") {
                    data.put("level", 2)
                    interestCompleted.value = user
                    interestCompleted.value = null
                    realm.executeTransaction { _ ->
                        val badgeData = realm.where(BadgeData::class.java).equalTo("id", "chemistry$key").findFirst()
                        if(badgeData != null) {
                            badgeData.count = 1
                        }else {
                            val badgeData = realm.createObject(BadgeData::class.java, "chemistry$key")
                            badgeData.type = "chemistry"
                            badgeData.count = 1
                        }
                    }
                    MessagingService.sendPushMessage(user.pushToken!!, 1, user.id!!, user.nickName!!, "", key)
                }else if(interest == 0) {
                    data.put("level", 0)
                }
                ref.update(data)
            } else {
                data.put("level", 1)
                data.put("${yourPrefix}Interest", -1)
                data.put("${yourPrefix}Id", user.id)
                data.put("${yourPrefix}Name", user.nickName)
                data.put("${yourPrefix}PushToken", user.pushToken)
                data.put("${myPrefix}Id", Me.value?.id)
                data.put("${myPrefix}Name", Me.value?.nickName)
                data.put("${myPrefix}PushToken", Me.value?.pushToken)
                ref.set(data)
            }

            realm.executeTransaction { _ ->
                if(realm.where(SearchedUser::class.java).equalTo("id", user.id).count() == 0L && interest == 0) {
                    realm.createObject(SearchedUser::class.java, user.id)
                }
            }
        }
    }

    fun stackEmpty() {
        viewMode.value = 0
        interestMeList.value = interestMeList.value
    }

    fun response() {
        choiceList.value?.clear()
        choiceList.value?.addAll(interestMeList.value!!)
        interestMeList.value?.clear()
        choiceList.value = choiceList.value
        viewMode.value = 4
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

}