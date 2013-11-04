package darkages;

import darkages.character.CharacterStats;
import darkages.character.PlayerCharacter;
import darkages.dao.Database;
import darkages.util.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import pluginbase.jdbc.SpringJdbcAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


class H2Database implements Database {

    private static final String PLAYER_TABLE = "Player";
    private static final String PLAYER_ID = "PlayerId";
    private static final String PLAYER_NAME = "Name";
    private static final String PLAYER_UUID = "Uuid";
    private static final String CHARACTER_TABLE = "Character";
    private static final String CHARACTER_ID = "CharacterId";
    private static final String CHARACTER_NAME = "Name";
    private static final String CHARACTER_STATS_TABLE = "CharacterStats";
    private static final String CHARACTER_STATS_ID = "CharacterStatsId";
    private static final String CHARACTER_SELECTION_TABLE = "CharacterSelection";

    @NotNull
    private pluginbase.jdbc.SpringJdbcAgent agent;

    H2Database(SpringJdbcAgent jdbcAgent) {
        this.agent = jdbcAgent;
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        JdbcTemplate jdbcTemplate = agent.createJdbcTemplate();
        jdbcTemplate.execute(new PlayerTable());
        jdbcTemplate.execute(new CharacterTable());
        jdbcTemplate.execute(new CharacterStatsTable());
        jdbcTemplate.execute(new CharacterSelectionTable());
    }

    @Override
    public long getPlayerId(Player player) {
        JdbcTemplate jdbcTemplate = agent.createJdbcTemplate();
        PlayerId idRetriever = new PlayerId(player.getName());
        return idRetriever.getId(jdbcTemplate);
    }

    @Override
    public PlayerCharacter getSelectedCharacter(long playerId) {

        // TODO
        return new PlayerCharacter(playerId, new CharacterStats());
    }

    static class PlayerTable implements StatementCallback, SqlProvider {

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DataAccessException {
            stmt.execute(getSql());
            return null;
        }

        @Override
        public String getSql() {
            return "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE + " ("
                    + PLAYER_ID + " BIGINT IDENTITY, "
                    + PLAYER_NAME + " VARCHAR(16) UNIQUE, "
                    + PLAYER_UUID + " UUID UNIQUE, "
                    + "CHECK (" + PLAYER_NAME + " IS NOT NULL OR " + PLAYER_UUID + " IS NOT NULL)"
                    + ")";
        }
    }

    static class CharacterTable implements StatementCallback, SqlProvider {

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DataAccessException {
            stmt.execute(getSql());
            return null;
        }

        @Override
        public String getSql() {
            return "CREATE TABLE IF NOT EXISTS " + CHARACTER_TABLE + " ("
                    + CHARACTER_ID + " BIGINT IDENTITY, "
                    + PLAYER_ID + " BIGINT NOT NULL, "
                    + CHARACTER_NAME + " VARCHAR(64) UNIQUE NOT NULL, "
                    + "FOREIGN KEY (" + PLAYER_ID + ") REFERENCES " + PLAYER_TABLE + "(" + PLAYER_ID + ")"
                    + ")";
        }
    }

    static class CharacterStatsTable implements StatementCallback, SqlProvider {

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DataAccessException {
            stmt.execute(getSql());
            return null;
        }

        @Override
        public String getSql() {
            return "CREATE TABLE IF NOT EXISTS " + CHARACTER_STATS_TABLE + " ("
                    + CHARACTER_STATS_ID + " BIGINT IDENTITY, "
                    + CHARACTER_ID + " BIGINT NOT NULL, "
                    + "Level TINYINT DEFAULT 1 NOT NULL, "
                    + "Experience BIGINT DEFAULT 0 NOT NULL, "
                    + "MaxHp INT DEFAULT 50 NOT NULL, "
                    + "MaxMp INT DEFAULT 50 NOT NULL, "
                    + "CurrentHp INT DEFAULT 50 NOT NULL, "
                    + "CurrentMp INT DEFAULT 50 NOT NULL, "
                    + "Strength SMALLINT DEFAULT 3 NOT NULL, "
                    + "Dexterity SMALLINT DEFAULT 3 NOT NULL, "
                    + "Constitution SMALLINT DEFAULT 3 NOT NULL, "
                    + "Intelligence SMALLINT DEFAULT 3 NOT NULL, "
                    + "Wisdom SMALLINT DEFAULT 3 NOT NULL, "
                    + "FOREIGN KEY (" + CHARACTER_ID + ") REFERENCES " + CHARACTER_TABLE + "(" + CHARACTER_ID + ")"
                    + ")";
        }
    }

    static class CharacterSelectionTable implements StatementCallback, SqlProvider {

        @Override
        public Object doInStatement(Statement stmt) throws SQLException, DataAccessException {
            stmt.execute(getSql());
            return null;
        }

        @Override
        public String getSql() {
            return "CREATE TABLE IF NOT EXISTS " + CHARACTER_SELECTION_TABLE + " ("
                    + PLAYER_ID + " BIGINT NOT NULL PRIMARY KEY, "
                    + CHARACTER_ID + " BIGINT NOT NULL UNIQUE, "
                    + "FOREIGN KEY (" + PLAYER_ID + ") REFERENCES " + PLAYER_TABLE + "(" + PLAYER_ID + "), "
                    + "FOREIGN KEY (" + CHARACTER_ID + ") REFERENCES " + CHARACTER_TABLE + "(" + CHARACTER_ID + ")"
                    + ")";
        }
    }

    static class PlayerId implements PreparedStatementCreator, SqlProvider, RowCallbackHandler {

        private static final String GET_ID = "SELECT " + PLAYER_ID + " FROM " + PLAYER_TABLE + " WHERE " + PLAYER_NAME + " = ?";
        private static final String CREATE_ID = "INSERT INTO " + PLAYER_TABLE + " (" + PLAYER_NAME + ") VALUES (?)";

        private static final long NOT_FOUND_ID = -1L;

        private String name;
        private long id;

        private String currentSql;

        PlayerId(@NotNull String name) {
            this.name = name;
            this.id = NOT_FOUND_ID;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement = con.prepareStatement(getSql());
            statement.setString(1, name);
            return statement;
        }

        @Override
        public String getSql() {
            return currentSql;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            if (id == NOT_FOUND_ID) {
                id = rs.getLong(1);
            }
        }

        public long getId(@NotNull JdbcTemplate template) {
            currentSql = GET_ID;
            template.query(this, this);
            if (id == NOT_FOUND_ID) {
                id = createId(template);
                Log.fine("Created player id '%s' for '%s'", id, name);
            }
            return id;
        }

        private long createId(JdbcTemplate template) {
            currentSql = CREATE_ID;
            KeyHolder generatedKeys = new GeneratedKeyHolder();
            template.update(this, generatedKeys);
            return generatedKeys.getKey().longValue();
        }
    }

    static class CharacterSelectionRetriever implements PreparedStatementCreator, SqlProvider, RowCallbackHandler {

        public static final long NOT_FOUND_ID = -1L;

        private long playerId;
        private long characterId;

        CharacterSelectionRetriever(long playerId) {
            this.playerId = playerId;
            this.characterId = NOT_FOUND_ID;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement = con.prepareStatement(getSql());
            statement.setLong(1, playerId);
            return statement;
        }

        @Override
        public String getSql() {
            return "SELECT " + PLAYER_ID + " FROM " + PLAYER_TABLE + " WHERE " + PLAYER_NAME + " = ?";
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            if (characterId == NOT_FOUND_ID) {
                characterId = rs.getLong(1);
            }
        }

        public long getCharacterId() {
            return characterId;
        }
    }
}
