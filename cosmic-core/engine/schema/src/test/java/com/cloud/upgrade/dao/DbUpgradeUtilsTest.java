package com.cloud.upgrade.dao;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
public class DbUpgradeUtilsTest {

    @Mock
    private Connection connectionMock;

    @Mock
    private DatabaseAccessObject daoMock;

    @Before
    public void setupClass() {
        Whitebox.setInternalState(DbUpgradeUtils.class, "dao", daoMock);
    }

    @Test
    public void testDropKeyIfExistWhenNoKeysAreSupplied() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final List<String> keys = new ArrayList<>();
        final boolean isForeignKey = false;

        DbUpgradeUtils.dropKeysIfExist(conn, tableName, keys, isForeignKey);

        verify(daoMock, times(0)).dropKey(eq(conn), eq(tableName), anyString(), eq(isForeignKey));
    }

    @Test
    public void testDropKeyIfExistWhenOneKeysIsSupplied() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key = "key";
        final List<String> keys = Arrays.asList(new String[]{key});
        final boolean isForeignKey = false;

        DbUpgradeUtils.dropKeysIfExist(conn, tableName, keys, isForeignKey);

        verify(daoMock, times(1)).dropKey(conn, tableName, key, isForeignKey);
    }

    @Test
    public void testDropKeyIfExistWhenThreeKeysAreSupplied() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String key1 = "key1";
        final String key2 = "key2";
        final List<String> keys = Arrays.asList(new String[]{key1, key2});
        final boolean isForeignKey = false;

        DbUpgradeUtils.dropKeysIfExist(conn, tableName, keys, isForeignKey);

        verify(daoMock, times(1)).dropKey(conn, tableName, key1, isForeignKey);
        verify(daoMock, times(1)).dropKey(conn, tableName, key2, isForeignKey);
    }

    @Test
    public void testDropPrimaryKey() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";

        DbUpgradeUtils.dropPrimaryKeyIfExists(conn, tableName);

        verify(daoMock, times(1)).dropPrimaryKey(conn, tableName);
    }

    @Test
    public void testDropTableColumnsIfExistWhenNoKeysAreSupplied() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final List<String> columns = new ArrayList<>();

        DbUpgradeUtils.dropTableColumnsIfExist(conn, tableName, columns);

        verify(daoMock, times(0)).columnExists(eq(conn), eq(tableName), anyString());
        verify(daoMock, times(0)).dropColumn(eq(conn), eq(tableName), anyString());
    }

    @Test
    public void testDropTableColumnsIfExistWhenOneKeysIsSuppliedAndColumnExists() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String column = "column";
        when(daoMock.columnExists(conn, tableName, column)).thenReturn(true);
        final List<String> columns = Arrays.asList(new String[]{column});

        DbUpgradeUtils.dropTableColumnsIfExist(conn, tableName, columns);

        verify(daoMock, times(1)).columnExists(conn, tableName, column);
        verify(daoMock, times(1)).dropColumn(conn, tableName, column);
    }

    @Test
    public void testDropTableColumnsIfExistWhenOneKeysIsSuppliedAndColumnDoesNotExists() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String column = "column";
        when(daoMock.columnExists(conn, tableName, column)).thenReturn(false);
        final List<String> columns = Arrays.asList(new String[]{column});

        DbUpgradeUtils.dropTableColumnsIfExist(conn, tableName, columns);

        verify(daoMock, times(1)).columnExists(conn, tableName, column);
        verify(daoMock, times(0)).dropColumn(conn, tableName, column);
    }

    @Test
    public void testDropTableColumnsIfExistWhenThreeKeysAreSuppliedAnOneDoesnotExist() throws Exception {
        final Connection conn = connectionMock;
        final String tableName = "tableName";
        final String column1 = "column1";
        final String column2 = "column2";
        final String column3 = "column3";
        when(daoMock.columnExists(conn, tableName, column1)).thenReturn(true);
        when(daoMock.columnExists(conn, tableName, column2)).thenReturn(false);
        when(daoMock.columnExists(conn, tableName, column3)).thenReturn(true);
        final List<String> keys = Arrays.asList(new String[]{column1, column2, column3});

        DbUpgradeUtils.dropTableColumnsIfExist(conn, tableName, keys);

        verify(daoMock, times(1)).columnExists(conn, tableName, column1);
        verify(daoMock, times(1)).dropColumn(conn, tableName, column1);
        verify(daoMock, times(1)).columnExists(conn, tableName, column2);
        verify(daoMock, times(0)).dropColumn(conn, tableName, column2);
        verify(daoMock, times(1)).columnExists(conn, tableName, column3);
        verify(daoMock, times(1)).dropColumn(conn, tableName, column3);
    }
}
