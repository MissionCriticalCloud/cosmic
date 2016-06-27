//

//

package com.cloud.storage.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;

import org.junit.Test;

public class LocalTemplateDownloaderTest {

    @Test
    public void localTemplateDownloaderTest() throws Exception {
        final String url = new File("pom.xml").toURI().toURL().toString();
        final long defaultMaxTemplateSizeInBytes = TemplateDownloader.DEFAULT_MAX_TEMPLATE_SIZE_IN_BYTES;
        final String tempDir = System.getProperty("java.io.tmpdir");
        final TemplateDownloader td = new LocalTemplateDownloader(url, tempDir, defaultMaxTemplateSizeInBytes);

        final long bytes = td.download(true, null);

        assertThat(bytes, greaterThan(0L));
    }
}
