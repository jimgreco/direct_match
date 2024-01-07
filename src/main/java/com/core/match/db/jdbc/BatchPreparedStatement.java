package com.core.match.db.jdbc;

import com.core.util.log.Log;

import java.sql.*;

/**
 * Created by hli on 2/17/16.
 */
public class BatchPreparedStatement {
    private final int optimalBatchSize;
    private final Log log;
    private final Connection connection;

    private PreparedStatement preparedStatement;
    private int batchSize;
    private int insertCount;

    public BatchPreparedStatement(PreparedStatement preparedStatement, Log log, Connection connection, int batchSize){
        this.preparedStatement=preparedStatement;
        this.log=log;
        this.connection=connection;
        this.optimalBatchSize=batchSize;
    }

    public void addBatch() throws SQLException {

        preparedStatement.addBatch();
        preparedStatement.clearParameters();
        setParamsAsNull();

        batchSize++;
        if(batchSize==optimalBatchSize){
            log.debug(log.log().add("JDBC batch size reached"));
            executeAndCommitToDB();
        }
    }

    private void setParamsAsNull() throws SQLException {
        preparedStatement.setNull(1,Types.DATE);
        preparedStatement.setNull(2,Types.TIME);
        preparedStatement.setNull(3,Types.BIGINT);
        preparedStatement.setNull(4,Types.INTEGER);
        preparedStatement.setNull(5,Types.VARCHAR);
        preparedStatement.setNull(6,Types.VARCHAR);
        preparedStatement.setNull(7,Types.INTEGER);
        preparedStatement.setNull(8,Types.INTEGER);
        preparedStatement.setNull(9,Types.VARCHAR);
        preparedStatement.setNull(10,Types.VARCHAR);
        preparedStatement.setNull(11,Types.VARCHAR);
        preparedStatement.setNull(12,Types.DATE);
        preparedStatement.setNull(13,Types.BIGINT);
        preparedStatement.setNull(14,Types.BIT);
        preparedStatement.setNull(15,Types.INTEGER);
        preparedStatement.setNull(16,Types.BIGINT);
        preparedStatement.setNull(17,Types.VARCHAR);
        preparedStatement.setNull(18,Types.VARCHAR);
        preparedStatement.setNull(19,Types.INTEGER);
        preparedStatement.setNull(20,Types.INTEGER);
        preparedStatement.setNull(21,Types.BIGINT);
        preparedStatement.setNull(22,Types.INTEGER);
        preparedStatement.setNull(23,Types.BIGINT);
        preparedStatement.setNull(24,Types.BIGINT);
        preparedStatement.setNull(25,Types.BIGINT);
        preparedStatement.setNull(26,Types.VARCHAR);
        preparedStatement.setNull(27,Types.VARCHAR);
        preparedStatement.setNull(28,Types.VARCHAR);
        preparedStatement.setNull(29,Types.VARCHAR);
        preparedStatement.setNull(30,Types.BIT);
        preparedStatement.setNull(31,Types.INTEGER);
        preparedStatement.setNull(32,Types.INTEGER);
        preparedStatement.setNull(33,Types.DATE);
        preparedStatement.setNull(34,Types.DATE);
        preparedStatement.setNull(35,Types.VARCHAR);
        preparedStatement.setNull(36,Types.BIT);
        preparedStatement.setNull(37,Types.INTEGER);
        preparedStatement.setNull(38,Types.INTEGER);
        preparedStatement.setNull(39,Types.INTEGER);
        preparedStatement.setNull(40,Types.BIT);
        preparedStatement.setNull(41,Types.BIT);
        preparedStatement.setNull(42,Types.INTEGER);
        preparedStatement.setNull(43,Types.BIT);
        preparedStatement.setNull(44,Types.VARCHAR);
        preparedStatement.setNull(45,Types.VARCHAR);
        preparedStatement.setNull(46,Types.INTEGER);
        preparedStatement.setNull(47,Types.INTEGER);
        preparedStatement.setNull(48,Types.VARCHAR);
        preparedStatement.setNull(49,Types.VARCHAR);
        preparedStatement.setNull(50,Types.VARCHAR);
        preparedStatement.setNull(51,Types.VARCHAR);
        preparedStatement.setNull(52,Types.BIGINT);
        preparedStatement.setNull(53,Types.BIGINT);
        preparedStatement.setNull(54,Types.TIME);
    }

    public void setString(int columnIndex, String nameAsString) throws SQLException {
        preparedStatement.setString(columnIndex,nameAsString);
    }

    public void setInt(int columnIndex, int netDV01Limit) throws SQLException {
        preparedStatement.setInt(columnIndex, netDV01Limit);
    }

    public void setTimestamp(int i, Timestamp startTimeStamp) throws SQLException {
        preparedStatement.setTimestamp(i, startTimeStamp);
    }

    public void setDate(int columnIndex, Date tradeDate) throws SQLException {
        preparedStatement.setDate(columnIndex, tradeDate);
    }

    public void setLong(int columnIndex, long timestamp) throws SQLException {
        preparedStatement.setLong(columnIndex, timestamp);
    }

    public void execute() throws SQLException {
        preparedStatement.execute();
    }

    public void setBoolean(int columnIndex, boolean isReplace) throws SQLException {
        preparedStatement.setBoolean(columnIndex, isReplace);
    }

    public void setNull(int columnIndex, int integer) throws SQLException {
        preparedStatement.setNull(columnIndex, integer);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getInsertCount() {
        return insertCount;
    }

    public void executeAndCommitToDB() throws SQLException {

        if (batchSize==0) return;

        log.debug(log.log().add("Db Insert:").add(batchSize));
        int [] result=  preparedStatement.executeBatch();

        connection.commit();
        insertCount+=batchSize;
        log.debug(log.log().add("Db Insert Done"));


        if(result.length!=batchSize){
            log.error(log.log().add("Unable full batch write. Expected to write:").add(batchSize).add(" actual:").add(result.length));
        }
        preparedStatement.clearBatch();
        setParamsAsNull();
        batchSize=0;
    }
}
