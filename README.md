# Android Refresh Token Sample :repeat_one:

- Android Refresh token with Retrofit, OkHttp and Coroutines Mutex.
- When multiple requests hit **401** (_HTTP_UNAUTHORIZED_), only single `Refresh token request` will be executed.
- After successful refresh, all pending requests will be executed concurrently.
- Store user and token using [Proto DataStore from Jetpack](https://developer.android.com/topic/libraries/architecture/datastore).
- This example app shows how you can encrypt your data when using [Proto DataStore from Jetpack](https://developer.android.com/topic/libraries/architecture/datastore).

[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.21-c711e1.svg?logo=kotlin)](http://kotlinlang.org)
[![Build CI](https://github.com/hoc081098/Refresh-Token-Sample/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/Refresh-Token-Sample/actions/workflows/build.yml)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fhoc081098%2FRefresh-Token-Sample&count_bg=%2379C83D&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](https://opensource.org/licenses/MIT)

## Buy me a coffee
Liked some of my work? Buy me a coffee (or more likely a beer)

[!["Buy Me A Coffee"](https://cdn.buymeacoffee.com/buttons/default-orange.png)](https://www.buymeacoffee.com/hoc081098)

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

- Change `baseUrl` (e.g. `http://YOUR_ID_ADDRESS:3000/`) at `app/src/main/java/com/hoc081098/refreshtokensample/data/DataModule.kt`
- Change `expiresIn` (default value is 1 minute) at `server/routes/index.js`.

# Find this repository useful? ❤️

Star this repository and follow me for next creations! Thanks for your support 💗💗.
