package darkages;

import darkages.character.CharacterStats;
import darkages.character.PlayerCharacter;
import darkages.dao.Database;
import darkages.util.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
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

    public static final long NOT_FOUND_ID = -1L;

    private static final String PLAYER_TABLE = "Player";
    private static final String PLAYER_ID = "PlayerId";
    private static final String PLAYER_NAME = "Name";
    private static final String PLAYER_UUID = "Uuid";
    private static final String CHARACTER_TABLE = "Character";
    private static final String CHARACTER_ID = "CharacterId";
    private static final String CHARACTER_NAME = "Name";
    private static final String CHARACTER_STATS_TABLE = "CharacterStats";
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
    public PlayerCharacter getSelectedCharacter(String playerName) {
        JdbcTemplate jdbcTemplate = agent.createJdbcTemplate();
        long playerId = getPlayerId(jdbcTemplate, playerName);
        if (playerId == NOT_FOUND_ID) {
            return null;
        }
        long characterId = getSelectedCharacterId(jdbcTemplate, playerId);
        CharacterStats stats = getCharacterStats(jdbcTemplate, characterId);
        return new PlayerCharacter(playerId, stats);
    }

    private long getPlayerId(JdbcTemplate jdbcTemplate, String playerName) {
        PlayerIdRetriever idRetriever = new PlayerIdRetriever(playerName);
        jdbcTemplate.query(idRetriever, idRetriever);
        return idRetriever.getId();
    }

    private long getSelectedCharacterId(JdbcTemplate jdbcTemplate, long playerId) {
        CharacterSelectionRetriever idRetriever = new CharacterSelectionRetriever(playerId);
        jdbcTemplate.query(idRetriever, idRetriever);
        return idRetriever.getCharacterId();
    }

    private CharacterStats getCharacterStats(JdbcTemplate jdbcTemplate, long characterId) {
        Log.finest("Retrieving character stats for character id %s", characterId);
        CharacterStatsRetriever statsRetriever = new CharacterStatsRetriever(characterId);
        return jdbcTemplate.query(statsRetriever, statsRetriever);
    }

    @NotNull
    @Override
    public PlayerCharacter createCharacter(String playerName) {
        JdbcTemplate jdbcTemplate = agent.createJdbcTemplate();
        long playerId = createPlayer(jdbcTemplate, playerName);
        long characterId = createCharacter(jdbcTemplate, playerId, playerName);
        selectCharacter(jdbcTemplate, playerId, characterId);
        CharacterStats stats = createCharacterStats(jdbcTemplate, characterId);
        return new PlayerCharacter(playerId, stats);
    }

    private long createPlayer(JdbcTemplate jdbcTemplate, String playerName) {
        Log.fine("Player '%s' not found in database.  Creating new entry.", playerName);
        PlayerIdCreator idCreator = new PlayerIdCreator(playerName);
        KeyHolder generatedKeys = new GeneratedKeyHolder();
        jdbcTemplate.update(idCreator, generatedKeys);
        return generatedKeys.getKey().longValue();
    }

    private long createCharacter(JdbcTemplate jdbcTemplate, long playerId, String characterName) {
        CharacterCreator characterCreator = new CharacterCreator(playerId, characterName);
        KeyHolder generatedKeys = new GeneratedKeyHolder();
        jdbcTemplate.update(characterCreator, generatedKeys);
        return generatedKeys.getKey().longValue();
    }

    private void selectCharacter(JdbcTemplate jdbcTemplate, long playerId, long characterId) {
        jdbcTemplate.update(new CharacterSelector(playerId, characterId));
    }

    private CharacterStats createCharacterStats(JdbcTemplate jdbcTemplate, long characterId) {
        CharacterStats stats = new CharacterStats(characterId);
        jdbcTemplate.update(new CharacterStatsCreator(stats));
        return stats;
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
                    + CHARACTER_ID + " BIGINT NOT NULL PRIMARY KEY, "
                    + "Level TINYINT DEFAULT 1 NOT NULL, "
                    + "Experience BIGINT DEFAULT 0 NOT NULL, "
                    + "MaxHealth INT DEFAULT 50 NOT NULL, "
                    + "MaxMana INT DEFAULT 50 NOT NULL, "
                    + "Health INT DEFAULT 50 NOT NULL, "
                    + "Mana INT DEFAULT 50 NOT NULL, "
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

    static class PlayerIdRetriever implements PreparedStatementCreator, SqlProvider, RowCallbackHandler {

        private String name;
        private long id;

        PlayerIdRetriever(@NotNull String name) {
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
            return "SELECT " + PLAYER_ID + " FROM " + PLAYER_TABLE + " WHERE " + PLAYER_NAME + " = ?";
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            if (id == NOT_FOUND_ID) {
                id = rs.getLong(1);
            }
        }

        public long getId() {
            return id;
        }
    }

    static class PlayerIdCreator implements PreparedStatementCreator, SqlProvider {

        private String name;

        PlayerIdCreator(@NotNull String name) {
            this.name = name;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement = con.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            return statement;
        }

        @Override
        public String getSql() {
            return "INSERT INTO " + PLAYER_TABLE + " (" + PLAYER_NAME + ") VALUES (?)";
        }
    }

    static class CharacterSelectionRetriever implements PreparedStatementCreator, SqlProvider, RowCallbackHandler {

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
            return "SELECT " + CHARACTER_ID + " FROM " + CHARACTER_SELECTION_TABLE + " WHERE " + PLAYER_ID + " = ?";
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

    static class CharacterStatsRetriever implements PreparedStatementCreator, SqlProvider,ResultSetExtractor<CharacterStats> {

        private long characterId;

        CharacterStatsRetriever(long characterId) {
            this.characterId = characterId;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement = con.prepareStatement(getSql());
            statement.setLong(1, characterId);
            return statement;
        }

        @Override
        public String getSql() {
            return "SELECT * FROM " + CHARACTER_STATS_TABLE + " WHERE " + CHARACTER_ID + " = ?";
        }

        @Override
        public CharacterStats extractData(ResultSet rs) throws SQLException, DataAccessException {
            rs.next();
            CharacterStats stats = new CharacterStats(rs.getLong(1));
            stats.setLevel(rs.getByte(2));
            stats.setExp(rs.getLong(3));
            stats.setMaxHealth(rs.getInt(4));
            stats.setMaxMana(rs.getInt(5));
            stats.setHealth(rs.getInt(6));
            stats.setMana(rs.getInt(7));
            stats.setStrength(rs.getShort(8));
            stats.setDexterity(rs.getShort(9));
            stats.setConstitution(rs.getShort(10));
            stats.setIntelligence(rs.getShort(11));
            stats.setWisdom(rs.getShort(12));
            return stats;
        }
    }

    static abstract class CharacterStatsPreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

        static enum Type {
            UPDATE, INSERT
        }

        private CharacterStats stats;
        private Type type;

        CharacterStatsPreparedStatementCreator(@NotNull CharacterStats stats, @NotNull Type type) {
            this.stats = stats;
            this.type = type;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement;
            switch (type) {
                case INSERT:
                    statement = con.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS);
                    break;
                default:
                    statement = con.prepareStatement(getSql());
                    break;
            }
            statement.setByte(1, stats.getLevel());
            statement.setLong(2, stats.getExp());
            statement.setInt(3, stats.getMaxHealth());
            statement.setInt(4, stats.getMaxMana());
            statement.setInt(5, stats.getHealth());
            statement.setInt(6, stats.getMana());
            statement.setShort(7, stats.getStrength());
            statement.setShort(8, stats.getDexterity());
            statement.setShort(9, stats.getConstitution());
            statement.setShort(10, stats.getIntelligence());
            statement.setShort(11, stats.getWisdom());
            statement.setLong(12, stats.getCharacterId());
            return statement;
        }
    }

    static class CharacterStatsCreator extends CharacterStatsPreparedStatementCreator {

        CharacterStatsCreator(@NotNull CharacterStats stats) {
            super(stats, Type.INSERT);
        }

        @Override
        public String getSql() {
            return "INSERT INTO " + CHARACTER_STATS_TABLE + " "
                    + "(Level, Experience, MaxHealth, MaxMana, Health, Mana, Strength, Dexterity, Constitution, Intelligence, Wisdom, " + CHARACTER_ID + ") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }

    static class CharacterStatsUpdater extends CharacterStatsPreparedStatementCreator {

        CharacterStatsUpdater(@NotNull CharacterStats stats) {
            super(stats, Type.UPDATE);
        }

        @Override
        public String getSql() {
            return "UPDATE INTO " + CHARACTER_STATS_TABLE + " SET "
                    + "Level=?, Experience=?, MaxHealth=?, MaxMana=?, Health=?, Mana=?, Strength=?, Dexterity=?, Constitution=?, Intelligence=?, Wisdom=? "
                    + "WHERE " + CHARACTER_ID + "=?";
        }
    }

    static class CharacterCreator implements PreparedStatementCreator, SqlProvider {

        private long playerId;
        private String name;

        CharacterCreator(long playerId, @NotNull String name) {
            this.playerId = playerId;
            this.name = name;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement = con.prepareStatement(getSql(), Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, playerId);
            statement.setString(2, name);
            return statement;
        }

        @Override
        public String getSql() {
            return "INSERT INTO " + CHARACTER_TABLE + " (" + PLAYER_ID + ", " + CHARACTER_NAME + ") VALUES (?, ?)";
        }
    }

    static class CharacterSelector implements PreparedStatementCreator, SqlProvider {

        private long playerId;
        private long characterId;

        CharacterSelector(long playerId, long characterId) {
            this.playerId = playerId;
            this.characterId = characterId;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement statement = con.prepareStatement(getSql());
            statement.setLong(1, playerId);
            statement.setLong(2, characterId);
            return statement;
        }

        @Override
        public String getSql() {
            return "MERGE INTO " + CHARACTER_SELECTION_TABLE + " (" + PLAYER_ID + ", " + CHARACTER_ID + ") VALUES (?, ?)";
        }
    }
}
