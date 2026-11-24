#!/usr/bin/env python3
"""Adapt a pinned, openly licensed BrowserQuest map and 16px tilesheet."""
import argparse
import hashlib
import json
import pathlib
import struct
import urllib.request
from collections import deque

REV = "af32d247cac3495ca430d0effbb88dd5f3250b2c"
BASE = "https://raw.githubusercontent.com/mozilla/BrowserQuest/" + REV + "/"
SIZE = 96
X0, Y0 = 0, 160
ENTRY = (50, 18)
EXIT = (50, 17)

def get(path, git_sha):
    with urllib.request.urlopen(BASE + path, timeout=45) as response:
        data = response.read()
    actual = hashlib.sha1(("blob %d\0" % len(data)).encode() + data).hexdigest()
    if actual != git_sha:
        raise ValueError("Unexpected upstream file: " + path)
    return data

def convert(src, png):
    w, h = struct.unpack(">II", png[16:24])
    assert w % 16 == 0 and h % 16 == 0 and src["tilesize"] == 16
    assert X0 + SIZE <= src["width"] and Y0 + SIZE <= src["height"]
    high = set(src["high"])
    blocked = set(src["collisions"]) | set(src["blocking"])
    ground = [[0] * (SIZE * SIZE) for _ in range(5)]
    tops = [[0] * (SIZE * SIZE) for _ in range(5)]
    collision = [0] * (SIZE * SIZE)
    encounters = [0] * (SIZE * SIZE)
    for r in range(SIZE):
        for c in range(SIZE):
            index = r * SIZE + c
            old = (r + Y0) * src["width"] + c + X0
            gids = src["data"][old]
            gids = gids if isinstance(gids, list) else [gids]
            assert all(0 <= gid <= (w // 16) * (h // 16) for gid in gids)
            low = [v for v in gids if v and v not in high]
            top = [v for v in gids if v and v in high]
            for i, v in enumerate(low): ground[i][index] = v
            for i, v in enumerate(top): tops[i][index] = v
            if old in blocked or not low or r in (0, SIZE - 1) or c in (0, SIZE - 1):
                collision[index] = 1
    assert not any(collision[r * SIZE + c] for r,c in (ENTRY, EXIT)), "Portal must be walkable"
    reached = {ENTRY}
    q = deque([ENTRY])
    while q:
        r,c = q.popleft()
        for y,x in ((r-1,c),(r+1,c),(r,c-1),(r,c+1)):
            if 0 <= y < SIZE and 0 <= x < SIZE and not collision[y*SIZE+x] and (y,x) not in reached:
                reached.add((y,x))
                q.append((y,x))
    assert EXIT in reached and len(reached) > 500, "Frontier must have a substantial connected area"
    # Encounter bands on traversable outdoor routes, away from the arrival area.
    for r,c in sorted(reached):
        if abs(r-ENTRY[0])+abs(c-ENTRY[1]) > 12 and (r//8+c//12)%3 == 0 and (r*7+c*11)%5 == 0:
            encounters[r*SIZE+c] = 1
    assert sum(encounters) > 50
    names = ["Base","Walk","Walkable","Ground4","Ground5"]
    layers = []
    def layer(name, data, visible=True):
        layers.append(dict(name=name,type="tilelayer",width=SIZE,height=SIZE,x=0,y=0,opacity=1,visible=visible,data=data))
    for i, data in enumerate(ground):
        if i < 3 or any(data): layer(names[i],data)
    layer("Collision",collision,False)
    for i,data in enumerate(tops):
        if i == 0 or any(data): layer("Top" if i==0 else "Top"+str(i+1),data)
    layer("Encounters",encounters,False)
    return dict(width=SIZE,height=SIZE,tilewidth=16,tileheight=16,orientation="orthogonal",renderorder="right-down",version=1,layers=layers,tilesets=[dict(firstgid=1,name="frontier",image="../tilesets/frontier.png",imagewidth=w,imageheight=h,columns=w//16,tilewidth=16,tileheight=16,tilecount=(w//16)*(h//16),margin=0,spacing=0)]), len(reached), sum(encounters)

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--output",type=pathlib.Path,required=True)
    args=p.parse_args()
    source=json.loads(get("client/maps/world_client.json","1d57acb82271c5ff91dddd5c8acc2a8f0f8f2285"))
    png=get("client/img/1/tilesheet.png","b9f7707d4239a650a68f513b48f826062c0372fb")
    world, reachable, encounters=convert(source,png)
    for sub in ("maps","tilesets"): (args.output/sub).mkdir(parents=True,exist_ok=True)
    (args.output/"maps/frontier.json").write_text(json.dumps(world,separators=(",",":")))
    (args.output/"tilesets/frontier.png").write_bytes(png)
    print("Frontier: %d connected tiles, %d encounter tiles" % (reachable,encounters))
if __name__ == "__main__": main()
