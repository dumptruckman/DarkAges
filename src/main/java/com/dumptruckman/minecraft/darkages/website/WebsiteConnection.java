package com.dumptruckman.minecraft.darkages.website;

import com.dumptruckman.minecraft.darkages.DarkAgesPlugin;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WebsiteConnection {

    private static final String CREATE_USER = "INSERT INTO `wp_users` "
            + "(`user_login`, `user_pass`, `user_nicename`, `user_email`, `user_registered`, `user_status`, `display_name`) "
            + "VALUES ('?', MD5('?'), '?', '?', NOW(), '0', '?')";

    private static final String CREATE_CAPABILITIES = "INSERT INTO `wp_usermeta` "
            + "(`user_id`, `meta_key`, `meta_value`) "
            + "VALUES ('?', 'wp_capabilities', 'a:1:{s:10:\"subscriber\";s:1:\"1\";}')";

    private static final String CREATE_USER_LEVEL = "INSERT INTO `wp_usermeta` "
            + "(`user_id`, `meta_key`, `meta_value`) "
            + "VALUES ('?', 'wp_user_level', '0')";

    private static final String SELECT_USER = "SELECT * FROM `wp_users` WHERE `user_login`='?'";

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

    private boolean isUserRegistered() throws SQLException {
        try (final PreparedStatement statement = getWebsiteSQLConnection().prepareStatement(SELECT_USER)) {
            statement.setString(1, playerName);
            final ResultSet result = statement.executeQuery();
            return result.next();
        }
    }
}
