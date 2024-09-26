package za.co.varsitycollege.st10204772.opsc7312_poe

object loggedUser {
    var name: String? = null
    var cellNo: String? = null

    // Initialize the user (optional, depends on how you want to use it)
    fun initializeUser(userName: String, userCellNo: String) {
        name = userName
        cellNo = userCellNo
    }

    // Optionally, a method to clear user data when needed
    fun clearUserData() {
        name = null
        cellNo = null
    }
}