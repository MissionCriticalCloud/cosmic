package com.cloud.hypervisor.ovm3.objects;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudstackPlugin extends OvmObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudstackPlugin.class);
    private boolean checkstoragestarted = false;

    public CloudstackPlugin(final Connection connection) {
        setClient(connection);
    }

    public String getVncPort(final String vmName) throws Ovm3ResourceException {
        return (String) callWrapper("get_vncport", vmName);
    }

    public boolean ovsUploadSshKey(final String key, final String content) throws Ovm3ResourceException {
        return nullIsFalseCallWrapper("ovs_upload_ssh_key", key, content);
    }

    public boolean ovsUploadFile(final String path, final String file, final String content) throws Ovm3ResourceException {
        return nullIsFalseCallWrapper("ovs_upload_file", path, file, content);
    }

    public boolean ovsDomrUploadFile(final String domr, final String path, final String file,
                                     final String content) throws Ovm3ResourceException {
        return nullIsFalseCallWrapper("ovs_domr_upload_file", domr, path, file,
                content);
    }

    public ReturnCode domrExec(final String ip, final String cmd) throws Ovm3ResourceException {
        final ReturnCode rc = new ReturnCode();
        rc.setValues((Map<String, String>) callWrapper("exec_domr", ip, cmd));
        return rc;
    }

    public boolean dom0CheckPort(final String ip, final Integer port, Integer retries,
                                 final Integer interval) throws Ovm3ResourceException {
        Boolean checkResult = false;
    /* should deduct the interval from the timeout and sleep on it */
        final Integer sleep = interval;
        try {
            while (!checkResult && retries > 0) {
                checkResult = nullIsFalseCallWrapper("check_dom0_port", ip, port, interval);
                retries--;
                Thread.sleep(sleep * 1000);
            }
        } catch (final Exception e) {
            LOGGER.error("Dom0 port check failed: " + e);
        }
        return checkResult;
    }

    public Map<String, String> ovsDom0Stats(final String bridge) throws Ovm3ResourceException {
        return (Map<String, String>) callWrapper(
                "ovs_dom0_stats", bridge);
    }

    public Map<String, String> ovsDomUStats(final String domain) throws Ovm3ResourceException {
        return (Map<String, String>) callWrapper(
                "ovs_domU_stats", domain);
    }

    public boolean domrCheckPort(final String ip, final Integer port) throws Ovm3ResourceException {
        return (Boolean) callWrapper("check_domr_port", ip, port);
    }

    public boolean domrCheckSsh(final String ip) throws Ovm3ResourceException {
        return (Boolean) callWrapper("check_domr_ssh", ip);
    }

    public boolean ovsControlInterface(final String dev, final String cidr) throws Ovm3ResourceException {
        return (Boolean) callWrapper("ovs_control_interface", dev, cidr);
    }

    public boolean ping(final String host) throws Ovm3ResourceException {
        return (Boolean) callWrapper("ping", host);
    }

    public boolean ovsCheckFile(final String file) throws Ovm3ResourceException {
        return (Boolean) callWrapper("ovs_check_file", file);
    }

    public boolean dom0HasIp(final String ovm3PoolVip) throws Ovm3ResourceException {
        return (Boolean) callWrapper("check_dom0_ip", ovm3PoolVip);
    }

    public boolean dom0CheckStorageHealthCheck(final String path, final String script, final String guid, final Integer timeout,
                                               final Integer interval) throws Ovm3ResourceException {
        final Object[] x = (Object[]) callWrapper("check_dom0_storage_health_check", path, script, guid, timeout, interval);
        final Boolean running = (Boolean) x[0];
        checkstoragestarted = (Boolean) x[1];
        return running;
    }

    public boolean dom0CheckStorageHealthCheck() {
        return checkstoragestarted;
    }

    /* return something else in the future */
    public boolean dom0CheckStorageHealth(final String path, final String script, final String guid, final Integer timeout)
            throws Ovm3ResourceException {
        return (Boolean) callWrapper("check_dom0_storage_health", path, script, guid, timeout);
    }

    public boolean ovsMkdirs(final String dir) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("ovs_mkdirs", dir);
    }

    public boolean ovsMkdirs(final String dir, final Integer mode) throws Ovm3ResourceException {
        return nullIsTrueCallWrapper("ovs_mkdirs", dir, mode);
    }

    public static class ReturnCode {
        private final Map<String, Object> returnCode = new HashMap<String, Object>() {
            private static final long serialVersionUID = 5L;

            {
                put("rc", null);
                put("exit", null);
                put("err", null);
                put("out", null);
            }
        };

        public ReturnCode() {
        }

        public void setValues(final Map<String, String> mapValues) {
            returnCode.putAll(mapValues);
        }

        public Boolean getRc() throws Ovm3ResourceException {
            final Object rc = returnCode.get("rc");
            Long code = 1L;
            if (rc instanceof Integer) {
                code = new Long((Integer) rc);
            } else if (rc instanceof Long) {
                code = (Long) rc;
            } else {
                LOGGER.debug("Incorrect return code: " + rc);
                return false;
            }
            returnCode.put("exit", code);
            if (code != 0) {
                return false;
            }
            return true;
        }

        public String getStdOut() {
            return (String) returnCode.get("out");
        }

        public String getStdErr() {
            return (String) returnCode.get("err");
        }

        public Integer getExit() {
            if (returnCode.get("exit") == null) {
                returnCode.put("exit", returnCode.get("rc"));
            }
            return ((Long) returnCode.get("exit")).intValue();
        }
    }
}
