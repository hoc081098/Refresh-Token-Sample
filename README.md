# Android Refresh Token Sample :repeat_one:

- Android Refresh token with Retrofit, OkHttp and Coroutines Mutex.
- When multiple requests hit **401** (_HTTP_UNAUTHORIZED_), only single `Refresh token request` will be executed.
- After successful refresh, all pending requests will be executed concurrently.

## Features

- ✅ Refresh token only once for multiple requests
- ✅ Log out user if refreshToken failed
- ✅ Log out if user gets an error after first refreshing
- ✅ Queue all requests while token is being refreshed

# Run local server
```
cd server
npm i
npm run start
```

Change `baseUrl` at `app/src/main/java/com/hoc081098/refreshtokensample/data/DataModule.kt`
