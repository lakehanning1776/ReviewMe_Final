# ReviewMe: account-linked reviews and community tags

## Included
- Existing community tags and tag-based catalog filtering remain in place.
- Registration and login store a logged-in user ID and username in the HTTP session.
- Passwords continue to be stored as BCrypt hashes by the existing UserService.
- Reviews submitted while logged in are linked to the `users` table by `game_reviews.user_id`.
- A user can have one review per game; submitting again updates that user's review.
- Legacy reviews keep their existing reviewerName and remain visible even without a linked account.
- Logged-out visitors can browse reviews and tags but must log in to post a review.

## Test
1. Keep your original project folder as a backup and extract this ZIP into a new folder.
2. Verify `application.properties` matches your local MySQL connection. Do not run DROP/TRUNCATE commands.
3. Start `DemoApplication`. Hibernate uses `ddl-auto=update` to add the nullable `user_id` column to the existing review table and preserve legacy rows.
4. Register a new account, log in, open a game, submit a review, and confirm the username appears.
5. Submit another review for the same game while logged in; it should update the existing review.
6. Log out and verify the review form asks you to log in.
7. Add community tags and click tags to filter the collection.

## Notes
- The app uses a simple session-based custom login consistent with the existing project structure. This is suitable as a project feature baseline; before public deployment, enable CSRF protection, strengthen session handling, and add ownership checks for administrative game-management routes.
- Database migration/build have not been verified in this environment; test locally before relying on the changes.
