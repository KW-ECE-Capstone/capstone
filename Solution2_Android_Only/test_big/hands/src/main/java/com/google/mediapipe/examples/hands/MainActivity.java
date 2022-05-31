package com.google.mediapipe.examples.hands;



import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

// ContentResolver dependency

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.mediapipe.formats.proto.LandmarkProto.Landmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutioncore.VideoInput;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;


/** Main activity of MediaPipe Hands app. */
public class MainActivity extends AppCompatActivity {
  YouTubePlayerView youTubePlayerView;
  String videoId;
  private static final String TAG = "MainActivity";
  Button btn_yStart;
  private Hands hands;
  Intent intent;
  SpeechRecognizer mRecognizer;
  final int PERMISSION = 1;
  String Voice_Result;
  // Run the pipeline and the model inference on GPU or CPU.
  private static final boolean RUN_ON_GPU = true;

  private enum InputSource {
    UNKNOWN,
    IMAGE,
    VIDEO,
    CAMERA,
  }
  private InputSource inputSource = InputSource.UNKNOWN;

  // Image demo UI and image loader components.


  // Video demo UI and video loader components.
  private VideoInput videoInput;

  // Live camera demo UI and camera components.
  private CameraInput cameraInput;

  private SolutionGlSurfaceView<HandsResult> glSurfaceView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    if(Build.VERSION.SDK_INT >= 23){
      ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET,
              Manifest.permission.RECORD_AUDIO},PERMISSION);
      if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)){
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_SAME,0);
      }
    }
    else{
      mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
    }





    intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName()); // 여분의 키
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
    youTubePlayerView = findViewById(R.id.youtube_player_view);
    getLifecycle().addObserver(youTubePlayerView);
//    mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this); // 새 SpeechRecognizer 를 만드는 팩토리 메서드
//    mRecognizer.setRecognitionListener(listener); // 리스너 설정
//    listener.onBeginningOfSpeech();


    Intent gt = getIntent();
    videoId = gt.getStringExtra("id");





    btn_yStart = findViewById(R.id.btn_yStart);
