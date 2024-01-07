package com.core.match.db.jdbc;


import com.core.app.Exposed;
import com.core.util.log.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class DatabaseAdaptor {

    private final String tableName;

    private final Log log;
    protected Connection dbConnection;
    private PreparedStatement preparedStatement;

    public DatabaseAdaptor(String driver, String connectionString, Log log, String query, String tableName){
        this.log=log;
        this.tableName=tableName;
        try {
            dbConnection = DriverManager.getConnection(connectionString);
            dbConnection.setAutoCommit(false);
            preparedStatement=dbConnection.prepareStatement(query);
        } catch (SQLException  e) {
            log.error(log.log().add("Db Adaptor instantiation exception:").add(e.getMessage()));
        }
    }

    @Exposed(name="closeDBConnection")
    public void close() throws SQLException {
        dbConnection.close();
    }

    public PreparedStatement getPreparedStatement(){
        return preparedStatement;
    }



    public void executeAndCommitToDB() throws SQLException {
        int [] result=  preparedStatement.executeBatch();
        dbConnection.commit();
        log.debug(log.log().add("Db Insert :").add(result.length).add(" records into ").add(tableName));
        preparedStatement.clearBatch();

    }

}
