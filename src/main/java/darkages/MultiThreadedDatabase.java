package darkages;

import darkages.character.PlayerCharacter;
import darkages.dao.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.jdbc.SpringJdbcAgent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MultiThreadedDatabase implements Database {

    private H2Database database;
    private ExecutorService importantDataExecutor;
    private ExecutorService otherDataExecutor;

    MultiThreadedDatabase(SpringJdbcAgent jdbcAgent) {
        importantDataExecutor = Executors.newSingleThreadExecutor();
        otherDataExecutor = Executors.newSingleThreadExecutor();
        database = new H2Database(jdbcAgent);
    }

    public void shutdown() throws InterruptedException {
        importantDataExecutor.awaitTermination(5L, TimeUnit.MINUTES);
        otherDataExecutor.awaitTermination(5L, TimeUnit.MINUTES);
    }

    @Nullable
    @Override
    public PlayerCharacter getSelectedCharacter(final String playerName) {
        try {
            return importantDataExecutor.submit(new Callable<PlayerCharacter>() {
                @Override
                public PlayerCharacter call() throws Exception {
                    return database.getSelectedCharacter(playerName);
                }
            }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public PlayerCharacter createCharacter(final String playerName) {
        try {
            return importantDataExecutor.submit(new Callable<PlayerCharacter>() {
                @Override
                public PlayerCharacter call() throws Exception {
                    return database.createCharacter(playerName);
                }
            }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