//    fullScreenButton = findViewById(R.id.full_screen);

    setupLiveDemoUiComponents();
    //-------------------------------------유튜브---------------------------------


    //-------------------------------------유튜브---------------------------------
    btn_yStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        btn_yStart.setVisibility(View.GONE);
        youTubePlayerView.setVisibility(View.VISIBLE);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
          @Override
          public void onReady(@NonNull YouTubePlayer youTubePlayer) {

            youTubePlayer.loadVideo(videoId,0);
            //파라미터는 videoId와 startTime이다.
          }
        });
      }
    });










  }


  public boolean onCreateOptionsMenu(Menu menu){
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item){

    switch(item.getItemId()){
      case R.id.Btn_Camera:
        setupLiveDemoUiComponents();
        return true;
      case R.id.Btn_CameraOff:
        glSurfaceView.setVisibility(View.INVISIBLE);
        return true;
      case R.id.Big_Screen:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        glSurfaceView.setVisibility(View.INVISIBLE);
        return true;
      case R.id.Small_Screen:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return true;
      case R.id.Btn_Voice:
//        mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this); // 새 SpeechRecognizer 를 만드는 팩토리 메서드
//        mRecognizer.setRecognitionListener(listener); // 리스너 설정
//        mRecognizer.startListening(intent);
        return true;


    }
    return false;
  }

  @Override
  protected void onResume() {
    super.onResume();


    if (inputSource == InputSource.CAMERA) {
      // Restarts the camera and the opengl surface rendering.
      cameraInput = new CameraInput(this);
      cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
      glSurfaceView.post(this::startCamera);
      glSurfaceView.setVisibility(View.INVISIBLE);
    } else if (inputSource == InputSource.VIDEO) {
      videoInput.resume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (inputSource == InputSource.CAMERA) {
      glSurfaceView.setVisibility(View.GONE);
      cameraInput.close();
    } else if (inputSource == InputSource.VIDEO) {
      videoInput.pause();
    }
  }









  /** Sets up the UI components for the live demo with camera input. */
  public void setupLiveDemoUiComponents() {
//    startCameraButton = findViewById(R.id.button_start_camera);
//
//    startCameraButton.setOnClickListener(
//            v -> {
//              if (inputSource == InputSource.CAMERA) {
//                glSurfaceView.setVisibility(View.VISIBLE);
//                return;
//              }
//              //stopCurrentPipeline();
//              setupStreamingModePipeline(InputSource.CAMERA);
//            });
    if (inputSource == InputSource.CAMERA) {
      glSurfaceView.setVisibility(View.VISIBLE);
      return;
    }
    //stopCurrentPipeline();
    setupStreamingModePipeline(InputSource.CAMERA);

  }

  /** Sets up core workflow for streaming mode. */
  private void setupStreamingModePipeline(InputSource inputSource) {
    this.inputSource = inputSource;
    // Initializes a new MediaPipe Hands solution instance in the streaming mode.
    hands =
            new Hands(
                    this,
                    HandsOptions.builder()
                            .setStaticImageMode(false)
                            .setMaxNumHands(1)
                            .setRunOnGpu(RUN_ON_GPU)
                            .setModelComplexity(0)
                            .setMinTrackingConfidence((float)0.1)
                            .build());
    hands.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));

    if (inputSource == InputSource.CAMERA) {
      cameraInput = new CameraInput(this);
      cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
    } else if (inputSource == InputSource.VIDEO) {
      videoInput = new VideoInput(this);
      videoInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
    }

    // Initializes a new Gl surface view with a user-defined HandsResultGlRenderer.
    glSurfaceView =
            new SolutionGlSurfaceView<>(this, hands.getGlContext(), hands.getGlMajorVersion());
    glSurfaceView.setSolutionResultRenderer(new HandsResultGlRenderer());
    glSurfaceView.setRenderInputImage(true);
    hands.setResultListener(
            handsResult -> {
              logWristLandmark(handsResult, /*showPixelValues=*/ false);
              glSurfaceView.setRenderData(handsResult);
              glSurfaceView.requestRender();
            });

    // The runnable to start camera after the gl surface view is attached.
    // For video input source, videoInput.start() will be called when the video uri is available.
    if (inputSource == InputSource.CAMERA) {
      glSurfaceView.post(this::startCamera);
    }

    // Updates the preview layout.
    FrameLayout frameLayout = findViewById(R.id.preview_display_layout);
    frameLayout.removeAllViewsInLayout();
    frameLayout.addView(glSurfaceView);
    glSurfaceView.setVisibility(View.VISIBLE);

    frameLayout.requestLayout();
  }

  private void startCamera() {
    cameraInput.start(
            this,
            hands.getGlContext(),
            CameraInput.CameraFacing.FRONT,
            glSurfaceView.getWidth(),
            glSurfaceView.getHeight());
  }

  private void stopCurrentPipeline() {
    if (cameraInput != null) {
      cameraInput.setNewFrameListener(null);
      cameraInput.close();
    }
    if (videoInput != null) {
      videoInput.setNewFrameListener(null);
      videoInput.close();
    }
    if (glSurfaceView != null) {
      glSurfaceView.setVisibility(View.GONE);
    }
    if (hands != null) {
      hands.close();
    }
  }

  private void logWristLandmark(HandsResult result, boolean showPixelValues) {
    if (result.multiHandLandmarks().isEmpty()) {
      return;
    }
    NormalizedLandmark wristLandmark =
            result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);
    // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
    if (showPixelValues) {
      int width = result.inputBitmap().getWidth();
      int height = result.inputBitmap().getHeight();
      Log.i(
              TAG,
              String.format(
                      "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                      wristLandmark.getX() * width, wristLandmark.getY() * height));
    } else {
      Log.i(
              TAG,
              String.format(
                      "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f, z=%f",
                      wristLandmark.getX(), wristLandmark.getY(), wristLandmark.getZ()));       // 220504: getZ 추가(조시언)
    }
    if (result.multiHandWorldLandmarks().isEmpty()) {
      return;
    }
    Landmark wristWorldLandmark =
            result.multiHandWorldLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);
    Log.i(
            TAG,
            String.format(
                    "MediaPipe Hand wrist world coordinates (in meters with the origin at the hand's"
                            + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                    wristWorldLandmark.getX(), wristWorldLandmark.getY(), wristWorldLandmark.getZ()));
  }



  //---------------------------------------------음성 인식-----------------------------------------------------------------
