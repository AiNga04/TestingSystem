# Account, Group and GroupAccount API

Postman baseUrl: `http://localhost:8080`. Every request below already includes `/api`.
All responses use ApiResponse; list data uses PageResponse. IDs and foreign keys must be 1..255.

## Account

| Method | URL | Purpose |
| --- | --- | --- |
| POST | `/api/v1/accounts` | Create (201, Location header) |
| GET | `/api/v1/accounts/{id}` | Get active account and relation DTOs |
| GET | `/api/v1/accounts` | List active accounts |
| GET | `/api/v1/accounts?deleted=true` | List soft-deleted accounts |
| PUT | `/api/v1/accounts/{id}` | Update profile and foreign keys |
| DELETE | `/api/v1/accounts/{id}` | Soft delete |
| PATCH | `/api/v1/accounts/{id}/restore` | Restore (no body) |

Create body:

```json
{
  "email": "newuser@example.com",
  "username": "newuser",
  "fullName": "New User",
  "departmentId": 1,
  "positionId": 1
}
```

Update body:

```json
{"fullName":"Updated User","departmentId":2,"positionId":2}
```

Email and username remain immutable, matching the existing entity design. Required text is trimmed before saving. Email must be valid; email, username and fullName have a 50-character limit. Email and username stay unique across active and deleted accounts. Referenced Department and Position must exist and be active; the service locks referenced rows inside the transaction before writing.

Query parameters: `keyword` (email/username/fullName), `departmentId`, `positionId`, `deleted`, `page` (default 0), `size` (1..100, default 10), `sortBy` (accountId/email/username/fullName/createdAt), `direction` (asc/desc).

Account data contains accountId, email, username, fullName, department (DepartmentResponse), position (PositionResponse), createdAt and deletedAt. Collection relations are not serialized.

## Group

| Method | URL | Purpose |
| --- | --- | --- |
| POST | `/api/v1/groups` | Create (201, Location header) |
| GET | `/api/v1/groups/{id}` | Get active group |
| GET | `/api/v1/groups` | List active groups |
| GET | `/api/v1/groups?deleted=true` | List deleted groups |
| PUT | `/api/v1/groups/{id}` | Update name and creator |
| DELETE | `/api/v1/groups/{id}` | Soft delete |
| PATCH | `/api/v1/groups/{id}/restore` | Restore (no body) |

Create/update body:

```json
{"groupName":"Backend Team","creatorId":1}
```

Name is required, trimmed, at most 50 characters and unique across active/deleted groups. Creator must exist and be active. Creator is not automatically added as a member.

Query parameters: keyword, creatorId, deleted, page, size, sortBy (groupId/groupName/createdAt), direction. Group data contains groupId, groupName, creator (AccountSummary), createdAt and deletedAt. Existing rows with a null creator remain readable.

## GroupAccount membership

| Method | URL | Purpose |
| --- | --- | --- |
| POST | `/api/v1/groups/{groupId}/members` | Add member (201, Location) |
| GET | `/api/v1/groups/{groupId}/members` | Page of active members |
| GET | `/api/v1/groups/{groupId}/members/{accountId}` | Read a member |
| DELETE | `/api/v1/groups/{groupId}/members/{accountId}` | Remove link (200) |

Add body:

```json
{"accountId":2}
```

List parameters: keyword (username/fullName), page, size, sortBy (accountId/joinedAt/username), direction.

The composite primary key is (GroupID, AccountID), mapped through GroupAccountId, @EmbeddedId and @MapsId. GroupAccountResponse includes groupId, groupName, account (AccountResponse) and joinedAt. Adding the same account twice returns 409. Removing a member deletes only the GroupAccount link, never Account or Group. Re-adding after removal sets a new joinedAt. All writes lock the group first, then the account, and execute in one transaction.

The Account-to-Group relationship is many-to-many through a link entity: two ManyToOne associations on GroupAccount and inverse OneToMany collections on Account/Group. There is no duplicate direct ManyToMany mapping because JoinDate is part of the membership model. Already-loaded inverse collections are synchronized without fetching all members.

## Soft-delete behavior and errors

Account/Group soft deletion preserves their rows, creator references and membership links. Deleted accounts are hidden from member reads/lists. Restoring an account makes its retained memberships visible again. Soft-deleting a group hides its membership endpoints; restoring the group preserves membership history. Group restore requires its creator to be active, so restore the creator first when needed. Account restore requires active Department and Position. A deleted account can still be removed from an active group's membership list by DELETE.

GET by ID and PUT accept active parents only. Deleted names/email/username remain reserved. Repeated DELETE returns 404; restore on an already-active entity returns 409.

- 400: invalid body, text, FK range, paging or sort; validation errors include field messages.
- 404: missing resource/FK, deleted parent, or missing membership.
- 409: duplicate identity/name/member or an existing but deleted FK.
- Database constraints remain the final guard for competing writes; constraint conflicts use the existing handler.

## Queries and schema

Paged Account queries use EntityGraph for department/position. Group queries fetch creator. Membership queries fetch group, account, account.department and account.position. No collection is fetched in paged queries. Integration tests measure SQL counts: Account/Group list at most page + count (2 queries); member list adds one parent lookup (3 queries). Tests also assert no secondary entity/collection fetches.

References: [Spring Data EntityGraph](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html), [Hibernate association and composite-key guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html).

New Docker databases get DeletedAt for Account and Group from 01_init.sql. Existing databases use the idempotent migration (already applied to this workspace's running MySQL):

```powershell
Get-Content database/migrations/05_account_group_soft_delete.sql -Raw | docker compose -f docker/docker-compose.yml exec -T mysql sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql -u root'
```

Restart Spring Boot after updating the code. Do not reset the Docker volume. Run `mvnw.cmd test` with MySQL/.env available; integration writes roll back, though auto-increment counters can advance.

Import `account-group.postman_collection.json` in Postman for an ordered sample flow. Set departmentId/positionId to active rows, run Create Account and Create Group first; their scripts capture IDs for subsequent requests.
