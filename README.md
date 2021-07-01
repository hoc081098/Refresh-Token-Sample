# Refresh Token Sample

- When multiple requests hit **404** (_HTTP_UNAUTHORIZED_), only single `Refresh token request` will be executed.
- After successful refresh, all pending requests will be executed concurrently.

# Run local server
```
cd server
npm i
npm run start
```

Change `baseUrl` at `app/src/main/java/com/hoc081098/refreshtokensample/data/DataModule.kt`
