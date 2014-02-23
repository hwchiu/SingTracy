import socket
import time
import signal
import sys
import os
import commands

sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
#socket.setsockopt(1, 4, 1)
sock.bind(("140.113.214.87",7777))
sock.listen(10)

#f = open ("/var/www/hls/list.m3u8", "r")
#line = f.readline()
#line = f.readline()
#line = f.readline()
#line = f.readline()
#line = f.readline()

while True:
	con,address = sock.accept()
	pid = os.fork()
	if pid:
		con.recv(1024)
		con.send("HTTP/1.1 200 OK\n")
		con.send("Content-type: application/x-mpegURL\n\n")
		print "In\n"
		#while True:
		r, strr = commands.getstatusoutput('grep -v EXT-X list.m3u8 | grep -v EXTM3U | tail -11 | head -1 | cut -d"-" -f 2 | cut -d. -f 1')
		con.send( "#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-MEDIA-SEQUENCE:" + str(int(strr)) +"\n#EXT-X-ALLOW-CACHE:NO\n#EXT-X-TARGETDURATION:2\n")
		r, strr =  commands.getstatusoutput('grep -v EXT-X-ENDLIST /var/www/hls/list.m3u8 | tail -12')
		#con.send(commands.getstatusoutput('grep -v EXT-X-ENDLIST /var/www/hls/list.m3u8 | tail - )
		con.send(strr + "\n")
		#con.send( "#EXTINF:8.408400,\noutput-000.ts\n#EXTINF:2.402400,\noutput-001.ts\n")
			
		con.shutdown(1)
		con.close()
		break
	else:
		pass

