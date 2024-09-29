package za.co.varsitycollege.st10204772.opsc7312_poe

object loggedUser {
    var user: User? = null

    // Initialize the loggedUser with a User object
    fun initializeUser(user: User) {
        this.user = user
    }

    // Optionally, a method to clear user data when needed
    fun clearUserData() {
        user = null
    }

    // Optionally, a method to update specific fields
    fun updateUserName(newName: String) {
        user?.Name = newName
    }

    // You can add more methods to update other fields as needed
}
