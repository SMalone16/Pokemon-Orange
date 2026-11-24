# Pokemon Orange

Classroom adaptation of Pokemon Brown, a Brown University CSCI0320 student project.

## Start

In the repository Codespace:
```bash
git pull origin master
bash start.sh
```
Open port 4567 privately; test with two separate browser profiles. If permitted by school policy, make the game port public and share the URL during class only. PostgreSQL is private. Students enter an unused fictional name (4–12 letters/numbers); a random starter is assigned. No email, code or password is requested. A random browser token owns the account; the same name cannot be claimed from another browser. Returning players resume automatically on the same browser/profile or enter their display name to reuse the locally stored account credential. To use another device, copy the private Recovery code from the game and enter it with the same display name. Keep that code private; names alone cannot take over an existing account. Existing players whose original browser credentials are already lost cannot be recovered by this mechanism. Legacy accounts can resume with their existing token; the old login screen is no longer offered. Do not use real student names. Chat remains off by default. Name-only registration is open to anyone with the URL; this is not school authentication. Make the port private after class.

## Controls and reconnecting

WASD and arrow keys move one tile; click-to-move still works. The client attempts automatic reconnection with backoff, heartbeat and existing session token. Saves occur every 30 seconds and on graceful shutdown. Disconnects may cancel an active battle/trade. The legacy server has not been load-tested with 20+ users.

## Codespaces timeout

Go to https://github.com/settings/codespaces and set the default idle timeout to 120 minutes (or up to 240 if allowed). For an existing Codespace, check the Codespaces VS Code command palette for timeout settings; if a new Codespace is needed, back up before creating it. Keep the editor open while supervising; student web traffic is not a reliable indicator of editor activity. Use `docker compose logs -f game` to monitor actual gameplay logs. A stopped Codespace must be resumed by the owner; the browser cannot restart it. Codespaces are development environments, not permanent game hosting.

## Encounters and artwork

The roster includes 79 defined species (15 retained and 64 new). Wild encounters are inspired by Kanto's early, middle and late level bands and scale with team progression; this is not an exact Red/Blue route map, learnset or evolution system. Existing legacy species remain loadable for saves. New creatures have original numbered placeholder art unless authorized artwork is supplied.

PokeAPI hosts original Red/Blue front/back PNGs; their license notice explicitly identifies the images as copyrighted by The Pokémon Company. Public availability and educational purpose alone do not establish permission. See https://github.com/PokeAPI/sprites/blob/master/LICENCE.txt and https://github.com/PokeAPI/sprites. Only if you have appropriate permission, run:
```bash
python3 scripts/prepare-sprites.py --download-authorized
bash start.sh
```
The script downloads missing Gen I front/back sprites into git-ignored art-overrides, converts them to compatible single-frame atlases on build, and preserves the original animated assets. You may also supply your own appropriately licensed PNGs as front-species.png/back-species.png in art-overrides. No new third-party sprite files are committed. Do not publish unauthorized art or assume an educational exemption. Existing repository assets also need a rights review before public distribution.

## Teacher commands

- `bash smoke-test.sh`: disposable account and asset test.
- `docker compose logs -f game`: live logs (Ctrl+C stops viewing).
- `docker compose ps`: server status.
- `docker compose attach game`: teacher console; detach Ctrl+P then Ctrl+Q.
- `docker compose down`: graceful stop; retains database volume.
- Back up: `docker compose exec -T db pg_dump -U pokemon -d pokemon_orange > orange-backup.sql`. Store securely; do not commit. Do not delete .env or the volume. Rebuilds and Codespace deletion can destroy local Docker data.

Original project: https://github.com/felixs-alt/Pokemon-Brown

## Orange Frontier and experience

New players spawn in the 96×96 BrowserQuest-derived Frontier. Existing saves remain in their original chunk. From the old town spawn, walk east to row 27, column 34; from the Frontier arrival tile, step one tile west to return. Wild encounters are scattered on connected routes away from arrival. See [FRONTIER-CREDITS.md](FRONTIER-CREDITS.md) for source, CC BY-SA 3.0 license and adaptations. The first build downloads and verifies the pinned map and tilesheet.

Wild knockouts and completed PvP victories grant experience to surviving team Pokémon using the existing level-based formula; captures, running, losing and forfeits do not grant XP. The victory screen shows total earned XP. Existing level and experience data are saved in PostgreSQL.

After updating, test with two browser profiles: join, win a wild battle, verify XP, close/reopen, and recover using the same name and private recovery code. Test both Frontier portals and verify blocked terrain. A database backup before updating is recommended.
