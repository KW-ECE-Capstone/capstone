package com.example.test_mediapipe_tensor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

import org.tensorflow.lite.*;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean RUN_ON_GPU = true;     //false로 하면 SIGSEGV, SEGV_MAPERR 에러 발생
    //위의 에러는 inputstream이 close되었는데 JNI에서 read하려다 포인터가 null이라서 생기는 에러

    private enum InputSource{
        UNKNOWN,
        CAMERA
    }

    private Hands hands;
    private InputSource inputSource = InputSource.UNKNOWN;
    private CameraInput cameraInput;
    private SolutionGlSurfaceView<HandsResult> glSurfaceView;
    private TextView Date;
    private TextView PrintResult;
    private ProgressHandler handler;
    private Interpreter tflite; // instance of the driver class to run model inference with Tensorflow Lite
    String time;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //double[] inputs = {0.473972,0.422807,0.549512,0.486547,0.591408,0.569618,0.632841,0.621523,0.687649,0.670258,0.472084,0.654611,0.540619,0.752105,0.617385,0.780825,0.673016,0.784885,0.466915,0.627833,0.529033,0.756932,0.622783,0.7987,0.696129,0.820382,0.489161,0.589503,0.565776,0.696462,0.658659,0.737037,0.72946,0.763009,0.529973,0.549646,0.622391,0.586593,0.684666,0.588849,0.734648,0.590322};
    ByteBuffer inputs;
    float[][] outputs = new float[1][4];


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Date = (TextView) findViewById(R.id.Date);
        PrintResult = (TextView) findViewById(R.id.Print_result);
        handler = new ProgressHandler();
        tflite = ModelModule.getTfliteInterpreter(this, "keras_model_1653398839.tflite", Boolean.FALSE);
        runTime();
        setupLiveDemoUiComponents();
    }

    public void runTime() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        time = sdf.format(new Date(System.currentTimeMillis()));
                        Message message = handler.obtainMessage();
                        handler.sendMessage(message);
                        Thread.sleep(1000);
                    }catch (InterruptedException e) {}
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(inputSource == InputSource.CAMERA) {
            cameraInput = new CameraInput(this);
            cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
            glSurfaceView.post(this::startCamera);
            glSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(inputSource == InputSource.CAMERA) {
            glSurfaceView.setVisibility(View.GONE);
            cameraInput.close();
        }
    }

    private void setupLiveDemoUiComponents(){
        Button gatherDataButton = findViewById(R.id.button_gather_data);
        gatherDataButton.setOnClickListener(
                v -> {
                    if(inputSource == InputSource.CAMERA){
                        return;
                    }
                    stopCurrentPipeline();
                    setupStreamingModePipeline(InputSource.CAMERA);
                }
        );
    }

    private void setupStreamingModePipeline(InputSource inputSource){
        this.inputSource = inputSource;
        hands =
                new Hands(
                        this,
                        HandsOptions.builder()
                                .setStaticImageMode(false)
                                .setMaxNumHands(1)
                                .setRunOnGpu(RUN_ON_GPU)
                                .setModelComplexity(0)
                                .setMinDetectionConfidence((float)0.3)
                                .setMinTrackingConfidence((float)0.3)
                                .build()
                );
        hands.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));
        if(inputSource == InputSource.CAMERA){
            cameraInput = new CameraInput(this);
            cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
        }
        glSurfaceView =
                new SolutionGlSurfaceView<>(this, hands.getGlContext(), hands.getGlMajorVersion());
        glSurfaceView.setSolutionResultRenderer(new HandsResultGlRenderer());
        glSurfaceView.setRenderInputImage(true);
        hands.setResultListener(
                handsResult -> {
                    logLandmark(handsResult);
                    glSurfaceView.setRenderData(handsResult);
                    glSurfaceView.requestRender();
                }
        );
        if(inputSource == InputSource.CAMERA){
            glSurfaceView.post(this::startCamera);
        }
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
                glSurfaceView.getHeight()
        );
    }

    private void logLandmark(HandsResult result) {
        if(result.multiHandLandmarks().isEmpty()){
            return;
        }
        int i = 0;
        int j = 0;
        double[] arr = new double[42];
        StringBuilder sb = new StringBuilder();
        List<LandmarkProto.NormalizedLandmarkList> landmark = result.multiHandLandmarks();
        for(LandmarkProto.NormalizedLandmark ls : landmark.get(i).getLandmarkList()){
            arr[j++] = ls.getX();
            arr[j++] = ls.getY();
            i += 1;
        }
        inputs = (ByteBuffer) preprocessing_input(arr);
        //Float[] outputs = new Float[1];
        //tflite.run(arr, outputs);
        //double[] inputs = {4.74E-01,4.23E-01,5.50E-01,4.87E-01,5.91E-01,5.70E-01,6.33E-01,6.22E-01,6.88E-01,6.70E-01,4.72E-01,6.55E-01,5.41E-01,7.52E-01,6.17E-01,7.81E-01,6.73E-01,7.85E-01,4.67E-01,6.28E-01,5.29E-01,7.57E-01,6.23E-01,7.99E-01,6.96E-01,8.20E-01,4.89E-01,5.90E-01,5.66E-01,6.96E-01,6.59E-01,7.37E-01,7.29E-01,7.63E-01,5.30E-01,5.50E-01,6.22E-01,5.87E-01,6.85E-01,5.89E-01,7.35E-01,5.90E-01};
        //tflite.run(inputs, outputs);
        //PrintResult.setText(Float.toString(outputs[0]));
        /*Log.i(
                TAG,
                String.format(
                        "Vector Data %s",
                        Arrays.toString(arr)
                )
        );*/
        if(inputs != null)
            tflite.run(inputs, outputs);
        Log.i(
                TAG,
                String.format(
                        "Vector Output %s",
                        Arrays.deepToString(outputs)
                )
        );
        //PrintResult.setText(Float.toString(outputs[0][0]));
    }

    private void stopCurrentPipeline() {
        if(cameraInput != null) {
            cameraInput.setNewFrameListener(null);
            cameraInput.close();
        }
        if(glSurfaceView != null) {
            glSurfaceView.setVisibility(View.GONE);
        }
        if(hands != null) {
            hands.close();
        }
    }

    class ProgressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Date.setText(time);
            // index 0의 경우
            //double[] tmp = {0.473972,0.422807,0.549512,0.486547,0.591408,0.569618,0.632841,0.621523,0.687649,0.670258,0.472084,0.654611,0.540619,0.752105,0.617385,0.780825,0.673016,0.784885,0.466915,0.627833,0.529033,0.756932,0.622783,0.7987,0.696129,0.820382,0.489161,0.589503,0.565776,0.696462,0.658659,0.737037,0.72946,0.763009,0.529973,0.549646,0.622391,0.586593,0.684666,0.588849,0.734648,0.590322};
            // index 1의 경우
            //double[] tmp ={0.783186,0.351997,0.730444,0.459442,0.646889,0.501663,0.567789,0.514229,0.499711,0.533322,0.543944,0.449756,0.454406,0.463253,0.398976,0.469086,0.349661,0.47305,0.535644,0.393431,0.432021,0.399304,0.363739,0.401959,0.308562,0.404308,0.545553,0.342659,0.448248,0.333167,0.385545,0.327946,0.332525,0.327186,0.567711,0.294966,0.495757,0.280411,0.449189,0.277246,0.404365,0.279454};
            //inputs = (ByteBuffer) preprocessing_input(tmp);
            //if(inputs != null) {
            //    tflite.run(inputs, outputs);
            //}
            //PrintResult.setText(Arrays.deepToString(outputs));
        }
    }

    public ByteBuffer preprocessing_input(double[] input){
        ByteBuffer to_return = ByteBuffer.allocateDirect(4 * input.length);
        to_return.order(ByteOrder.nativeOrder());
        for(int i=0; i<input.length; i++){
            to_return.putFloat((float)input[i]);
        }
        return to_return;
    }

    /*class UsedAsync extends AsyncTask<Integer, Integer, Integer> {
        Calendar cal;
        String timeGre;

        @Override
        protected Integer doInBackground(Integer... params){
            while(isCancelled() == false) {
                cal = new GregorianCalendar();
                timeGre = String.format(
                        "%d/%d/%d %d:%d:%d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.HOUR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.SECOND)
                );
                publishProgress();
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){}
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            cal = new GregorianCalendar();
            timeGre = String.format(
                    "%d/%d/%d %d:%d:%d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.SECOND)
            );
            Gre.setText(timeGre);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer){
            super.onPostExecute(integer);
        }

        @Override
        protected void onProgressUpdate(Integer ... values){
            Gre.setText(timeGre);
            super.onProgressUpdate(values);
        }
    }*/
}