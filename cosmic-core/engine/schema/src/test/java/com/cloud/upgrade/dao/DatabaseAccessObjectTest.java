package com.cloud.upgrade.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseAccessObjectTest {

    private final DatabaseAccessObject dao = new DatabaseAccessObject();
    @Mock
    private PreparedStatement preparedStatementMock;
    @Mock
    private Connection connectionMock;
    @Mock
    private Logger loggerMock;

    @Before
    public void setup() {
        Whitebox.setInternalState(dao, "s_logger", loggerMock);
    }

    @Test
    public void testDropKey() throws Exception {
        when(connectionMock.prepareStatement(contains("DROP KEY"))).thenReturn(preparedStatementMock);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key = "key";
        final boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("successfully"));
    }

    @Test(expected = NullPointerException.class)
    public void testDropKeyWhenConnectionIsNull() throws Exception {
        final Connection conn = null;
        final String tableName = "tableName";
        final String key = "key";
        final boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);
    }

    @Test
    public void testDropKeyWhenTableNameIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("null DROP KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = null;
        final String key = "key";
        final boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropKeyWhenKeyIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP KEY null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key = null;
        final boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropKeyWhenKeysAreForeignKeys() throws Exception {
        when(connectionMock.prepareStatement(contains("DROP FOREIGN KEY"))).thenReturn(preparedStatementMock);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key = "key";
        final boolean isForeignKey = true;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("successfully"));
    }

    @Test
    public void testDropKeyWhenPrepareStatementResultsInException() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(any(String.class))).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key = "key";
        final boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeUpdate();
        verify(preparedStatementMock, times(0)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropKeyWhenExecuteUpdateResultsInException() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key = "key";
        final boolean isForeignKey = false;

        dao.dropKey(conn, tableName, key, isForeignKey);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testClosePreparedStatementWhenPreparedStatementIsNull() throws Exception {
        final PreparedStatement preparedStatement = null;
        final String errorMessage = "some message";

        dao.closePreparedStatement(preparedStatement, errorMessage);

        verify(loggerMock, times(0)).warn(anyString(), any(Throwable.class));
    }

    @Test
    public void testClosePreparedStatementWhenPreparedStatementIsNotNullAndThereIsNoException() throws Exception {
        final PreparedStatement preparedStatement = preparedStatementMock;
        final String errorMessage = "some message";

        dao.closePreparedStatement(preparedStatement, errorMessage);

        verify(preparedStatement, times(1)).close();
        verify(loggerMock, times(0)).warn(anyString(), any(Throwable.class));
    }

    @Test
    public void testClosePreparedStatementWhenPreparedStatementIsNotNullAndThereIsException() throws Exception {
        final SQLException sqlException = new SQLException();
        doThrow(sqlException).when(preparedStatementMock).close();

        final PreparedStatement preparedStatement = preparedStatementMock;
        final String errorMessage = "some message";

        dao.closePreparedStatement(preparedStatement, errorMessage);

        verify(preparedStatement, times(1)).close();
        verify(loggerMock, times(1)).warn(errorMessage, sqlException);
    }

    @Test
    public void testDropPrimaryKey() throws Exception {
        when(connectionMock.prepareStatement(contains("DROP PRIMARY KEY"))).thenReturn(preparedStatementMock);

        final Connection conn = connectionMock;
        final String tableName = "tableName";

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("successfully"));
    }

    @Test(expected = NullPointerException.class)
    public void testDropPrimaryKeyWhenConnectionIsNull() throws Exception {
        final Connection conn = null;
        final String tableName = "tableName";

        dao.dropPrimaryKey(conn, tableName);
    }

    @Test
    public void testDropPrimaryKeyWhenTableNameIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("null DROP PRIMARY KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = null;

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropPrimaryKeyWhenPrepareStatementResultsInException() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP PRIMARY KEY"))).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = null;

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeUpdate();
        verify(preparedStatementMock, times(0)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testDropPrimaryKeyWhenExecuteUpdateResultsInException() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP PRIMARY KEY"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = null;

        dao.dropPrimaryKey(conn, tableName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(contains("Exception"));
    }

    @Test
    public void testColumnExists() throws Exception {
        when(connectionMock.prepareStatement(contains("SELECT"))).thenReturn(preparedStatementMock);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String columnName = "columnName";

        dao.columnExists(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString(), any(Throwable.class));
    }

    @Test(expected = NullPointerException.class)
    public void testColumnExistsWhenConnectionIsNull() throws Exception {
        final Connection conn = null;
        final String tableName = "tableName";
        final String columnName = "columnName";

        dao.columnExists(conn, tableName, columnName);
    }

    @Test
    public void testColumnExistsWhenTableNameIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("FROM null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = null;
        final String columnName = "columnName";

        dao.columnExists(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(anyString());
    }

    @Test
    public void testColumnExistsWhenColumnNameIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("SELECT null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeQuery()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String columnName = null;

        dao.columnExists(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(anyString());
    }

    @Test
    public void testDropColumn() throws Exception {
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeQuery();
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(1)).debug(anyString());
        verify(loggerMock, times(0)).warn(anyString(), any(Throwable.class));
    }

    @Test(expected = NullPointerException.class)
    public void testDropColumnWhenConnectionIsNull() throws Exception {

        final Connection conn = null;
        final String tableName = "tableName";
        final String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);
    }

    @Test
    public void testDropColumnWhenTableNameIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("ALTER TABLE null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = null;
        final String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testDropColumnWhenColumnNameIsNull() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(contains("DROP COLUMN null"))).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String columnName = null;

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testDropColumnWhenPrepareStatementResultsInException() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(anyString())).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(0)).executeUpdate();
        verify(preparedStatementMock, times(0)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }

    @Test
    public void testDropColumnWhenexecuteUpdateResultsInException() throws Exception {
        final SQLException sqlException = new SQLException();
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        when(preparedStatementMock.executeUpdate()).thenThrow(sqlException);

        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String columnName = "columnName";

        dao.dropColumn(conn, tableName, columnName);

        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        verify(preparedStatementMock, times(1)).close();
        verify(loggerMock, times(0)).debug(anyString());
        verify(loggerMock, times(1)).warn(anyString(), eq(sqlException));
    }
}
