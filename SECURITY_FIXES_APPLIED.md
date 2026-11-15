# ReviewMe security fixes applied

This patch is based on the project archive supplied for the November 1, 2026 test session. It addresses the observed issues below.

## Changes

1. **Game-management authorization:** adding, editing, and deleting games now requires a valid logged-in account whose database role is `ADMIN` or `CREATOR`. The check reads the current role from the database rather than trusting only the role cached in the session. The edit/add links are also hidden from regular users, but the server-side checks are the actual security control.
2. **CSRF:** Spring Security CSRF protection is enabled again. The application's login/register and application POST forms use Thymeleaf `th:action` forms so Spring Security's request-data integration can add the CSRF token. Verify every POST form in the running app after applying the patch.
3. **Login throttling:** failed attempts are tracked by remote IP plus normalized username. Five failures in a 15-minute window trigger a 15-minute lockout. A successful login clears that key. This is an in-memory, single-instance throttle; production deployment should use a shared store (for example Redis) and add monitoring.
4. **Session fixation:** the session ID is rotated after successful login.
5. **Community tags:** anonymous users are redirected to login before adding tags.
6. **Deletion method:** game deletion is now POST-only instead of a destructive GET route, so it is protected by CSRF when invoked from a form.

## Important testing notes

- This patch must be built and tested against the database and application environment used for the November 1 test.
- Existing `ADMIN` and `CREATOR` accounts should retain their database roles.
- Re-test a regular user against `/showNewGameForm`, `/showFormForUpdate/{id}`, `/saveGame`, and `/deleteGame/{id}`.
- Re-test an authenticated creator and administrator to confirm intended game management still works.
- Re-test all POST forms: login, registration, reviews, tags, creator profile/reviews/codes, reports, moderation, and role changes. A missing CSRF token should be rejected; normal forms should submit successfully.
- Test login throttling with a test account: five wrong passwords should trigger the lockout; a correct password should work after the lockout expires.
- The previous JMeter homepage GET test does not verify login throttling, CSRF, or authorization.
- No claim is made that the patch has passed runtime testing until the build and manual retest are completed.
