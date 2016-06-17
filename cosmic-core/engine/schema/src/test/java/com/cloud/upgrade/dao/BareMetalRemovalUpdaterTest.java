package com.cloud.upgrade.dao;

import com.cloud.utils.exception.CloudRuntimeException;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RunWith(PowerMockRunner.class)
public class BareMetalRemovalUpdaterTest {

    private final BareMetalRemovalUpdater bareMetalRemovalUpdater = new BareMetalRemovalUpdater();

    @Mock
    private ResultSet values;

    @Mock
    private Connection dbConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    public void test_hasReferenceToBareMetal_isFalse() throws Exception {
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal(""), is(false));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("something"), is(false));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("baremeta"), is(false));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("aremetal"), is(false));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("baretal"), is(false));
    }

    @Test
    public void test_hasReferenceToBareMetal_isTrue() throws Exception {
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("baremetal"), is(true));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("BAREMETAL"), is(true));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("BaReMeTaL"), is(true));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("somethingbaremetal"), is(true));
        assertThat(bareMetalRemovalUpdater.hasReferenceToBareMetal("somethingbaremetalsomethingelse"), is(true));
    }

    @Test
    public void test_createUpdatedValue_updatesWithSingleValue() throws Exception {
        assertThat(bareMetalRemovalUpdater.createUpdatedValue("baremetal"), is(equalTo("")));
        assertThat(bareMetalRemovalUpdater.createUpdatedValue("baremetal,"), is(equalTo("")));
        assertThat(bareMetalRemovalUpdater.createUpdatedValue(",baremetal,"), is(equalTo("")));
    }

    @Test
    public void test_createUpdatedValue_updatesWithMultipleValues() throws Exception {
        assertThat(bareMetalRemovalUpdater.createUpdatedValue("hypervisor_a,baremetal"), is(equalTo("hypervisor_a")));
        assertThat(bareMetalRemovalUpdater.createUpdatedValue("baremetal,hypervisor_b"), is(equalTo("hypervisor_b")));
        assertThat(bareMetalRemovalUpdater.createUpdatedValue("BAREMETAL,hypervisor_b"), is(equalTo("hypervisor_b")));
        assertThat(bareMetalRemovalUpdater.createUpdatedValue("hypervisor_a,baremetal,hypervisor_b"), is(equalTo("hypervisor_a,hypervisor_b")));
    }

    @Test
    public void test_findAndUpdateRowsThatReferenceBareMetal_whenThereAreNoRows() throws Exception {
        when(values.next()).thenReturn(false);
        final List<Map<String, String>> updatedRows = new ArrayList<>();

        bareMetalRemovalUpdater.findAndUpdateRowsThatReferenceBareMetal(values, updatedRows);

        assertThat(updatedRows, hasSize(0));
    }

    @Test
    public void test_findAndUpdateRowsThatReferenceBareMetal_whenThereIsOneRowThatDoesNotReferenceBareMetal() throws Exception {
        when(values.next())
                .thenReturn(true)
                .thenReturn(false);
        when(values.getString(BareMetalRemovalUpdater.VALUE)).thenReturn("someValue");
        final List<Map<String, String>> updatedRows = new ArrayList<>();

        bareMetalRemovalUpdater.findAndUpdateRowsThatReferenceBareMetal(values, updatedRows);

        assertThat(updatedRows, hasSize(0));
    }

    @Test
    public void test_findAndUpdateRowsThatReferenceBareMetal_whenThereIsOneRowThatReferencesBareMetal() throws Exception {
        when(values.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(values.getString(BareMetalRemovalUpdater.VALUE))
                .thenReturn("someValue")
                .thenReturn("hypervisor_a,baremetal");
        when(values.getString(BareMetalRemovalUpdater.CATEGORY))
                .thenReturn("someCategory");
        when(values.getString(BareMetalRemovalUpdater.INSTANCE))
                .thenReturn("someInstance");
        when(values.getString(BareMetalRemovalUpdater.COMPONENT))
                .thenReturn("someComponent");
        when(values.getString(BareMetalRemovalUpdater.NAME))
                .thenReturn("someName");
        final List<Map<String, String>> updatedRows = new ArrayList<>();

        bareMetalRemovalUpdater.findAndUpdateRowsThatReferenceBareMetal(values, updatedRows);

        assertThat(updatedRows, hasSize(1));
        assertThat(updatedRows, contains(
                allOf(
                        hasEntry(BareMetalRemovalUpdater.VALUE, "hypervisor_a"),
                        hasEntry(BareMetalRemovalUpdater.CATEGORY, "someCategory"),
                        hasEntry(BareMetalRemovalUpdater.INSTANCE, "someInstance"),
                        hasEntry(BareMetalRemovalUpdater.COMPONENT, "someComponent"),
                        hasEntry(BareMetalRemovalUpdater.NAME, "someName")
                )
        ));
    }

    @Test
    public void test_findAndUpdateRowsThatReferenceBareMetal_whenThereAreTwoRowsThatReferencesBareMetal() throws Exception {
        when(values.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(values.getString(BareMetalRemovalUpdater.VALUE))
                .thenReturn("BAREMETAL,hypervisor_a")
                .thenReturn("someValue")
                .thenReturn("hypervisor_b,baremetal");
        when(values.getString(BareMetalRemovalUpdater.CATEGORY))
                .thenReturn("someCategory")
                .thenReturn("anotherCategory");
        when(values.getString(BareMetalRemovalUpdater.INSTANCE))
                .thenReturn("someInstance")
                .thenReturn("anotherInstance");
        when(values.getString(BareMetalRemovalUpdater.COMPONENT))
                .thenReturn("someComponent")
                .thenReturn("anotherComponent");
        when(values.getString(BareMetalRemovalUpdater.NAME))
                .thenReturn("someName")
                .thenReturn("anotherName");
        final List<Map<String, String>> updatedRows = new ArrayList<>();

        bareMetalRemovalUpdater.findAndUpdateRowsThatReferenceBareMetal(values, updatedRows);

        assertThat(updatedRows, hasSize(2));
        assertThat(updatedRows, contains(
                allOf(
                        hasEntry(BareMetalRemovalUpdater.VALUE, "hypervisor_a"),
                        hasEntry(BareMetalRemovalUpdater.CATEGORY, "someCategory"),
                        hasEntry(BareMetalRemovalUpdater.INSTANCE, "someInstance"),
                        hasEntry(BareMetalRemovalUpdater.COMPONENT, "someComponent"),
                        hasEntry(BareMetalRemovalUpdater.NAME, "someName")
                ),
                allOf(
                        hasEntry(BareMetalRemovalUpdater.VALUE, "hypervisor_b"),
                        hasEntry(BareMetalRemovalUpdater.CATEGORY, "anotherCategory"),
                        hasEntry(BareMetalRemovalUpdater.INSTANCE, "anotherInstance"),
                        hasEntry(BareMetalRemovalUpdater.COMPONENT, "anotherComponent"),
                        hasEntry(BareMetalRemovalUpdater.NAME, "anotherName")
                )
        ));
    }

    @Test
    public void test_updateConfigurationItem_whenThereIsNoException() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.UPDATE_CONFIGURATION_ITEM)).thenReturn(preparedStatement);

        bareMetalRemovalUpdater.updateConfigurationItem(dbConnection, "someCategory", "someInstance", "someComponent", "someName", "someValue");

        verify(preparedStatement, times(5)).setString(anyInt(), anyString());
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test(expected = CloudRuntimeException.class)
    public void test_updateConfigurationItem_whenThereAnException() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.UPDATE_CONFIGURATION_ITEM)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);

        bareMetalRemovalUpdater.updateConfigurationItem(dbConnection, "someCategory", "someInstance", "someComponent", "someName", "someValue");

        verify(preparedStatement, times(5)).setString(anyInt(), anyString());
    }

    @Test
    public void test_updateConfigurationItems_whenThereAreNoUpdates() throws Exception {
        bareMetalRemovalUpdater.updateConfigurationItems(dbConnection, Collections.<Map<String, String>>emptyList());

        verify(dbConnection, never()).prepareStatement(anyString());
    }

    @Test
    public void test_updateConfigurationItems_whenThereIsOneUpdate() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.UPDATE_CONFIGURATION_ITEM)).thenReturn(preparedStatement);
        final List<Map<String, String>> updatedRows = new ArrayList<>();
        final HashMap<String, String> updatedRow = new HashMap<>();
        updatedRow.put(BareMetalRemovalUpdater.VALUE, "hypervisor_a");
        updatedRow.put(BareMetalRemovalUpdater.CATEGORY, "someCategory");
        updatedRow.put(BareMetalRemovalUpdater.INSTANCE, "someInstance");
        updatedRow.put(BareMetalRemovalUpdater.COMPONENT, "someComponent");
        updatedRow.put(BareMetalRemovalUpdater.NAME, "someName");
        updatedRows.add(updatedRow);

        bareMetalRemovalUpdater.updateConfigurationItems(dbConnection, updatedRows);

        verify(preparedStatement, times(5)).setString(anyInt(), anyString());
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void test_updateConfigurationItems_whenThereAreTwoUpdates() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.UPDATE_CONFIGURATION_ITEM)).thenReturn(preparedStatement);
        final List<Map<String, String>> updatedRows = new ArrayList<>();
        final HashMap<String, String> updatedRow1 = new HashMap<>();
        updatedRow1.put(BareMetalRemovalUpdater.VALUE, "hypervisor_a");
        updatedRow1.put(BareMetalRemovalUpdater.CATEGORY, "someCategory");
        updatedRow1.put(BareMetalRemovalUpdater.INSTANCE, "someInstance");
        updatedRow1.put(BareMetalRemovalUpdater.COMPONENT, "someComponent");
        updatedRow1.put(BareMetalRemovalUpdater.NAME, "someName");
        final HashMap<String, String> updatedRow2 = new HashMap<>();
        updatedRow1.put(BareMetalRemovalUpdater.VALUE, "hypervisor_b");
        updatedRow1.put(BareMetalRemovalUpdater.CATEGORY, "anotherCategory");
        updatedRow1.put(BareMetalRemovalUpdater.INSTANCE, "anotherInstance");
        updatedRow1.put(BareMetalRemovalUpdater.COMPONENT, "anotherCmponent");
        updatedRow1.put(BareMetalRemovalUpdater.NAME, "anotherName");
        updatedRows.add(updatedRow1);
        updatedRows.add(updatedRow2);

        bareMetalRemovalUpdater.updateConfigurationItems(dbConnection, updatedRows);

        verify(preparedStatement, times(10)).setString(anyInt(), anyString());
        verify(preparedStatement, times(2)).executeUpdate();
    }

    @Test
    public void test_createUpdatedConfigurationItemValues_whenThereAreNoConfigurationItems() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.SELECT_CONFIGURATION_ITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(values);
        when(values.next()).thenReturn(false);

        final List<Map<String, String>> updatedRows = bareMetalRemovalUpdater.createUpdatedConfigurationItemValues(dbConnection);

        assertThat(updatedRows, hasSize(0));
    }

    @Test
    public void test_createUpdatedConfigurationItemValues_whenThereAreConfigurationItems() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.SELECT_CONFIGURATION_ITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(values);
        when(values.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(values.getString(BareMetalRemovalUpdater.VALUE))
                .thenReturn("BAREMETAL,hypervisor_a")
                .thenReturn("hypervisor_b,baremetal");
        when(values.getString(BareMetalRemovalUpdater.CATEGORY))
                .thenReturn("someCategory")
                .thenReturn("anotherCategory");
        when(values.getString(BareMetalRemovalUpdater.INSTANCE))
                .thenReturn("someInstance")
                .thenReturn("anotherInstance");
        when(values.getString(BareMetalRemovalUpdater.COMPONENT))
                .thenReturn("someComponent")
                .thenReturn("anotherComponent");
        when(values.getString(BareMetalRemovalUpdater.NAME))
                .thenReturn("someName")
                .thenReturn("anotherName");

        final List<Map<String, String>> updatedRows = bareMetalRemovalUpdater.createUpdatedConfigurationItemValues(dbConnection);

        assertThat(updatedRows, hasSize(2));
    }

    @Test
    public void test_updateConfigurationValuesThatReferenceBareMetal_whenThereAreNoConfigurationItems() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.SELECT_CONFIGURATION_ITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(values);
        when(values.next()).thenReturn(false);

        bareMetalRemovalUpdater.updateConfigurationValuesThatReferenceBareMetal(dbConnection);

        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    public void test_updateConfigurationValuesThatReferenceBareMetal_whenThereAreConfigurationItems() throws Exception {
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.SELECT_CONFIGURATION_ITEMS)).thenReturn(preparedStatement);
        when(dbConnection.prepareStatement(BareMetalRemovalUpdater.UPDATE_CONFIGURATION_ITEM)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(values);
        when(values.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(values.getString(BareMetalRemovalUpdater.VALUE))
                .thenReturn("BAREMETAL,hypervisor_a")
                .thenReturn("hypervisor_b,baremetal");
        when(values.getString(BareMetalRemovalUpdater.CATEGORY))
                .thenReturn("someCategory")
                .thenReturn("anotherCategory");
        when(values.getString(BareMetalRemovalUpdater.INSTANCE))
                .thenReturn("someInstance")
                .thenReturn("anotherInstance");
        when(values.getString(BareMetalRemovalUpdater.COMPONENT))
                .thenReturn("someComponent")
                .thenReturn("anotherComponent");
        when(values.getString(BareMetalRemovalUpdater.NAME))
                .thenReturn("someName")
                .thenReturn("anotherName");

        bareMetalRemovalUpdater.updateConfigurationValuesThatReferenceBareMetal(dbConnection);

        verify(preparedStatement, times(2)).executeUpdate();
    }
}