# Pokemon Orange

A classroom-hosted adaptation of Pokemon Brown, a Brown University CSCI0320 student project. Explore a shared world, catch and train creatures, trade, battle and enter tournaments. The original project is an experimental game, not a production service.

## Start a class server in GitHub Codespaces

1. Choose **Code → Codespaces → Create codespace on master**. Wait for the dev container to finish.
2. In the terminal at the repository root, run:

   ```bash
   bash start.sh
   ```

   The script creates a private random database password and class registration code (once per Codespace), builds with Java 8/Maven, starts PostgreSQL on a private Docker network, and waits for the game to respond. No sudo, Java installation, database JSON, or manual port edits are needed.
3. Open **Ports → 4567 → Open in Browser**. Test registration and login privately first, then right-click the port and choose **Port Visibility → Public**, if school policy permits. Share the forwarded URL and the class code printed in the terminal.
4. Students use a fictional 4–12 character alphanumeric player name, the class code, a unique game-only password of at least 8 characters, and a starter with a 4–12 character alphanumeric nickname. No actual emails or school passwords. Chrome recommended.

## Optional private smoke test

Run `bash smoke-test.sh` after startup. It checks the homepage, locally served browser libraries, rejection of an incorrect class code, registration and login. It creates one disposable fictional test account. It does not replace testing movement, multiplayer, trades and battles in two separate browser profiles.

## Daily operation

- Start again: `bash start.sh`
- View logs: `docker compose logs -f game` (Ctrl+C exits log viewing only).
- Stop: `docker compose down` (preserves the database volume).
- Teacher console: `docker compose attach game` (detach with Ctrl+P then Ctrl+Q; Ctrl+C can stop the game).
- Show class code: `sed -n 's/^CLASS_CODE=//p' .env`
- Status: `docker compose ps`

Keep the same Codespace and do not delete the Docker volume or .env file. Docker data may be lost on a Codespace rebuild or deletion; back up before rebuilding. To back up while the database is running: `docker compose exec -T db pg_dump -U pokemon -d pokemon_orange > orange-backup.sql`. Store the dump securely, never commit it. The game saves periodically (60 seconds by default); attempts a final save on graceful shutdown. Allow time for a periodic save before stopping as an additional precaution.

## Classroom defaults and limitations

- Only the game port is published; PostgreSQL is not exposed. The game uses same-origin HTTP requests and secure WebSockets when served over HTTPS.
- Registration requires a generated classroom code. Login uses each player's game password. The code limits casual registration, not all access to the public URL; it is not a school identity system.
- Free-text chat is disabled in both the browser and server by default. A teacher can set ENABLE_CHAT=true in .env and restart, but moderation is not provided.
- This legacy project has not had a full security audit or a 20-player load test. Use pseudonyms, test with multiple browsers, supervise play, follow school policies and make the game port private again after class. Do not expose real student data or leave the server publicly accessible unattended.
- Codespaces can stop after inactivity, and deleting a Codespace destroys its local storage. Keep the server open during class.
- Some original visual/gameplay glitches may remain.

## Troubleshooting

Run `docker compose ps` and `docker compose logs --tail=100 db game`. If you change code, rerun `bash start.sh` to rebuild. Never share .env or database credentials.

Original project: https://github.com/felixs-alt/Pokemon-Brown
