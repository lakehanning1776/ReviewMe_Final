# ReviewMe creator features — setup notes

This update adds JPA entity classes and Spring Data repositories for the existing MySQL tables `creator_profiles`, `creator_reviews`, and `creator_codes`. It does not yet add the web forms/controllers for creating or editing these records.

## Database connection

`application.properties` now reads the password from the `DB_PASSWORD` environment variable so the database password is not hardcoded in source. In IntelliJ, open **Run → Edit Configurations…**, select the ReviewMe run configuration, and set Environment variables to `DB_PASSWORD=YOUR_LOCAL_MYSQL_PASSWORD`. Do not commit the real value.

The app expects the existing `demo30` schema and tables. Keep `spring.jpa.hibernate.ddl-auto=update`; do not use `create`, `create-drop`, or delete the data directory.

## Added files

- `model/CreatorProfile.java`
- `model/CreatorReview.java`
- `model/CreatorCode.java`
- `repository/CreatorProfileRepository.java`
- `repository/CreatorReviewRepository.java`
- `repository/CreatorCodeRepository.java`

The database entities are mapped to the table/column names created in MySQL Workbench. Creator review ratings are represented as decimal values, and the creator sponsorship/affiliate fields remain separate from regular player reviews.


## Creator dashboard forms (added)

The creator dashboard now supports:
- A creator video-review form with a separate 1.0–10.0 score (half-point increments), video URL, notes, and sponsored disclosure. These scores are shown separately from player community ratings on each game page.
- Discount-code management with optional game association, discount description, store URL, expiration date, and affiliate disclosure. Creators can delete their own codes.
- Active game-specific discount codes and creator video reviews are displayed on the relevant game detail page. Expired codes are hidden from game pages.

The app uses `spring.jpa.hibernate.ddl-auto=update`; the `creator_reviews.rating` column is mapped to `DECIMAL(3,1)` so a score of 10.0 fits. Back up your database before running schema updates.
