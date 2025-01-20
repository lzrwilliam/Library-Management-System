public class User extends Person implements Persistable {
    private static int idCounter = 1;
    private int id;
    private UserRole role;

    public User(String name, String surname, UserRole role) {
        this.id = idCounter++;
        this.name = name;
        this.surname = surname;
        this.role = role;
    }

    @Override
    public int getId() {
        return id;
    }
    public UserRole getRole() {return role;}

    @Override
    public String toCSVString() {
        return id + "," + getName() + "," + getSurname() + "," + role;
    }

    public static User fromCSVString(String[] data) {
        if (data.length < 4) throw new IllegalArgumentException("Invalid data for User");
        int id = Integer.parseInt(data[0].trim());
        String name = data[1].trim();
        String surname = data[2].trim();
        UserRole role = UserRole.valueOf(data[3].trim());
        User user = new User(name, surname, role);
        user.id = id;
        return user;
    }



    public void setRole(UserRole role) {
        this.role = role;
    }
    public void setId(int id) {
        this.id = id;
    }

}
