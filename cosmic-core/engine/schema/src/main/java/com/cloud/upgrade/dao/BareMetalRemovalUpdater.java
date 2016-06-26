package com.cloud.upgrade.dao;

import com.cloud.utils.StringUtils;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class BareMetalRemovalUpdater {

    public static final String VALUE = "value";
    public static final String CATEGORY = "category";
    public static final String INSTANCE = "instance";
    public static final String COMPONENT = "component";
    public static final String NAME = "name";
    public static final String SELECT_CONFIGURATION_ITEMS = "SELECT category, instance, component, name, value FROM configuration WHERE name = ?";
    public static final String UPDATE_CONFIGURATION_ITEM = "UPDATE configuration SET value = ? WHERE category = ? AND instance = ? AND component = ? AND name = ?";
    private static final Logger logger = Logger.getLogger(BareMetalRemovalUpdater.class);
    private static final String BARE_METAL = "baremetal";
    private static final String VALUE_SEPARATOR = ",";
    private static final String HYPERVISOR_LIST = "hypervisor.list";

    public void updateConfigurationValuesThatReferenceBareMetal(final Connection conn) {
        logger.info("Updating configuration items that reference BareMetal and removing that reference");
        final List<Map<String, String>> updatedRows = createUpdatedConfigurationItemValues(conn);
        if (!updatedRows.isEmpty()) {
            updateConfigurationItems(conn, updatedRows);
        }
    }

    public List<Map<String, String>> createUpdatedConfigurationItemValues(final Connection conn) {
        final List<Map<String, String>> updatedRows = new LinkedList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SELECT_CONFIGURATION_ITEMS)) {
            preparedStatement.setString(1, HYPERVISOR_LIST);
            final ResultSet values = preparedStatement.executeQuery();
            findAndUpdateRowsThatReferenceBareMetal(values, updatedRows);
            values.close();
        } catch (final SQLException e) {
            logger.error("Caught exception while reading configuration values: " + e.getMessage());
            throw new CloudRuntimeException(e);
        }
        return updatedRows;
    }

    public void updateConfigurationItems(final Connection conn, final List<Map<String, String>> updatedRows) {
        for (final Map<String, String> row : updatedRows) {
            final String category = row.get(CATEGORY);
            final String instance = row.get(INSTANCE);
            final String component = row.get(COMPONENT);
            final String name = row.get(NAME);
            final String value = row.get(VALUE);
            logger.info("Updating configuration item: " + printConfigurationItem(category, instance, component, name, value));
            updateConfigurationItem(conn, category, instance, component, name, value);
        }
    }

    public void findAndUpdateRowsThatReferenceBareMetal(final ResultSet values, final List<Map<String, String>> updatedRows) throws SQLException {
        while (values.next()) {
            final String value = values.getString(VALUE);
            if (value != null && hasReferenceToBareMetal(value)) {
                final String category = values.getString(CATEGORY);
                final String instance = values.getString(INSTANCE);
                final String component = values.getString(COMPONENT);
                final String name = values.getString(NAME);
                logger.info("Found one configuration item that references BareMetal: " + printConfigurationItem(category, instance, component, name, value));
                final HashMap<String, String> updatedRow = createUpdatedRow(category, instance, component, name, createUpdatedValue(value));
                updatedRows.add(updatedRow);
            }
        }
    }

    private String printConfigurationItem(final String category, final String instance, final String component, final String name, final String value) {
        return "category = " + category + ", instance = " + instance + ", component = " + component + ", name = " + name + ", value = " + value;
    }

    public void updateConfigurationItem(final Connection conn, final String category, final String instance, final String component, final String name, final String value) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_CONFIGURATION_ITEM)) {
            preparedStatement.setString(1, value);
            preparedStatement.setString(2, category);
            preparedStatement.setString(3, instance);
            preparedStatement.setString(4, component);
            preparedStatement.setString(5, name);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            logger.error("Caught exception while updating values: " + e.getMessage());
            throw new CloudRuntimeException(e);
        }
    }

    public boolean hasReferenceToBareMetal(final String value) {
        return value.toLowerCase().contains(BARE_METAL);
    }

    private HashMap<String, String> createUpdatedRow(final String category, final String instance, final String component, final String name, final String filteredValue) {
        final HashMap<String, String> newRow = new HashMap<>();
        newRow.put(CATEGORY, category);
        newRow.put(INSTANCE, instance);
        newRow.put(COMPONENT, component);
        newRow.put(NAME, name);
        newRow.put(VALUE, filteredValue);
        return newRow;
    }

    public String createUpdatedValue(final String value) {
        final String[] splitParts = value.split(VALUE_SEPARATOR);
        final List<String> filteredParts = new ArrayList<>(splitParts.length);
        for (final String part : splitParts) {
            if (!part.toLowerCase().equals(BARE_METAL)) {
                filteredParts.add(part);
            }
        }
        final String updatedValue = StringUtils.join(filteredParts, VALUE_SEPARATOR);
        logger.info("Updated value (without reference to BareMetal): " + updatedValue);
        return updatedValue;
    }
}
