#!/usr/bin/env python3
"""Create single-frame Phaser atlases for species missing original art.
Use --download-authorized only if you have rights to the third-party images.
"""
import argparse
import json
import pathlib
import struct
import urllib.request
import zlib

ROOT = pathlib.Path(__file__).resolve().parents[1]
ROSTER = ROOT / "data/pokemon/pokemon.json"
ORIGINAL = ROOT / "src/main/resources/static/assets/sprites/pokemon"
OVERRIDES = ROOT / "art-overrides"
URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-i/red-blue/transparent/"
COLORS = {"Grass": (78,170,92), "Fire": (238,112,56), "Water": (59,144,225),
          "Electric": (237,192,48), "Bug": (137,170,57), "Poison": (156,90,177),
          "Rock": (165,133,89), "Ground": (191,154,105), "Psychic": (231,92,146),
          "Ghost": (119,102,170), "Fighting": (184,85,63), "Dragon": (95,116,209),
          "Normal": (140,151,158)}
DIGITS = ["111101101101111","010110010010111","111001111100111","111001111001111",
          "101101111001001","111100111001111","111100111101111","111001001001001",
          "111101111101111","111101111001111"]

def png_chunk(tag, content):
    return struct.pack(">I", len(content)) + tag + content + struct.pack(">I", zlib.crc32(tag + content) & 0xffffffff)

def placeholder(num, color, facing):
    # Original generic numbered pixel-art token, not a Pokemon likeness.
    w = h = 96
    pixels = bytearray(w*h*4)
    def rect(x0,y0,x1,y1,c):
        for y in range(y0,y1):
            for x in range(x0,x1):
                i = (y*w+x)*4
                pixels[i:i+4] = bytes((*c,255))
    dark = tuple(max(0,x-52) for x in color)
    rect(16,30,80,78,dark)
    rect(20,26,76,74,color)
    rect(26,20,70,30,color)
    rect(24,74,37,84,dark)
    rect(59,74,72,84,dark)
    if facing == "front":
        rect(29,35,37,43,(255,255,255))
        rect(59,35,67,43,(255,255,255))
        rect(33,38,37,43,(22,33,44))
        rect(59,38,63,43,(22,33,44))
    else:
        rect(40,31,56,40,dark)
    for index, ch in enumerate(str(num).zfill(3)):
        for offset, bit in enumerate(DIGITS[int(ch)]):
            if bit == "1":
                x = 32+index*12+(offset%3)*3
                y = 53+(offset//3)*3
                rect(x,y,x+3,y+3,(255,255,255))
    scan = b"".join(b"\0"+pixels[i*w*4:(i+1)*w*4] for i in range(h))
    return b"\x89PNG\r\n\x1a\n"+png_chunk(b"IHDR",struct.pack(">IIBBBBB",w,h,8,6,0,0,0))+png_chunk(b"IDAT",zlib.compress(scan))+png_chunk(b"IEND",b"")

def atlas(data, path):
    if not data.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ValueError("Not a PNG")
    w,h = struct.unpack(">II",data[16:24])
    if not (0 < w <= 512 and 0 < h <= 512):
        raise ValueError("Unexpected sprite dimensions")
    path.with_suffix(".png").write_bytes(data)
    size = {"w":w,"h":h}
    frame = {"frame":{"x":0,"y":0,**size},"rotated":False,"trimmed":False,
             "spriteSourceSize":{"x":0,"y":0,**size},"sourceSize":size}
    path.with_suffix(".json").write_text(json.dumps({"frames":{"00.png":frame},"meta":{"size":size}}))

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--download-authorized",action="store_true")
    p.add_argument("--output",type=pathlib.Path)
    args = p.parse_args()
    if not args.download_authorized and args.output is None:
        p.error("Specify --output for generated atlases or --download-authorized.")
    if args.output: args.output.mkdir(parents=True,exist_ok=True)
    roster = json.loads(ROSTER.read_text())
    if args.download_authorized: OVERRIDES.mkdir(exist_ok=True)
    count=0
    for species, info in roster.items():
        if not 1 <= info["num"] <= 151: continue
        for facing in ("front","back"):
            key=f"{facing}-{species}"
            if (ORIGINAL/(key+".png")).is_file() and (ORIGINAL/(key+".json")).is_file(): continue
            override=OVERRIDES/(key+".png")
            if args.download_authorized and not override.exists():
                url=URL+("back/" if facing=="back" else "")+str(info["num"])+".png"
                try:
                    with urllib.request.urlopen(url,timeout=15) as response: data=response.read(1024*1024)
                    if not data.startswith(b"\x89PNG\r\n\x1a\n"): raise ValueError("Not PNG")
                    override.write_bytes(data)
                    count+=1
                except Exception as e:
                    print(f"Could not import {key}: {e}; using original placeholder.")
            if args.download_authorized: continue
            color=COLORS.get(info["types"][0], COLORS["Normal"])
            data=override.read_bytes() if override.exists() else placeholder(info["num"],color,facing)
            atlas(data,args.output/key)
            count+=1
    print(f"Prepared {count} files/atlases.")

if __name__=="__main__": main()