//  private RecognitionListener listener = new RecognitionListener() {
//    @Override
//    public void onReadyForSpeech(Bundle params) {
//      // 말하기 시작할 준비가되면 호출
//      Toast.makeText(getApplicationContext(),"음성인식 시작",Toast.LENGTH_SHORT).show();
//      Log.d("tst5", "시작");
//    }
//
//    @Override
//    public void onBeginningOfSpeech() {
//      // 말하기 시작했을 때 호출
//    }
//
//    @Override
//    public void onRmsChanged(float rmsdB) {
//      // 입력받는 소리의 크기를 알려줌
//    }
//
//    @Override
//    public void onBufferReceived(byte[] buffer) {
//      // 말을 시작하고 인식이 된 단어를 buffer에 담음
//    }
//
//    @Override
//    public void onEndOfSpeech() {
//      // 말하기를 중지하면 호출
//    }
//
//    @Override
//    public void onError(int error) {
//      // 네트워크 또는 인식 오류가 발생했을 때 호출
//      String message;
//
//      switch (error) {
//        case SpeechRecognizer.ERROR_AUDIO:
//          message = "오디오 에러";
//          break;
//        case SpeechRecognizer.ERROR_CLIENT:
//          message = "클라이언트 에러";
//          break;
//        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
//          message = "퍼미션 없음";
//          break;
//        case SpeechRecognizer.ERROR_NETWORK:
//          message = "네트워크 에러";
//          break;
//        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
//          message = "네트웍 타임아웃";
//          break;
//        case SpeechRecognizer.ERROR_NO_MATCH:
//          message = "찾을 수 없음";
//          break;
//        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
//          message = "RECOGNIZER 가 바쁨";
//          break;
//        case SpeechRecognizer.ERROR_SERVER:
//          message = "서버가 이상함";
//          break;
//        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
//          message = "말하는 시간초과";
//          break;
//        default:
//          message = "알 수 없는 오류임";
//          break;
//      }
//
//      Toast.makeText(getApplicationContext(), "에러 발생 : " + message,Toast.LENGTH_SHORT).show();
//      Log.d("tst5", "onError: "+message);
//    }
//
//    @Override
//    public void onResults(Bundle results) {
//
//      // 인식 결과가 준비되면 호출
//      // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
//      ArrayList<String> matches =
//              results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//      Voice_Result = matches.get(0);
//      if(Voice_Result.contains("재생")){
//        Toast.makeText(getApplicationContext(), "재생", Toast.LENGTH_SHORT).show();
//      }
//      if(Voice_Result.contains("정지")){
//        Toast.makeText(getApplicationContext(), "정지", Toast.LENGTH_SHORT).show();
//      }
//      if(Voice_Result.contains("앞")){
//        Toast.makeText(getApplicationContext(), "앞", Toast.LENGTH_SHORT).show();
//      }
//      if(Voice_Result.contains("뒤")){
//        Toast.makeText(getApplicationContext(), "뒤", Toast.LENGTH_SHORT).show();
//      }
//      if(Voice_Result.contains("반속")){
//        Toast.makeText(getApplicationContext(), "반속", Toast.LENGTH_SHORT).show();
//      }
//      if(Voice_Result.contains("정속")){
//        Toast.makeText(getApplicationContext(), "정속", Toast.LENGTH_SHORT).show();
//      }
//      if(Voice_Result.contains("배속")){
//        Toast.makeText(getApplicationContext(), "배속", Toast.LENGTH_SHORT).show();
//      }
//      youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer ->
//      {
//
//        // 정지 명령어
//        if (Voice_Result.contains("정지")) {
//          youTubePlayer.pause();
//        }
//
//        // 재생 명령어
//        else if (Voice_Result.contains("재생")) {
//          youTubePlayer.play();
//        }
//
//        else if (Voice_Result.contains("반속")){
//          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_0_5);
//        }
//
//        else if (Voice_Result.contains("정속")){
//          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1);
//        }
//
//        else if (Voice_Result.contains("배속")){
//          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_2);
//        }
//
//      });
//
//    }
//
//
//    @Override
//    public void onPartialResults(Bundle partialResults) {
//      // 부분 인식 결과를 사용할 수 있을 때 호출
//    }
//
//    @Override
//    public void onEvent(int eventType, Bundle params) {
//      // 향후 이벤트를 추가하기 위해 예약
//    }
//  };

}