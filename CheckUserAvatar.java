import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckUserAvatar {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/sport_equipment?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String query = "SELECT username, avatar FROM user WHERE username = 'admin' OR username = 'user1'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String user = rs.getString("username");
                String avatar = rs.getString("avatar");
                System.out.println("Username: " + user + ", Avatar: " + (avatar != null ? avatar : "null"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}