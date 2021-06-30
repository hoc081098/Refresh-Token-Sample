# Refresh Token Sample

- When multiple request hits 404 (HTTP_UNAUTHORIZED), single "Refresh token request" will be executed.
- After refresh successfully, all pending request will be executed sequentially.

# Run local server
```
cd server
npm i
npm run start
```

Change `baseUrl` at `app/src/main/java/com/hoc081098/refreshtokensample/data/DataModule.kt`