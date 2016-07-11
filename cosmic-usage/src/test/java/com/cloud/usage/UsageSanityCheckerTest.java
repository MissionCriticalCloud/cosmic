package com.cloud.usage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;

public class UsageSanityCheckerTest extends TestCase {

    @Test
    public void testCheckItemCountByPstmt() throws SQLException {
        // Prepare
        // Mock dependencies to exclude from the test
        final String sqlTemplate1 = "SELECT * FROM mytable1";
        final String sqlTemplate2 = "SELECT * FROM mytable2";

        final Connection conn = Mockito.mock(Connection.class);
        final PreparedStatement pstmt = Mockito.mock(PreparedStatement.class);
        final ResultSet rs = Mockito.mock(ResultSet.class);

        Mockito.when(conn.prepareStatement(sqlTemplate1)).thenReturn(pstmt);
        Mockito.when(conn.prepareStatement(sqlTemplate2)).thenReturn(pstmt);
        Mockito.when(pstmt.executeQuery()).thenReturn(rs, rs);

        // First if: true -> 8
        // Second loop: true -> 16
        Mockito.when(rs.next()).thenReturn(true, true);
        Mockito.when(rs.getInt(1)).thenReturn(8, 8, 16, 16);

        // Prepare class under test
        final UsageSanityChecker checker = new UsageSanityChecker();
        checker.conn = conn;
        checker.reset();
        checker.addCheckCase(sqlTemplate1, "item1");
        checker.addCheckCase(sqlTemplate2, "item2");

        // Execute
        checker.checkItemCountByPstmt();

        // Verify
        final Pattern pattern = Pattern.compile(".*8.*item1.*\n.*16.*item2.*");
        final Matcher matcher = pattern.matcher(checker.errors);
        assertTrue("Didn't create complete errors. It should create 2 errors: 8 item1 and 16 item2", matcher.find());
    }
}
