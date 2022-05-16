from socket import *
import threading
import time

def receive(sock):
    while True:
        recvData = sock.recv(1024)
        if "재생" in str(recvData.decode('utf-8)')):
            print('재생하겠습니다')
            continue
        if "정지" in str(recvData.decode('utf-8)')):
            print('정지하겠습니다')
            continue
        if "앞" in str(recvData.decode('utf-8)')):
            print('앞으로 이동 하겠습니다')
            continue
        if "뒤" in str(recvData.decode('utf-8)')):
            print('뒤로 이동 하겠습니다')
            continue
            if re.search(r'\b(exit|quit)\b', transcript, re.I):
                print('Exiting..')
                break
port = 8081

serverSock = socket(AF_INET, SOCK_STREAM)
serverSock.bind(('', port))
serverSock.listen(1)

print('%d번 포트로 접속 대기중...'%port)

connectionSock, addr = serverSock.accept()

print(str(addr), '에서 접속되었습니다.')


receiver = threading.Thread(target=receive, args=(connectionSock,))
receiver.start()


while True:
    time.sleep(3)
    pass