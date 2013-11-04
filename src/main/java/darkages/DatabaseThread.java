package darkages;

import darkages.character.PlayerCharacter;
import darkages.dao.Database;
import org.bukkit.entity.Player;
import pluginbase.jdbc.SpringJdbcAgent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

class DatabaseThread extends Thread implements Database {

    H2Database database;
    private final BlockingQueue<Callable> profileWriteQueue = new LinkedBlockingQueue<Callable>();
    private final BlockingQueue<Callable> waitingQueue = new LinkedBlockingQueue<Callable>();

    DatabaseThread(SpringJdbcAgent jdbcAgent) {
        super("DA Database Thread");
        database = new H2Database(jdbcAgent);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        // Finish pending db operations
    }

    @Override
    public void run() {
        while(true) {
            try {
                final Callable callable = profileWriteQueue.take();
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException ignore) { }
        }
    }

    @Override
    public long getPlayerId(Player player) {
        return database.getPlayerId(player);
    }

    @Override
    public PlayerCharacter getSelectedCharacter(long playerId) {
        return database.getSelectedCharacter(playerId);
    }
}
