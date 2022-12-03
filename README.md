# 광운대학교 전자통신공학과 캡스톤 프로젝트

![03_03_문장_로고타입_조합_국영문_가로_1Color_typeB](https://user-images.githubusercontent.com/88064555/160678993-70372853-5ca5-42bf-85a6-de9f68d5f888.jpg)

----------
## 프로젝트 목적

+ Why: 레시피 영상이 요리 초보의 진도에 맞춰 재생되게 하고 싶다.
+ What: 레시피 영상이 요리 초보의 진도에 맞춰 재생되게 하는 시스템
+ How: 안드로이드 어플리케이션
---

# Android Studio 모션 인식을 이용한 영상 제어 시스템

<p aligh="center">
    <img src="https://user-images.githubusercontent.com/95459089/202703971-2c0ac543-a56e-4604-a9a4-ba9ec73fd3c6.gif">
</p>


## ***Introduction*** ✔

+ Why: 레시피 영상이 요리 초보의 진도에 맞춰 재생되게 하고 싶다.
+ What: 레시피 영상이 요리 초보의 진도에 맞춰 재생되게 하는 시스템
+ How: 안드로이드 어플리케이션

### ***Block Diagram*** 🏳

<img width="864" alt="스크린샷 2022-03-30 오전 3 32 08" src="https://user-images.githubusercontent.com/88064555/160681059-60287651-0453-441f-8509-bf327c3f328f.png">

### ***Summary*** 🔽 

- Project 소개
    - ViCon (모션 인식을 기반한 영상 제어 시스템)
    - Youtube 영상 크롤링
    - 제스처 분류 API 구현
    - 영상 제어 API 구현
    - Youtube 영상과 안드로이드 내장 카메라 concurrent execution
    - 음성 분류 API 구현
    
- BACKEND
    - Mediapipe(안드로이드)를 활용한 안드로이드 카메라 입력 데이터화
    - TensorFlow Lite를 활용하기 위해 데스크탑 환경에서 학습시킨 데이터를 tflite 모델로 변환
    - YouTube API를 이용한 영상 제어 함수 커스터마이징
    - 학습된 데이터를 기반으로 클래스 분류 API
    - 음성 인식 API를 활용한 클래스 분류 API
    
- FRONTEND
    - YouTube 크롤링으로 받아온 JSON 객체 이미지화
    - 분류된 클래스로 실행한 함수 시각화
    - 영상과 카메라 concurrent execution

### ***Old Version*** 💿

 처음에는 다양한 레퍼런스들을 참고하여 서버를 가지는 구조로 설계하였다.

모바일 기기로부터 카메라 입력 → 서버에서 클래스 분류 후 기기로 결과값 전송 → 기기에서 처리하여 동작 수행

 서버에서 수행하는 동작이 Tensorflow를 이용하고, 이를 위해 우선적으로 머신러닝 모델을 학습시켜야 한다. 영상을 촬영하여 클래스별로 데이터 생성 및 수집을 하였다. 영상데이터를 처리하는 파이프라인 모듈로는 Mediapipe를 사용하였다. 데이터를 토대로 데이터셋을 생성 시계열 모델을 생성하였으나, 여러 시도에도 한계점 발생하였다. 이는 시행착오에서 서술하겠다. 이후, 시계열 모델이 아닌 이미지 분류 모델로 변경하였다.

 위와 같이 서버를 구성하고, 모바일 기기와 서버간 통신에는 소켓과 Wowza서버를 사용하였다. 영상통신에는 RTSP, RTMP, SRT, NDI 등 다양한 프로토콜이 있으나 다른 조원이 담당했던 부분으로 프로젝트 기간과 비용을 고려했을때 Wowza서버를 이용한 RTMP가 적절하다고 전달받았다.

 최종적으로, 모바일 기기에서 Wowza서버로 스트리밍 → 서버에서는 Wowza서버로부터 영상을 받아 클래스 분류 후 소켓 통신을 통해 기기로 결과값 전송 → 기기에서 처리의 구조로 작동되었다.

### ***New Version*** 🔨

기존 시스템의 딜레이 문제의 원인을 통신으로 생각하여, 안드로이드 Stand-alone으로 시스템 구조 변경을 시도하였다. 따라서, 기존 파이썬 서버에서 작동하던 Tensorflow와 Mediapipe를 안드로이드 버전으로 바꾸고 리팩토링을 진행하였다. Tensorflow는 Tensorflow Lite라는 버전을 사용하였다. Tensorflow Lite는 모델 학습은 불가하고, 클래스 분류 및 예측만 가능하며 학습의 경우에는 데스크탑 환경에서 학습을 한 뒤 tflite라는 모델로 변환하는 과정이 필요하다. Mediapipe도 안드로이드 솔루션을 이용해 진행하였다. 그러나, 기기간의 성능 편차가 큰 점, 적은 프로젝트 예산으로 인한 낮은 모바일 기기 성능, 그리고 리뉴얼의 목표가 딜레이의 최소화였기에 configuration을 저성능의 기기에 맞추어 설정하였다.

 미리 설정한 제스처 동작을 이용해 모델을 학습, tflite모델로 변환하여 애플리케이션 패키지에 포함시켰다. 시스템에서는 카메라 입력을 Mediapipe를 통해 데이터화시키고 이 데이터를 tflite모델에 입력, 클래스 출력값에 따라 동작을 수행하도록 하였다. 딜레이가 1.5초내로 실시간 수준의 성능을 보였다.


### ***조원 및 역할*** 🤔

+ 김진만 - 음성 인식부
+ 이정연 - 카메라/마이크, 명령어 분류부/적용부
+ 조시언 - 제스쳐 인식부
+ 최동혁 - 음성/제스쳐 통신부

### ***IDE*** 🥢
- BACKEND
    - VScode
    - Android Studio
- FRONTEND
    - VScode
    - Android Studio
 
 ### ***Front Components***
 1️⃣  레이아웃 관련 Components
 
 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | activity_main.xml | /res/ layout/ | 영상,카메라 레이아웃 |
 | item_utube.xml | /res/ layout/ | 크롤링 레이아웃 |
 | youtube_search.xml | /res/ layout/ | 검색 레이아웃 |
 | setup.xml | /res/ layout/ | 도움말 레이아웃 |
 
 2️⃣  애니메이션 Components
 
 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | fade_out.xml | /res/ anim/ | 시작 화면 레이아웃 |

 3️⃣  사이드바, 메뉴 Components
 
 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | menu.xml | /res/ layout/ | 기능 사이드바 |
 | toolbar_menu.xml | /res/ layout/ | 영상 조작 툴바 |

 ### ***Back Components***
 1️⃣  기능 구현 Components
 
 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | MainActivity.java | /hands/main/java/com/google/mediapipe/examples/hands/  | 카메라, 음성 허가, 메인 화면 기능 |
 | Search.java | /hands/main/java/com/google/mediapipe/examples/hands/ | 크롤링 기능 |
 | SeconActivity.java | /hands/main/java/com/google/mediapipe/examples/hands/ | 검색 레이아웃 |
 | Youtube.java | /hands/main/java/com/google/mediapipe/examples/hands// | 영상, 카메라 기능 |
 | Setup.java | /hands/main/java/com/google/mediapipe/examples/hands/ | 도움말 기능 |
 
 2️⃣  학습 데이터 Components
 
 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | keras_model.tflite | /hands/src/main/assets/ | 학습된 데이터셋 |

 3️⃣  환경설정 Components
 
 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | AndroidManifest.xml | /hands/src/main/ | 환경 설정 |
 
 4️⃣  API Components

 | File Name | Directory              | 목적            |
 | --------- | ---------------------- | --------------- |
 | YouTubeAndroidPlayerApi.jar | /hands/libs/ | Youtube API |
 | google_http_client_jackson2-1.0.1.jar | /hands/libs/ | 음성 인식 API |
 
### ***Process*** 🚀

- **시작 화면**

![image](https://user-images.githubusercontent.com/95459089/202745504-51472b87-1172-459a-801b-72bbd1b28326.png)

- **메인 화면**

![image](https://user-images.githubusercontent.com/95459089/202745617-439871ec-6b62-48c6-a197-a89d41fb5504.png)

- **도움말 화면**

![image](https://user-images.githubusercontent.com/95459089/202745690-c8ee5083-5062-4c7e-b5b3-04fc95253e16.png)

- **검색 화면**

![image](https://user-images.githubusercontent.com/95459089/202745743-42212235-e8f9-4213-adf8-abea8db1465a.png)

- **검색 완료 화면**

![image](https://user-images.githubusercontent.com/95459089/202745888-a76caa52-5c2c-49b3-9465-f62926830581.png)

- **비디오 실행 전 화면**

![image](https://user-images.githubusercontent.com/95459089/202745965-37df4bd5-df7b-462b-a273-52d457121b28.png)

- **비디오 실행 후 화면**

![image](https://user-images.githubusercontent.com/95459089/202746012-9aaedaac-a9f2-4890-9b40-56fae34caadc.png)

- **기능 사이드바**

![image](https://user-images.githubusercontent.com/95459089/202746048-7d60d485-52f6-4665-87e4-c62ae2649845.png)

- **도움말 사이드바**

![image](https://user-images.githubusercontent.com/95459089/202746090-d60f30f0-5c28-487d-b53c-b26b8746f3c3.png)




