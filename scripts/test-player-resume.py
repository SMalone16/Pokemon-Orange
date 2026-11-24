#!/usr/bin/env python3
"""HTTP join + authenticated WebSocket initialization + same-name recovery."""
import base64
import hashlib
import json
import os
import socket
import struct
import urllib.parse
import urllib.request

HOST, PORT = "127.0.0.1", 4567
BASE = "http://%s:%d" % (HOST, PORT)

def join(**fields):
    data = urllib.parse.urlencode(fields).encode()
    with urllib.request.urlopen(BASE + "/join", data=data, timeout=15) as res:
        return json.load(res)

def initialize(account):
    key = base64.b64encode(os.urandom(16)).decode()
    with socket.create_connection((HOST, PORT), timeout=15) as sock:
        sock.settimeout(15)
        req = ("GET /game HTTP/1.1\r\nHost: %s:%d\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Key: %s\r\nSec-WebSocket-Version: 13\r\n\r\n") % (HOST, PORT, key)
        sock.sendall(req.encode())
        with sock.makefile("rb") as stream:
            assert b" 101 " in stream.readline(), "WebSocket upgrade failed"
            headers = {}
            while True:
                line = stream.readline()
                if line == b"\r\n": break
                if not line: raise EOFError("Upgrade response ended")
                k,v = line.decode().split(":",1)
                headers[k.lower()] = v.strip()
            expected = base64.b64encode(hashlib.sha1((key+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11").encode()).digest()).decode()
            assert headers.get("sec-websocket-accept") == expected
            payload=json.dumps({"type":0,"payload":{"id":account["id"],"token":account["token"]}}).encode()
            mask=os.urandom(4)
            length=len(payload)
            header=bytes([0x81,0x80|length]) if length<126 else bytes([0x81,0x80|126])+struct.pack(">H",length)
            sock.sendall(header+mask+bytes(v^mask[i%4] for i,v in enumerate(payload)))
            def read(n):
                b=stream.read(n)
                if len(b)!=n: raise EOFError("WebSocket ended before initialization")
                return b
            for _ in range(30):
                a,b=read(2)
                opcode=a&15
                length=b&127
                if length==126: length=struct.unpack(">H",read(2))[0]
                if length==127: length=struct.unpack(">Q",read(8))[0]
                data=read(length)
                if opcode==8: raise AssertionError("Server closed before initialization")
                if opcode!=1: continue
                msg=json.loads(data)
                if msg.get("type")==1:
                    assert any(p["id"]==account["id"] for p in msg["payload"]["players"]), "Player missing from initial world"
                    return msg["payload"]
            raise AssertionError("No initialization packet")

def main():
    name="Resume"+os.urandom(3).hex()
    a=join(name=name)
    assert a.get("success"), "New player join failed"
    first=initialize(a)
    assert not join(name=name).get("success"), "Name-only account takeover allowed"
    assert not join(name=name,id=a["id"],token="wrong").get("success"), "Invalid recovery accepted"
    assert not join(name="Other"+os.urandom(3).hex(),id=a["id"],token=a["token"]).get("success"), "Mismatched name accepted"
    b=join(name=name,id=a["id"],token=a["token"])
    assert b.get("success") and a["id"]==b["id"], "Account recovery failed"
    second=initialize(b)
    assert first["location"]==second["location"], "Player location changed on resume"
    print("PASS: HTTP join, authenticated WebSocket initialization, reconnect, name takeover rejection and saved location")
if __name__=="__main__": main()
