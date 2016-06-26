//

//

package com.cloud.utils.exception;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ExceptionUtilTest {

    @Test
    public void test() throws Exception {
        final FileNotFoundException fnfe = new FileNotFoundException();
        try {
            ExceptionUtil.rethrow(fnfe, IOException.class);
            fail();
        } catch (final IOException e) {
            assertTrue("we won !?!", true);
        }

        ExceptionUtil.rethrow(fnfe, ClassNotFoundException.class);

        try {
            ExceptionUtil.rethrow(fnfe, FileNotFoundException.class);
            fail();
        } catch (final FileNotFoundException e) {
            assertTrue("we won !?!", true);
        }
    }
}
