Music Match - The Spotify Dating App
By Allana Morris(ST10204772), Sven Masche(ST10030798), Max Walsh(ST10203070), Reilly Mare(ST10080623)

This is a Kotlin-based Android dating application built using Android Studio. The app matches users based on their Spotify listening habits, utilizing the Spotify API. Users can sign in with their Google accounts or register using the in-app registration. Next the User must sign in to Spotify to display their profiles and get matched with others based on their favorite songs, artists, and playlists.

Features

- Spotify Authentication: Users sign in with their Spotify account to use the app.
- Retrieve Spotify Data: Fetches the user's Spotify profile information and top items (tracks, artists, genres.).
- Matchmaking Based on Spotify Data: Matches users based on their music preferences, creating a unique and personalized matchmaking experience.
- Biometric Authentication: Secure fingerprint login for quick, secure access.
- Google SSO: Users can register using their Google account or through in-app registration.
- Multi-language Support: Supports multiple South African languages, enhancing inclusivity.
- Real-Time Notifications: Keeps users updated on matches and messages.

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

- Go to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard/applications) and create a new application.
- Obtain the `client_id` and `client_secret` from your Spotify application.
- Set the redirect URI to match your Android app's scheme.

3. Configure the App

- In the `gradle.properties` file, add your Spotify client credentials:

  ```properties
  SPOTIFY_CLIENT_ID=your_client_id
  SPOTIFY_CLIENT_SECRET=your_client_secret
  SPOTIFY_REDIRECT_URI=your_redirect_uri
  ```

4. Firebase Setup

- Set up a Firebase project and add your Android app.
- Add the Firebase configuration file (`google-services.json`) to the app's `/app` directory.

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

6. APK Generation
   
- Build the app in Android Studio
- Use Build > Generate Signed APK for release versions.

7. Spotify Authentication

- Users are authenticated using the Spotify SDK.
- The app initiates an `AuthorizationRequest` and handles the response with `AuthorizationClient`.
- User information such as display name, profile picture, and top items (artists/tracks) is retrieved after successful authentication.

8. Retrieving User Spotify Data

After logging in, the app will:

- Use the access token to retrieve the user's Spotify profile information.
- Fetch the user's top tracks and artists using Spotify's Web API.
- Display the retrieved data in the user's profile and match them with other users based on musical preferences.


Architecture Overview

- Tech Stack: Kotlin, Firebase, Spotify SDK, OkHttp for network requests.
- API and Database: Uses Firebase for user data storage and retrieval, along with Spotify's REST API for fetching music data.
- Data Flow: The app retrieves Spotify profile data (top tracks, artists) using OAuth and displays it on the user profile to facilitate matchmaking.

Key Features and Functionality

1. Spotify Authentication
- OAuth Integration: The app uses Spotify's OAuth API to authenticate users.
- Data Retrieval: Fetches the user's Spotify profile information, including top tracks, artists, and genres, which are displayed on their profile.

2. Matchmaking Algorithm
- Criteria: Users are matched based on shared top genres and favourite artists, providing peronalised matchmaking experience.

3. User Interface (UI)
- Screenshots:
- User Experience: Simple navigation with streamlined login and profile view, focused on music compatibility.

4. Biometric Authentication
- The app integrates fingerprint-based authentication for secure access. After engabling biometrics, user can log in quickly and securely without entering credentials every time.

Release Notes

1. Changes from Prototype to Final Version
- UI Refinements: Updated the profile and match screens based on user feedback.
- Bug Fixes: Addressed issues with Spotify login and data retrieval.
- New Features: Added fingerprint authentication, real-time notifications, and improved matchmaking criteria.

2. Planned Improvements
- Expanded Matching Criteria: Adding playlist-based matching.
- Advanced Privacy Settings: Allow users to control who can view their profile.

Video Demonstration

A full walkthrough of the app, including registration, Spotify login, and matchmaking, is availabe on YouTube (link).

AI Usage Statement

The profject used AI assistance to address technical challenges, improve code efficiency, and streamline problem-solving. AI helped particularly in:
- Debugging Firebase and Spotify API integrations.
- Implementing biometric and secure authentication flows.

Each AI-assisted code contribution has been reviewed and integrated as per project requirements.

Troubleshooting & Known Issues

- Fingerprint Not Available: Ensure that fingerprint authentication is enabled in device settings.
- Spotify Login Issues: Confirm that Spotify developer credentials are correctly set in 'gradle.properties'.
  
License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
