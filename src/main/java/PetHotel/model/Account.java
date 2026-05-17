package PetHotel.model;

public class Account {
    private String username;
    private String password;
    private String fullName;
    private String role; 

    //cần thiết để lấy dữ liệu từ db
    public Account() {
    }

    // 3. Hàm khởi tạo đầy đủ tham số
    public Account(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // 4. Getter và Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Hàm toString() dùng để test in ra console xem dữ liệu có lấy lên đúng không
    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}'; 

    }
}