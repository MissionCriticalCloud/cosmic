//

//
package com.cloud.utils.storage;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

public class QCOW2UtilsTest {

    final Long virtualSize = 21474836480L;
    InputStream inputStream;

    /**
     * The QCOW2 Header
     * <p>
     * uint32_t magic;
     * uint32_t version;
     * <p>
     * uint64_t backing_file_offset;
     * uint32_t backing_file_size;
     * <p>
     * uint32_t cluster_bits;
     * uint64_t size;
     * <p>
     * uint32_t crypt_method;
     * <p>
     * uint32_t l1_size;
     * int64_t l1_table_offset;
     * <p>
     * uint64_t refcount_table_offset;
     * uint32_t refcount_table_clusters;
     * <p>
     * uint32_t nb_snapshots;
     * uint64_t snapshots_offset;
     *
     * @see https://people.gnome.org/~markmc/qcow-image-format.html
     */

    @Before
    public void setup() {

        final ByteBuffer byteBuffer = ByteBuffer.allocate(72);

        // Magic
        byteBuffer.put("QFI".getBytes(Charset.forName("UTF-8")));
        byteBuffer.put((byte) 0xfb);

        // Version
        byteBuffer.putInt(2);

        // Backing file offset
        byteBuffer.putLong(0L);

        // Backing file size
        byteBuffer.putInt(0);

        // Cluster bits
        byteBuffer.putInt(0);

        // Size
        byteBuffer.putLong(virtualSize);

        // Crypt method
        byteBuffer.putInt(0);

        // L1 Size
        byteBuffer.putInt(0);

        // L1 Table offset
        byteBuffer.putLong(0L);

        // Refcount table offset
        byteBuffer.putLong(0L);

        // Refcount table cluster
        byteBuffer.putInt(0);

        // NB Snapshots
        byteBuffer.putInt(0);

        // Snapshots offset
        byteBuffer.putLong(0L);

        inputStream = new ByteArrayInputStream(byteBuffer.array());
    }

    @Test
    public void getVirtualSizeHeaderLocation() {
        assertEquals(24, QCOW2Utils.getVirtualSizeHeaderLocation());
    }

    @Test
    public void getVirtualSizeTest() throws IOException {
        assertEquals(virtualSize.longValue(), QCOW2Utils.getVirtualSize(inputStream));
    }
}
