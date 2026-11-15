# ReviewMe review feature update

This update builds on the original ReviewMe Spring Boot + Thymeleaf + MySQL project.

## What's added
- Dark, responsive ReviewMe library page with in-page game search.
- Individual game detail/review page at `/games/{id}`.
- Persistent reviews in a new `game_reviews` table.
- Review display name, 1–5 rating, review text, and creation timestamp.
- Average rating and review count on the library and detail pages.
- Basic server-side review validation and user feedback.
- Updated add/edit game forms to match the new visual style.

## Run it
1. Open this project folder in IntelliJ.
2. Keep your existing local MySQL setup and database. `application.properties` is retained from the original project.
3. Run `DemoApplication`.
4. Visit `http://localhost:8080`.
5. Open a game and submit a test review. Hibernate's existing `spring.jpa.hibernate.ddl-auto=update` setting should create the new `game_reviews` table without dropping existing tables.

## Notes
- Reviews currently use a display name and are not linked to a signed-in user account.
- The original project has basic authentication pages, but this update does not change that authentication flow.
- This environment did not have Maven installed, so a full Maven build could not be run here. Please run the project in IntelliJ and report the first error if one appears.
