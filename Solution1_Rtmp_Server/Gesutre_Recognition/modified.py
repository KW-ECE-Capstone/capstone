import cv2
import mediapipe as mp
import numpy as np
from tensorflow.keras.models import load_model
import time
import socket
max_num_hands = 1
actions = ['rew','adv', 'stop', 'OK']
model = load_model('models/mode_1648573934.h5')
port = 44444
host = '223.194.7.72'
server_sock = socket.socket(socket.AF_INET)
server_sock.bind((host, port))
server_sock.listen(1)
print("기다리는 중")
client_sock, addr = server_sock.accept()
print('Connected by', addr)
#n = int(input())
#if n==1:
#  cap=cv2.VideoCapture("rtmp://223.194.7.87:1935/live/test")

# MediaPipe hands model
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
hands = mp_hands.Hands(
  max_num_hands = max_num_hands,
  min_detection_confidence = 0.5,
  min_tracking_confidence = 0.5
)

cap = cv2.VideoCapture(0)
action_seq = []
cmd_seq = []
cmdmode = 0
input_gesture = None
while cap.isOpened:
  ret, img = cap.read()
  if not ret:
    continue

  if cmdmode == 0:
    action_seq.append(def_gesture(img))
  else:
    cmd_seq.append(def_gesture(img))

  img = cv2.flip(img, 1)
  if cmdmode == 1:
    remain_time = 10 - int(time.time()) + int(entered_time)
    if remain_time < 0:
      cmdmode = 0
    cv2.putText(img,
                text = f'{input_gesture}? Waiting for {remain_time} secs',
                org = (10, 30),
                fontFace = cv2.FONT_HERSHEY_SIMPLEX,
                fontScale = 1,
                color = (0, 0, 0))
  img = cv2.resize(img, (1000, 750))
  cv2.imshow('Game', img)
  if cv2.waitKey(1) == ord('q'):
    break

  if cmdmode == 0:
    if len(action_seq) < 30:
      continue
    if len(set(action_seq[-30:-1])) == 1 and action_seq[-1] != None and action_seq[-1] != 'OK':
      input_gesture = action_seq[-1]
      action_seq.clear()
      cmdmode = 1
      entered_time = time.time()
  else:
    if len(cmd_seq) < 30:
      continue
    if len(set(cmd_seq[-30:-1])) == 1 and cmd_seq[-1] == 'OK':
      print(actions.index(input_gesture))
      client_sock.send(actions.index(input_gesture).to_bytes(4, byteorder='little'))
      cmdmode = 0
      cmd_seq.clear()

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
      if conf > 0.999:
        return actions[i_pred]
      else:
        return None