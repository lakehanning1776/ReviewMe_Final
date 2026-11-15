# ReviewMe Account Roles Setup

This update adds `USER`, `CREATOR`, and `ADMIN` roles to the existing `users` table. New registrations are always `USER`; the registration form cannot grant privileged roles. The Creator Dashboard is available to `CREATOR` and `ADMIN` accounts. The Admin Dashboard lets admins change roles.

## Database login
Set `DB_PASSWORD` in IntelliJ Run > Edit Configurations > Environment variables to the password for the MySQL account configured in `src/main/resources/application.properties`. Do not put the password in source control.

## Promote the first admin
Because registration must not allow users to make themselves admins, promote the intended first admin directly in MySQL Workbench. Run this after the app has started once so Hibernate adds the `role` column, or add the column manually first:

```sql
USE demo30;
UPDATE users SET role = 'ADMIN' WHERE username = 'YOUR_EXISTING_USERNAME';
SELECT id, username, role FROM users;
```

Replace `YOUR_EXISTING_USERNAME` with the exact username for the account you control. If the role column does not exist yet, run:

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
```

Log out and log back in after changing a role so the session refreshes. Do not share database passwords.

## Routes
- `/register` — public registration (always USER)
- `/login` — login
- `/creator` — creator profile editor (CREATOR or ADMIN)
- `/admin` — role management (ADMIN only)

This is the first role-system milestone. Video review and discount-code forms are not implemented in this version yet.
