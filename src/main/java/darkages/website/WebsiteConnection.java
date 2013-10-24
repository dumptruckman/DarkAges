package darkages.website;

import darkages.DarkAgesPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class WebsiteConnection {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b\\S+@\\S+\\.[a-zA-Z]{2,4}\\b");

    private static final String CREATE_USER = "INSERT INTO `wp_users` "
            + "(`user_login`, `user_pass`, `user_nicename`, `user_email`, `user_registered`, `user_status`, `display_name`) "
            + "VALUES (?, MD5(?), ?, ?, NOW(), '0', ?)";

    private static final String CREATE_CAPABILITIES = "INSERT INTO `wp_usermeta` "
            + "(`user_id`, `meta_key`, `meta_value`) "
            + "VALUES (?, 'wp_capabilities', 'a:1:{s:10:\"subscriber\";s:1:\"1\";}')";

    private static final String CREATE_USER_LEVEL = "INSERT INTO `wp_usermeta` "
            + "(`user_id`, `meta_key`, `meta_value`) "
            + "VALUES (?, 'wp_user_level', '0')";

    private static final String SELECT_USER = "SELECT * FROM `wp_users` WHERE `user_login`=?";

    private final DarkAgesPlugin plugin;
    private final String playerName;

    private Connection wpConn = null;

    public WebsiteConnection(final DarkAgesPlugin plugin, final Player player) throws ClassNotFoundException {
        this.plugin = plugin;
        this.playerName = player.getName();
        Class.forName("com.mysql.jdbc.Driver");
    }

    private Connection getWebsiteSQLConnection() throws SQLException {
        if (wpConn == null || !wpConn.isValid(10)) {
            if (wpConn != null) {
                wpConn.close();
            }
            wpConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gnarbros", "root", "flarto15");
        }
        return wpConn;
    }

    public boolean isUserRegistered() throws SQLException {
        try (final PreparedStatement statement = getWebsiteSQLConnection().prepareStatement(SELECT_USER)) {
            statement.setString(1, playerName);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
    }

    public void closeConnection() throws SQLException {
        getWebsiteSQLConnection().close();
    }

    public boolean isValidEmailAddress(@NotNull final String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public void registerUser(@NotNull final String password, @NotNull final String email) throws Exception {
        try {
            if (isUserRegistered()) {
                throw new Exception(playerName + " is already a registered user!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Something went wrong! Contact dumptruckman.");
        }
        Connection conn = null;
        try {
            // Get the connection and prepare the statements
            conn = getWebsiteSQLConnection();

            // Turn off auto-commit mode
            conn.setAutoCommit(false);

            // Set up the create user statement
            PreparedStatement createUser = conn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS);
            createUser.setString(1, playerName);
            createUser.setString(2, password);
            createUser.setString(3, playerName);
            createUser.setString(4, email);
            createUser.setString(5, playerName);

            // Send the create user statement and attempt to get the generated key
            createUser.executeUpdate();
            ResultSet rs = createUser.getGeneratedKeys();

            if (rs == null || !rs.next()) {
                // The create user statement probably failed for some reason so undo it
                conn.rollback();
            } else {
                long key = rs.getLong(1);

                // Set up and send the user capabilities statement
                PreparedStatement createCapabilities = conn.prepareStatement(CREATE_CAPABILITIES);
                createCapabilities.setLong(1, key);
                createCapabilities.executeUpdate();

                // Set up and send the user level statement
                PreparedStatement createUserLevel = conn.prepareStatement(CREATE_USER_LEVEL);
                createUserLevel.setLong(1, key);
                createUserLevel.executeUpdate();

                // Commit all the changes to the db.
                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    // If any issues occurred during the above roll back any changes
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    throw new Exception("Something went wrong! Contact dumptruckman.");
                }
            }
            e.printStackTrace();
            throw new Exception("Something went wrong! Contact dumptruckman.");
        } finally {
            if (conn != null) {
                try {
                    // Turn autocommit back on and close the connection
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
