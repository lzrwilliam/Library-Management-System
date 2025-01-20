public class SessionManager { // FOLOISTA CA SA RETINEM GLOBAL USER SELECTAT IN GUI
    private static SessionManager instance;
    private User currentUser;
    private int selectedUserId;
    private String selectedUserName;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        this.selectedUserId = currentUser.getId();
        this.selectedUserName = currentUser.getName();
    }

//    public int getSelectedUserId() {
//        return selectedUserId;
//    }
//
//    public String getSelectedUserName() {
//        return selectedUserName;
//    }
}
