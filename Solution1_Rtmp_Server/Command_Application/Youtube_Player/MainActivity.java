package com.example.youtube;

import android.os.Bundle;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.youtube.player.YouTubeBaseActivity;

public class MainActivity extends YouTubeBaseActivity {
    //객체 선언
    YouTubePlayerView playerView;
    YouTubePlayer player;
    //유튜브 API KEY와 동영상 ID 변수 설정
    private static String API_KEY = "AIzaSyDCf1GyMAoD_neKe1UtoMDvCuktkbhumEM";
    //https://www.youtube.com/watch?v=hl-ii7W4ITg ▶ 유튜브 동영상 v= 다음 부분이 videoId
    private static String videoId = "AlK2Gl6kHZI";
    //logcat 사용 설정
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPlayer();
        // 동영상 뒤로 점프
        Button btnRewind = findViewById(R.id.rewindBtn);
        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setJump(-10000);
            }
        });


        // 동영상 재생
        Button btnPlay = findViewById(R.id.playBtn);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
            }
        });
        // 동영상 정지
        Button btnPause = findViewById(R.id.pauseBtn);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseVideo();
            }
        });

        // 동영상 앞으로 점프
        Button btnAdvance = findViewById(R.id.advanceBtn);
        btnAdvance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setJump(10000);
            }
        });

    }
    // 동영상 재생
    private void playVideo() {
        if(player != null) {
            if(player.getCurrentTimeMillis() == 0) {
                player.cueVideo(videoId);
            }
            player.play();
        }
    }
    // 동영상 정지
    private void pauseVideo() {
        player.pause();
    }
    // 동영상 점프
    private void setJump(int sec) {
        int current = player.getCurrentTimeMillis();
        player.loadVideo(videoId, current+sec);
        }

        //유튜브 플레이어 메서드
        private void initPlayer () {
            playerView = findViewById(R.id.youTubePlayerView);
            playerView.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    player = youTubePlayer;
                    player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                        @Override
                        public void onLoading() {
                        }

                        @Override
                        public void onLoaded(String id) {
                            Log.d(TAG, "onLoaded: " + id);
                            player.play();
                        }

                        @Override
                        public void onAdStarted() {
                        }

                        @Override
                        public void onVideoStarted() {
                        }

                        @Override
                        public void onVideoEnded() {
                        }

                        @Override
                        public void onError(YouTubePlayer.ErrorReason errorReason) {
                            Log.d(TAG, "onError: " + errorReason);
                        }
                    });
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                }
            });
        }
    }
