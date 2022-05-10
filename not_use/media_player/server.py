# -*- coding:utf-8 -*-

import socket


host = '0.0.0.0'
port = 8800

server_sock = socket.socket(socket.AF_INET)
server_sock.bind((host, port))
server_sock.listen(1)

print("wait...")
client_sock, addr = server_sock.accept()

print('Connected by', addr)



while (True):
     data2 = int(input("sending : "))

     client_sock.send(data2.to_bytes(4, byteorder='little'))

     if (data2 == 99):
         client_sock.send(data2.to_bytes(4, byteorder='little'))
         break;


client_sock.close()
server_sock.close()

