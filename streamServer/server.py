import urllib2
import subprocess as sp
import os, tempfile
from threading import Thread
import time
import socket

socket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
socket.bind(("140.113.214.87",8888))
socket.listen(10)

connection,address = socket.accept()
pipe = sp.Popen(['./ffmpeg',
                '-i','test.mp4',
                '-i','/dev/stdin',
                '-filter_complex','amix=inputs=2:duration=first',
                '-map','0',
                '-c:v','libx264',
                '-c:a','aac',
                '-strict','-2',
                '-f','segment',
                '-flags','-global_header',
                '-segment_format','mpegts',
		'-segment_list_flags','live',
                '-segment_list','list.m3u8','output-%03d.ts'],stdin=sp.PIPE)

while True:
    buf = connection.recv(1024)
    if len(buf) ==0:
        break
    pipe.stdin.write(buf)

pipe.stdin.close()


