package com.hellowo.colosseum.ui.activity

import android.Manifest
import android.R.menu
import android.arch.lifecycle.ViewModelProviders
import android.media.*
import java.io.File.separator
import android.system.Os.mkdir
import java.nio.file.Files.exists
import android.os.Environment.getExternalStorageDirectory
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.hellowo.colosseum.R
import com.hellowo.colosseum.model.Couple
import com.hellowo.colosseum.viewmodel.FavobalityTestViewModel
import kotlinx.android.synthetic.main.activity_voice.*
import java.io.*
import java.util.*

class VoiceActivity : BaseActivity() {
    private lateinit var viewModel: FavobalityTestViewModel
    private val source = MediaRecorder.AudioSource.MIC
    private val sampleRate = 44100
    private val channel = AudioFormat.CHANNEL_IN_STEREO
    private val format = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, format)
    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FavobalityTestViewModel::class.java)
        setContentView(R.layout.activity_voice)
        initLayout()
        initObserve()
        viewModel.initCouple(intent.getSerializableExtra("couple") as Couple)
    }

    private fun initLayout() {
        val outputDir = cacheDir // context being the Activity pointer
        val voiceStoragePath = File.createTempFile("prefix", "extension", outputDir).path

        recordingButton!!.setOnClickListener{
            checkAudioRecordPermission()
        }

        playButton.setOnClickListener{
            play()
        }
    }

    private fun play() {
        val track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channel, format, bufferSize, AudioTrack.MODE_STREAM)
        var isPlaying = false
        val thread = Thread{
            val writeData = ByteArray(bufferSize)
            Log.d("aaa", "filePath size is " + filePath)
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
                    Log.d("aaa", "read bytes is " + ret)
                    if (ret <= 0) {
                        runOnUiThread{}
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
        }
        stopButton.setOnClickListener{ isPlaying = false }
        isPlaying = true
        thread.start()
    }

    private fun initObserve() {


    }


    private val permissionlistener = object : PermissionListener {
        override fun onPermissionGranted() {
            startRecording()
        }
        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
    }

    private fun checkAudioRecordPermission() {
        TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.RECORD_AUDIO)
                .check()
    }

    private fun startRecording() {
        val record = AudioRecord(source, sampleRate, channel, format, bufferSize)
        var isRecording = false
        val thread = Thread{
            val readData = ByteArray(bufferSize)
            val outputDir = cacheDir // context being the Activity pointer
            filePath = File.createTempFile("voice", "", outputDir).absolutePath
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(filePath)
                Log.d("aaa", "filePath is " + filePath)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            while (isRecording) {
                val ret = record.read(readData, 0, bufferSize)  //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
                Log.d("aaa", "read bytes is " + ret)

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
        stopButton.setOnClickListener{ isRecording = false }
        isRecording = true
        record.startRecording()
        thread.start()
    }

}