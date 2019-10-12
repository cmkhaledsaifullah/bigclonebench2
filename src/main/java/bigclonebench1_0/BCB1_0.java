package bigclonebench1_0;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import config.Config;

/**
 * @author  C M Khaled Saifullah
 * The class is responsible for Database connection with BigCloneBench1.0
 *
 * Please specify the @Config.BIGCLONEBENCH_1_0_PATH before running any program
 */

public class BCB1_0 {

    private static BCB1_0 instance = null;

    private BoneCP connectionPool = null;

    /**
     * The Function configure the BigCloneBench jdbc h2 bigclonebench1_0
     * @throws SQLException: If the sql does not find the bigclonebench
     */
    private BCB1_0() throws SQLException {
        BoneCPConfig config = new BoneCPConfig();
        Path db = null;
        db = Paths.get(Config.BIGCLONEBENCH_1_0_PATH).toAbsolutePath();
        config.setJdbcUrl("jdbc:h2:" + db.toString());
        config.setUsername("sa");
        config.setPassword("");
        config.setMinConnectionsPerPartition(1);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(1);
        connectionPool = new BoneCP(config);
    }

    /**
     * The function calls the
     * @return the instance of the BigCloneBench bigclonebench1_0
     * @throws SQLException: If the bigclonebench1_0 is not found
     */
    private static BCB1_0 getConnectionPool() throws SQLException {
        if(instance == null)
            instance = new BCB1_0();
        return instance;
    }

    /**
     *  @return The connectionpool of bigclonebench1_0
     *  @throws SQLException If the sql does not find the bigclonebench
     */
    public static Connection getConnection() throws SQLException {

        return BCB1_0.getConnectionPool().connectionPool.getConnection();
    }

}
