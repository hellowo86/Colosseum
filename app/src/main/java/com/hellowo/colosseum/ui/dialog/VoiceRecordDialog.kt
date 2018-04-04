package com.hellowo.colosseum.ui.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.media.*
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.*
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.skyfishjy.library.RippleBackground
import java.io.*
import java.util.*

@SuppressLint("ValidFragment")
class VoiceRecordDialog(private val voiceUrl: String?, private val evalutaion : Boolean,
                        private val dialogInterface: (String, Boolean) -> Unit) : BottomSheetDialog() {
    private val source = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_IN_STEREO
    private val format = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, format)
    private var filePath: String? = null

    lateinit var centerImage: ImageView
    lateinit var rippleView: RippleBackground
    lateinit var confirmBtn: LinearLayout
    lateinit var progressBar: ProgressBar
    lateinit var evaluationLy: LinearLayout
    lateinit var likeBtn: FrameLayout
    lateinit var hateBtn: FrameLayout
    lateinit var titleText: TextView
    lateinit var subText: TextView

    var isClosed = false

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_voice_record, null)
        dialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            centerImage = contentView.findViewById(R.id.centerImage)
            rippleView = contentView.findViewById(R.id.rippleView)
            confirmBtn = contentView.findViewById(R.id.confirmBtn)
            progressBar = contentView.findViewById(R.id.progressBar)
            evaluationLy = contentView.findViewById(R.id.evaluationLy)
            likeBtn = contentView.findViewById(R.id.likeBtn)
            hateBtn = contentView.findViewById(R.id.hateBtn)
            titleText = contentView.findViewById(R.id.titleText)
            subText = contentView.findViewById(R.id.subText)

            if(voiceUrl.isNullOrEmpty()) {
                titleText.text = context?.getString(R.string.voice_record)
                subText.text = context?.getString(R.string.voice_record_sub)
                confirmBtn.visibility = View.VISIBLE
                centerImage.setOnClickListener {
                    if(filePath == null) {
                        checkAudioRecordPermission()
                    }else {
                        play(null)
                    }
                }
            }else {
                titleText.text = context?.getString(R.string.voice_hear)
                subText.text = context?.getString(R.string.voice_hear_sub)
                confirmBtn.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                centerImage.setImageResource(R.drawable.play)
                centerImage.setColorFilter(activity?.resources?.getColor(R.color.lol_primary)!!)
                centerImage.visibility = View.INVISIBLE
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(voiceUrl!!)
                val twoMegabyte = 1024 * 1024 * 2.toLong()
                ref.getBytes(twoMegabyte).addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                }.addOnSuccessListener { bytes ->
                    progressBar.visibility = View.GONE
                    centerImage.setOnClickListener { play(bytes) }
                    play(bytes)
                }
            }

            if(evalutaion) {
                confirmBtn.visibility = View.GONE
                evaluationLy.visibility = View.VISIBLE
                likeBtn.setOnClickListener {
                    dialogInterface.invoke("", true)
                    dismiss()
                }
                hateBtn.setOnClickListener {
                    dialogInterface.invoke("", false)
                    dismiss()
                }
            }else {
                evaluationLy.visibility = View.GONE
            }
        }
    }

    private val permissionlistener = object : PermissionListener {
        override fun onPermissionGranted() {
            startRecording()
        }
        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
    }

    private fun checkAudioRecordPermission() {
        TedPermission(activity)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.RECORD_AUDIO)
                .check()
    }

    private fun startRecording() {
        var isRecording = false
        rippleView.startRippleAnimation()
        centerImage.visibility = View.INVISIBLE
        rippleView.postDelayed({
            if(!isClosed) {
                isRecording = false
                rippleView.stopRippleAnimation()
                centerImage.setImageResource(R.drawable.play)
                centerImage.setColorFilter(activity?.resources?.getColor(R.color.lol_primary)!!)
                centerImage.visibility = View.VISIBLE
                confirmBtn.setBackgroundColor(activity?.resources?.getColor(R.color.lol_primary)!!)
                confirmBtn.setOnClickListener {
                    filePath?.let { dialogInterface.invoke(it, false) }
                    dismiss()
                }
            }
        }, 5000)

        val record = AudioRecord(source, sampleRate, channel, format, bufferSize)
        val thread = Thread{
            val readData = ByteArray(bufferSize)
            val outputDir = activity?.cacheDir // context being the Activity pointer
            filePath = File.createTempFile("voice", "", outputDir).absolutePath
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(filePath)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            while (isRecording) {
                val ret = record.read(readData, 0, bufferSize)  //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
                try {
                    fos?.write(readData, 0, bufferSize)    //  읽어온 readData 를 파일에 write 함
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            record.stop()
            record.release()

            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        isRecording = true
        record.startRecording()
        thread.start()
    }

    private fun play(bytes: ByteArray?) {
        var isPlaying = false
        rippleView.startRippleAnimation()
        centerImage.visibility = View.INVISIBLE
        rippleView.postDelayed({
            if(!isClosed) {
                isPlaying = false
                rippleView.stopRippleAnimation()
                centerImage.visibility = View.VISIBLE
            }
        }, 5000)

        val track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel, format, bufferSize, AudioTrack.MODE_STREAM)
        val thread = Thread{
            val writeData = ByteArray(bufferSize)

            if(bytes == null) {
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(filePath)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val dis = DataInputStream(fis)
                track.play()  // write 하기 전에 play 를 먼저 수행해 주어야 함

                while (isPlaying) {
                    try {
                        val ret = dis.read(writeData, 0, bufferSize)
                        if (ret <= 0) {
                            activity?.runOnUiThread{}
                            break
                        }
                        track.write(writeData, 0, ret) // AudioTrack 에 write 를 하면 스피커로 송출됨
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                track.stop()
                track.release()

                try {
                    dis.close()
                    fis?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }else {
                val bis = ByteArrayInputStream(bytes)
                track.play()  // write 하기 전에 play 를 먼저 수행해 주어야 함

                while (isPlaying) {
                    try {
                        val ret = bis.read(writeData, 0, bufferSize)
                        if (ret <= 0) {
                            activity?.runOnUiThread{}
                            break
                        }
                        track.write(writeData, 0, ret) // AudioTrack 에 write 를 하면 스피커로 송출됨
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                track.stop()
                track.release()

                try {
                    bis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        isPlaying = true
        thread.start()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        isClosed = true
    }
}