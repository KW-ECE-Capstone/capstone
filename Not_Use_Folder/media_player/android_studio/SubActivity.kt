package com.example.capstone

import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.databinding.ActivitySubBinding
import kotlinx.android.synthetic.main.activity_sub.*
import java.io.DataInputStream
import java.net.Socket

var real_data = 0
var playerPosition = 0

class SubActivity : AppCompatActivity() {
    inner class NetworkThread : Thread() {
        override fun run() {
            try {
                var socket = Socket("223.194.7.87", 44444)
                while (true) {

                    var input = socket.getInputStream()
                    var dis = DataInputStream(input)
                    var data1 = dis.readInt()
                    real_data = data1 / 16777216
                    if (real_data == 1){
                        videoView.pause()
                        binding.textView2.text = "정지"
                    }
                    else if (real_data == 2){
                        videoView.start()
                        binding.textView2.text = "시작"
                    }
                    else if (real_data == 3){
                        videoView.pause()
                        playerPosition = videoView.currentPosition - 10000
                        videoView.seekTo(playerPosition)
                        videoView.start() // 동영상 -10초 이동
                        binding.textView2.text = "10초 전으로 이동"
                    }
                    else if (real_data == 4){
                        videoView.pause()
                        playerPosition = videoView.currentPosition + 10000
                        videoView.seekTo(playerPosition)
                        videoView.start() // 동영상 +10초 이동
                        binding.textView2.text = "10초 후로 이동"
                    }
                    else if (real_data == 99) {
                        binding.textView2.text = "서버 연결이 끊겼습니다."
                        break
                    }
                    else {
                        binding.textView2.text = "모션을 잘 인식하지 못하였습니다."
                    }

                    runOnUiThread {
                        binding.textView.text = "data1 : ${real_data}\n"
                    }
                }
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
    private val VIDEO_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private lateinit var binding: ActivitySubBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var thread = NetworkThread()
        thread.start()
        // 에뮬레이터로 확인하려면 내 프로젝트에 동영상 파일이 있어야 됨
        val VIDEO_PATH = "android.resource://" + packageName + "/" + R.raw.test
        var uri: Uri = Uri.parse(VIDEO_PATH)
        videoView.setVideoURI(uri)
        val mc = MediaController(this)
        videoView.setMediaController(mc) // 없으면 에러

        videoView.requestFocus() // 준비하는 과정을 미리함
        videoView.setOnPreparedListener {
            Toast.makeText(applicationContext, "동영상 재생 준비 완료", Toast.LENGTH_SHORT).show()
            videoView.pause() // 동영상 재개 }
        }
        videoView.setOnCompletionListener {
            Toast.makeText(applicationContext, "동영상 시청 완료", Toast.LENGTH_SHORT).show()
        }
        btnStart.setOnClickListener {
            videoView.start() // 동영상 재개
            mc.show(0)
        }
        btnPause.setOnClickListener {
            videoView.pause() // 동영상 일시정지 (Start 버튼 클릭하면 재개)
        }
        btnRewind.setOnClickListener {
            videoView.pause()
            playerPosition = videoView.currentPosition - 10000
            videoView.seekTo(playerPosition)
            videoView.start() // 동영상 -10초 이동
        }
        btnAdvance.setOnClickListener {
            videoView.pause()
            playerPosition = videoView.currentPosition + 10000
            videoView.seekTo(playerPosition)
            videoView.start() // 동영상 +10초 이동
        }
    }
}