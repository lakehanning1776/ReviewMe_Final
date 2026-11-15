# ReviewMe Community Tags

This update adds community tags to each game's details page and the game catalog.

## Features
- Add a community tag from a game's details page.
- Tags are normalized case-insensitively and reused across games (for example, `Replayable` and `replayable` are the same tag).
- A game cannot receive the same tag twice.
- Popular tags appear on the catalog page with a count of how many games use each tag.
- Click a tag to filter the catalog to games with that tag.
- Existing game and review data is retained. Hibernate's existing `ddl-auto=update` setting creates the new tag tables and link table without dropping existing tables.

## Testing
1. Run `DemoApplication` as usual.
2. Open a game's detail page.
3. In Community tags, add `Replayable` and submit.
4. Add `replayable` again; it should report that the tag is already attached.
5. Return to the catalog and click the tag to filter games.
6. Add the same tag to a second game and verify its count increases.

Tag counts represent the number of games using a tag, not the number of users voting for it. Tags are currently community-wide and are not tied to individual accounts.
