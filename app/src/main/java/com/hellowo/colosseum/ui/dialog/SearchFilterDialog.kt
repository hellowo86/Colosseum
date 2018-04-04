package com.hellowo.colosseum.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.hellowo.colosseum.R
import com.hellowo.colosseum.utils.log
import com.pixplicity.easyprefs.library.Prefs
import me.bendik.simplerangeview.SimpleRangeView

@SuppressLint("ValidFragment")
class SearchFilterDialog() : BottomSheetDialog() {

    lateinit var titleText: TextView

    @SuppressLint("SetTextI18n")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_search_filter, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            titleText = contentView.findViewById(R.id.titleText)

            val ageSeekbar = contentView.findViewById<SimpleRangeView>(R.id.ageSeekbar)
            val ageText = contentView.findViewById<TextView>(R.id.ageText)

            var ageMin = Prefs.getInt("ageMin", 0)
            var ageMax = Prefs.getInt("ageMax", 22)

            ageSeekbar.onRangeLabelsListener = object : SimpleRangeView.OnRangeLabelsListener{
                override fun getLabelTextForPosition(rangeView: SimpleRangeView, pos: Int, state: SimpleRangeView.State): String? {
                    return if (pos == 0) {
                        "18"
                    } else if (pos == 22) {
                        "40"
                    } else {
                        ""
                    }
                }
            }
            ageSeekbar.onTrackRangeListener = object : SimpleRangeView.OnTrackRangeListener{
                override fun onStartRangeChanged(rangeView: SimpleRangeView, start: Int) {
                    ageMin = start
                    ageText.text = "${18 + ageMin} ~ ${18 + ageMax}"
                }
                override fun onEndRangeChanged(rangeView: SimpleRangeView, end: Int) {
                    ageMax = end
                    ageText.text = "${18 + ageMin} ~ ${18 + ageMax}"
                }
            }
            ageSeekbar.onChangeRangeListener = object : SimpleRangeView.OnChangeRangeListener{
                override fun onRangeChanged(rangeView: SimpleRangeView, start: Int, end: Int) {
                    Prefs.putInt("ageMin", start)
                    Prefs.putInt("ageMax", end)
                }
            }

            ageSeekbar.start = ageMin
            ageSeekbar.end = ageMax
            ageText.text = "${18 + ageMin} ~ ${18 + ageMax}"


            val distanceSeekbar = contentView.findViewById<SimpleRangeView>(R.id.distanceSeekbar)
            val distanceText = contentView.findViewById<TextView>(R.id.distanceText)

            var distanceMin = Prefs.getInt("distanceMin", 0)
            var distanceMax = Prefs.getInt("distanceMax", 21)

            distanceSeekbar.onRangeLabelsListener = object : SimpleRangeView.OnRangeLabelsListener{
                override fun getLabelTextForPosition(rangeView: SimpleRangeView, pos: Int, state: SimpleRangeView.State): String? {
                    return when (pos) {
                        0 -> "0km"
                        21 -> "∞"
                        else -> ""
                    }
                }
            }
            distanceSeekbar.onTrackRangeListener = object : SimpleRangeView.OnTrackRangeListener{
                override fun onStartRangeChanged(rangeView: SimpleRangeView, start: Int) {
                    distanceMin = start
                    distanceText.text = "${getDistanceText(distanceMin)} ~ ${getDistanceText(distanceMax)}"
                }
                override fun onEndRangeChanged(rangeView: SimpleRangeView, end: Int) {
                    distanceMax = end
                    distanceText.text = "${getDistanceText(distanceMin)} ~ ${getDistanceText(distanceMax)}"
                }
            }
            distanceSeekbar.onChangeRangeListener = object : SimpleRangeView.OnChangeRangeListener{
                override fun onRangeChanged(rangeView: SimpleRangeView, start: Int, end: Int) {
                    Prefs.putInt("distanceMin", start)
                    Prefs.putInt("distanceMax", end)
                }
            }

            distanceSeekbar.start = distanceMin
            distanceSeekbar.end = distanceMax
            distanceText.text = "${getDistanceText(distanceMin)} ~ ${getDistanceText(distanceMax)}"
        }
    }

    fun getDistanceText(pos: Int): String {
        return when (pos) {
            in 0..20 -> "${pos * 10} km"
            21 -> getString(R.string.no_limit)
            else -> ""
        }
    }
}