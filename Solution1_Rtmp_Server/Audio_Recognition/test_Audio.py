#Audio+Socket
from __future__ import division
import re
import sys
from google.cloud import speech
from six.moves import queue
import os
from socket import *
import threading
import time
import pyaudio
#cv2
import cv2
import mediapipe as mp
import numpy as np
from tensorflow.keras.models import load_model
os.environ["GOOGLE_APPLICATION_CREDENTIALS"]="C:/Users/진만킴/PycharmProjects/speech/speech-to-text-340806-e7d33decdbe9.json"

# Audio recording parameters
RATE = 8000
CHUNK = int(RATE / 10)  # 100ms
words=["재생","정지","앞","뒤","빠르게","느리게"]
sentences=["재생하겠습니다","정지하겠습니다","앞으로 이동하겠습니다",
           "뒤로 이동하겠습니다.","빠르게 재생하겠습니다.","느리게 재생하겠습니다."]
# CV2 settings
max_num_hands = 1
actions = ['slow', 'rew', 'fast', 'adv', 'prev', 'next', 'stop', 'cmd']
seq_length = 30

model = load_model('models/mode_1648003085.h5')

# MediaPipe hands model
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
hands = mp_hands.Hands(
    max_num_hands=1,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5)

cap = cv2.VideoCapture(0)
seq = []
action_seq = []
cmdmode = 0
input_gesture = None

# Socket Settings
port = 8081
clientSock = socket(AF_INET, SOCK_STREAM)
clientSock.connect(('127.0.0.1', port))

def def_gesture(img):
  img = cv2.flip(img, 1)
  img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
  result = hands.process(img)
  img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
  if result.multi_hand_landmarks is not None:
    for res in result.multi_hand_landmarks:
      joint = np.zeros((21, 3))
      for j, lm in enumerate(res.landmark):
        joint[j] = [lm.x, lm.y, lm.z]
      v1 = joint[[0,1,2,3,0,5,6,7,0,9,10,11,0,13,14,15,0,17,18,19], :]
      v2 = joint[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20], :]
      v = v2 - v1
      v = v / np.linalg.norm(v, axis=1)[:, np.newaxis]
      angle = np.arccos(np.einsum('nt,nt->n',
      v[[0,1,2,4,5,6,8,9,10,12,13,14,16,17,18], :],
      v[[1,2,3,5,6,7,9,10,11,13,14,15,17,18,19], :]))
      angle = np.degrees(angle)
      data = np.array([angle],dtype = np.float32)
      y_pred = model.predict(data)
      i_pred = int(np.argmax(y_pred))
      conf = y_pred[:, i_pred]
      if conf > 0.99:
        return actions[i_pred]
      else:
        return None
class MicrophoneStream:
    """Opens a recording stream as a generator yielding the audio chunks."""
    def __init__(self, rate, chunk):
        self._rate = rate
        self._chunk = chunk

        # Create a thread-safe buffer of audio data
        self._buff = queue.Queue()
        self.closed = True

    def __enter__(self):
        self._audio_interface = pyaudio.PyAudio()
        self._audio_stream = self._audio_interface.open(
            format=pyaudio.paInt16,
            channels=1, rate=self._rate,
            input=True, frames_per_buffer=self._chunk,
            # Run the audio stream asynchronously to fill the buffer object.
            # This is necessary so that the input device's buffer doesn't
            # overflow while the calling thread makes network requests, etc.
            stream_callback=self._fill_buffer,
        )

        self.closed = False

        return self

    def __exit__(self, type, value, traceback):
        self._audio_stream.stop_stream()
        self._audio_stream.close()
        self.closed = True
        # Signal the generator to terminate so that the client's
        # streaming_recognize method will not block the process termination.
        self._buff.put(None)
        self._audio_interface.terminate()

    def _fill_buffer(self, in_data, frame_count, time_info, status_flags):
        """Continuously collect data from the audio stream, into the buffer."""
        self._buff.put(in_data)
        return None, pyaudio.paContinue

    def generator(self):
        while not self.closed:
            # Use a blocking get() to ensure there's at least one chunk of
            # data, and stop iteration if the chunk is None, indicating the
            # end of the audio stream.
            chunk = self._buff.get()
            if chunk is None:
                return
            data = [chunk]

            # Now consume whatever other data's still buffered.
            while True:
                try:
                    chunk = self._buff.get(block=False)
                    if chunk is None:
                        return
                    data.append(chunk)
                except queue.Empty:
                    break

            yield b''.join(data)



def listen_print_loop(responses):
    num_chars_printed = 0


    for response in responses:

        if not response.results:
            continue
        result = response.results[0]
        if not result.alternatives:
            continue
        transcript = result.alternatives[0].transcript

        overwrite_chars = ' ' * (num_chars_printed - len(transcript))

        if not result.is_final:
            sys.stdout.write(transcript + overwrite_chars + '\r')
            sys.stdout.flush()
            num_chars_printed = len(transcript)

        else:
            def send(sock):
                while True:
                    for i in range(6):
                        if words[i] in (transcript + overwrite_chars):
                            sendData = transcript + overwrite_chars
                            sock.send(sendData.encode('utf-8'))
                            time.sleep(1)
                            break
                        else:
                            pass
            handsign = threading.Thread(target=Gesture, args=())
            sender = threading.Thread(target=send, args=(clientSock,))
            handsign.start()
            sender.start()
            print(transcript + overwrite_chars)
            for i in range(6):
                if words[i] in (transcript + overwrite_chars):
                    print(sentences[i])
                    continue
                    if re.search(r'\b(exit|quit)\b', transcript, re.I):
                        print('Exiting..')
                        break

                num_chars_printed = 0
def main():
    language_code = 'ko-KR'  # a BCP-47 language tag

    client = speech.SpeechClient()
    config = speech.RecognitionConfig(
        encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
        sample_rate_hertz=RATE,
        language_code=language_code)
    streaming_config = speech.StreamingRecognitionConfig(
        config=config,
        interim_results=True)


    with MicrophoneStream(RATE, CHUNK) as stream:
        audio_generator = stream.generator()
        requests = (speech.StreamingRecognizeRequest(audio_content=content)
                    for content in audio_generator)

        responses = client.streaming_recognize(streaming_config, requests)

        # Now, put the transcription responses to use.
        listen_print_loop(responses)




if __name__ == '__main__':
    main()