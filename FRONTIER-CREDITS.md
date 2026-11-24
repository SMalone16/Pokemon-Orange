# Orange Frontier map and art

Adapted from [Mozilla BrowserQuest](https://github.com/mozilla/BrowserQuest), created by Little Workshop (Franck Lecollinet and Guillaume Lecollinet). Upstream source revision: `af32d247cac3495ca430d0effbb88dd5f3250b2c`.

The source map and tilesheet are licensed [Creative Commons Attribution-ShareAlike 3.0 Unported](https://creativecommons.org/licenses/by-sa/3.0/). The adapted Frontier map and its tilesheet remain under that same license. This does not grant rights to unrelated Pokémon assets. Source: [upstream LICENSE](https://github.com/mozilla/BrowserQuest/blob/af32d247cac3495ca430d0effbb88dd5f3250b2c/LICENSE).

Changes: cropped a 96×96 region (source x=0–95, y=160–255), converted ordered terrain and overhead layers to legacy Phaser Tiled JSON, combined source collision and blocking grids, bounded map edges, added Pokémon encounters and a portal to the existing town. Tilesheet is unchanged. Original BrowserQuest doors, quests, NPCs and monsters are not imported. The importer is in scripts/prepare-frontier.py and fetches verified, pinned source assets during build.
