#!/bin/sh
# One-time migration: rewrite stored image URLs from the old r2.dev development
# domain to the CDN custom domain. Safe to re-run (a no-op once rewritten).
# Run while Postgres (docker) and MongoDB are up.

OLD="https://pub-5d63e273a4ec4fa08526c133cd4a2084.r2.dev"
NEW="https://img.2ndhub.com"

echo "== Postgres: note photos (note_svc.note.photos JSON) =="
docker exec -i $(docker ps -qf "ancestor=postgres" -f "publish=5432" | head -1) \
  psql -U postgres -d secondhand -c \
  "UPDATE note_svc.note SET photos = REPLACE(photos, '$OLD', '$NEW') WHERE photos LIKE '%$OLD%';" \
  2>/dev/null || psql -h localhost -U postgres -d secondhand -c \
  "UPDATE note_svc.note SET photos = REPLACE(photos, '$OLD', '$NEW') WHERE photos LIKE '%$OLD%';"

echo "== Postgres: user avatars (user_svc.users.profile_photo) =="
docker exec -i $(docker ps -qf "ancestor=postgres" -f "publish=5432" | head -1) \
  psql -U postgres -d secondhand -c \
  "UPDATE user_svc.users SET profile_photo = REPLACE(profile_photo, '$OLD', '$NEW') WHERE profile_photo LIKE '%$OLD%';" \
  2>/dev/null || psql -h localhost -U postgres -d secondhand -c \
  "UPDATE user_svc.users SET profile_photo = REPLACE(profile_photo, '$OLD', '$NEW') WHERE profile_photo LIKE '%$OLD%';"

echo "== MongoDB: chat photo messages (secondhand_message.messages.content) =="
mongosh "mongodb://localhost:27017/secondhand_message" --quiet --eval "
  const r = db.messages.updateMany(
    { content: { \$regex: 'pub-5d63e273a4ec4fa08526c133cd4a2084\\\\.r2\\\\.dev' } },
    [ { \$set: { content: { \$replaceAll: { input: '\$content', find: '$OLD', replacement: '$NEW' } } } } ]
  );
  print('matched: ' + r.matchedCount + ', modified: ' + r.modifiedCount);
"

echo "Done."
