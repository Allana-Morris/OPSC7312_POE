Music Match - The Spotify Dating App
By Allana Morris(ST10204772), Sven Masche(ST10030798), Max Walsh(ST10203070), Reilly Mare(ST10080623)

This is a Kotlin-based Android dating application built using Android Studio. The app matches users based on their Spotify listening habits, utilizing the Spotify API. Users can sign in with their Google accounts or register using the in-app registration
Next the User must sign in to Spotify to display their profiles and get matched with others based on their favorite songs, artists, and playlists.

Features

- Spotify Authentication: Users sign in with their Spotify account to use the app.
- Retrieve Spotify Data: Fetches the user's Spotify profile information and top items (tracks, artists, genres.).
- Matchmaking Based on Spotify Data: Matches users based on their music preferences, creating a unique and personalized matchmaking experience.

Prerequisites

To build and run this project, you'll need:

- Android Studio installed
- A Spotify Developer Account
- Spotify API client ID and client secret
- Kotlin (as the primary programming language)
- A Firebase account (for database)

Getting Started

1. Clone the Repository

```sh
git clone https://github.com/Allana-Morris/OPSC7312_POE
```

2. Spotify Developer Setup

1. Go to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/applications) and create a new application.
2. Obtain the `client_id` and `client_secret` from your Spotify application.
3. Set the redirect URI to match your Android app's scheme.

3. Configure the App

- In the `gradle.properties` file, add your Spotify client credentials:

  ```properties
  SPOTIFY_CLIENT_ID=your_client_id
  SPOTIFY_CLIENT_SECRET=your_client_secret
  SPOTIFY_REDIRECT_URI=your_redirect_uri
  ```

4. Firebase Setup

1. Set up a Firebase project and add your Android app.
3. Add the Firebase configuration file (`google-services.json`) to the app's `/app` directory.

5. Dependencies

The following dependencies are used in the project:

- Spotify SDK: to handle Spotify login and API calls.
- OkHttp: for making network requests to the Spotify API.

Add these dependencies to your `build.gradle`:

```kotlin
implementation("com.spotify.android:auth:1.2.3")
implementation("com.google.firebase:firebase-auth:21.1.0")
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

6. Spotify Authentication

- Users are authenticated using the Spotify SDK.
- The app initiates an `AuthorizationRequest` and handles the response with `AuthorizationClient`.
- User information such as display name, profile picture, and top items (artists/tracks) is retrieved after successful authentication.

7. Retrieving User Spotify Data

After logging in, the app will:

1. Use the access token to retrieve the user's Spotify profile information.
2. Fetch the user's top tracks and artists using Spotify's Web API.
3. Display the retrieved data in the user's profile and match them with other users based on musical preferences.


8. License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
