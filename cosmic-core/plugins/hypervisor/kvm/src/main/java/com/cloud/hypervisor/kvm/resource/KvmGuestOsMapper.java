package com.cloud.hypervisor.kvm.resource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvmGuestOsMapper {
    private static final Logger logger = LoggerFactory.getLogger(KvmGuestOsMapper.class);
    private static final Map<String, String> mapper = new HashMap<>();

    static {
        mapper.put("CentOS 4.5 (32-bit)", "CentOS 4.5");
        mapper.put("CentOS 4.6 (32-bit)", "CentOS 4.6");
        mapper.put("CentOS 4.7 (32-bit)", "CentOS 4.7");
        mapper.put("CentOS 4.8 (32-bit)", "CentOS 4.8");
        mapper.put("CentOS 5.0 (32-bit)", "CentOS 5.0");
        mapper.put("CentOS 5.0 (64-bit)", "CentOS 5.0");
        mapper.put("CentOS 5.1 (32-bit)", "CentOS 5.1");
        mapper.put("CentOS 5.1 (64-bit)", "CentOS 5.1");
        mapper.put("CentOS 5.2 (32-bit)", "CentOS 5.2");
        mapper.put("CentOS 5.2 (64-bit)", "CentOS 5.2");
        mapper.put("CentOS 5.3 (32-bit)", "CentOS 5.3");
        mapper.put("CentOS 5.3 (64-bit)", "CentOS 5.3");
        mapper.put("CentOS 5.4 (32-bit)", "CentOS 5.4");
        mapper.put("CentOS 5.4 (64-bit)", "CentOS 5.4");
        mapper.put("CentOS 5.5 (32-bit)", "CentOS 5.5");
        mapper.put("CentOS 5.5 (64-bit)", "CentOS 5.5");
        mapper.put("Red Hat Enterprise Linux 2", "Red Hat Enterprise Linux 2");
        mapper.put("Red Hat Enterprise Linux 3 (32-bit)", "Red Hat Enterprise Linux 3");
        mapper.put("Red Hat Enterprise Linux 3 (64-bit)", "Red Hat Enterprise Linux 3");
        mapper.put("Red Hat Enterprise Linux 4(64-bit)", "Red Hat Enterprise Linux 4");
        mapper.put("Red Hat Enterprise Linux 4.5 (32-bit)", "Red Hat Enterprise Linux 4.5");
        mapper.put("Red Hat Enterprise Linux 4.6 (32-bit)", "Red Hat Enterprise Linux 4.6");
        mapper.put("Red Hat Enterprise Linux 4.7 (32-bit)", "Red Hat Enterprise Linux 4.7");
        mapper.put("Red Hat Enterprise Linux 4.8 (32-bit)", "Red Hat Enterprise Linux 4.8");
        mapper.put("Red Hat Enterprise Linux 5.0 (32-bit)", "Red Hat Enterprise Linux 5.0");
        mapper.put("Red Hat Enterprise Linux 5.0 (64-bit)", "Red Hat Enterprise Linux 5.0");
        mapper.put("Red Hat Enterprise Linux 5.1 (32-bit)", "Red Hat Enterprise Linux 5.1");
        mapper.put("Red Hat Enterprise Linux 5.1 (32-bit)", "Red Hat Enterprise Linux 5.1");
        mapper.put("Red Hat Enterprise Linux 5.2 (32-bit)", "Red Hat Enterprise Linux 5.2");
        mapper.put("Red Hat Enterprise Linux 5.2 (64-bit)", "Red Hat Enterprise Linux 5.2");
        mapper.put("Red Hat Enterprise Linux 5.3 (32-bit)", "Red Hat Enterprise Linux 5.3");
        mapper.put("Red Hat Enterprise Linux 5.3 (64-bit)", "Red Hat Enterprise Linux 5.3");
        mapper.put("Red Hat Enterprise Linux 5.4 (32-bit)", "Red Hat Enterprise Linux 5.4");
        mapper.put("Red Hat Enterprise Linux 5.4 (64-bit)", "Red Hat Enterprise Linux 5.4");
        mapper.put("Red Hat Enterprise Linux 5.5 (32-bit)", "Red Hat Enterprise Linux 5.5");
        mapper.put("Red Hat Enterprise Linux 5.5 (64-bit)", "Red Hat Enterprise Linux 5.5");
        mapper.put("Red Hat Enterprise Linux 6.0 (32-bit)", "Red Hat Enterprise Linux 6.0");
        mapper.put("Red Hat Enterprise Linux 6.0 (64-bit)", "Red Hat Enterprise Linux 6.0");
        mapper.put("Fedora 13", "Fedora 13");
        mapper.put("Fedora 12", "Fedora 12");
        mapper.put("Fedora 11", "Fedora 11");
        mapper.put("Fedora 10", "Fedora 10");
        mapper.put("Fedora 9", "Fedora 9");
        mapper.put("Fedora 8", "Fedora 8");
        mapper.put("Ubuntu 12.04 (32-bit)", "Ubuntu 12.04");
        mapper.put("Ubuntu 12.04 (64-bit)", "Ubuntu 12.04");
        mapper.put("Ubuntu 10.04 (32-bit)", "Ubuntu 10.04");
        mapper.put("Ubuntu 10.04 (64-bit)", "Ubuntu 10.04");
        mapper.put("Ubuntu 10.10 (32-bit)", "Ubuntu 10.10");
        mapper.put("Ubuntu 10.10 (64-bit)", "Ubuntu 10.10");
        mapper.put("Ubuntu 9.10 (32-bit)", "Ubuntu 9.10");
        mapper.put("Ubuntu 9.10 (64-bit)", "Ubuntu 9.10");
        mapper.put("Ubuntu 9.04 (32-bit)", "Ubuntu 9.04");
        mapper.put("Ubuntu 9.04 (64-bit)", "Ubuntu 9.04");
        mapper.put("Ubuntu 8.10 (32-bit)", "Ubuntu 8.10");
        mapper.put("Ubuntu 8.10 (64-bit)", "Ubuntu 8.10");
        mapper.put("Ubuntu 8.04 (32-bit)", "Other Linux");
        mapper.put("Ubuntu 8.04 (64-bit)", "Other Linux");
        mapper.put("Debian GNU/Linux 5(32-bit)", "Debian GNU/Linux 5");
        mapper.put("Debian GNU/Linux 5(64-bit)", "Debian GNU/Linux 5");
        mapper.put("Debian GNU/Linux 5.0 (32-bit)", "Debian GNU/Linux 5");
        mapper.put("Debian GNU/Linux 4(32-bit)", "Debian GNU/Linux 4");
        mapper.put("Debian GNU/Linux 4(64-bit)", "Debian GNU/Linux 4");
        mapper.put("Debian GNU/Linux 6(64-bit)", "Debian GNU/Linux 6");
        mapper.put("Debian GNU/Linux 6(32-bit)", "Debian GNU/Linux 6");
        mapper.put("Other 2.6x Linux (32-bit)", "Other 2.6x Linux");
        mapper.put("Other 2.6x Linux (64-bit)", "Other 2.6x Linux");
        mapper.put("Other Linux (32-bit)", "Other Linux");
        mapper.put("Other Linux (64-bit)", "Other Linux");
        mapper.put("Other Ubuntu (32-bit)", "Other Linux");
        mapper.put("Other Ubuntu (64-bit)", "Other Linux");
        mapper.put("Asianux 3(32-bit)", "Other Linux");
        mapper.put("Asianux 3(64-bit)", "Other Linux");
        mapper.put("Windows 7 (32-bit)", "Windows 7");
        mapper.put("Windows 7 (64-bit)", "Windows 7");
        mapper.put("Windows Server 2003 Enterprise Edition(32-bit)", "Windows Server 2003");
        mapper.put("Windows Server 2003 Enterprise Edition(64-bit)", "Windows Server 2003");
        mapper.put("Windows Server 2003 DataCenter Edition(32-bit)", "Windows Server 2003");
        mapper.put("Windows Server 2003 DataCenter Edition(64-bit)", "Windows Server 2003");
        mapper.put("Windows Server 2003 Standard Edition(32-bit)", "Windows Server 2003");
        mapper.put("Windows Server 2003 Standard Edition(64-bit)", "Windows Server 2003");
        mapper.put("Windows Server 2003 Web Edition", "Windows Server 2003");
        mapper.put("Microsoft Small Bussiness Server 2003", "Windows Server 2003");
        mapper.put("Windows Server 2008 (32-bit)", "Windows Server 2008");
        mapper.put("Windows Server 2008 (64-bit)", "Windows Server 2008");
        mapper.put("Windows Server 2008 R2 (64-bit)", "Windows Server 2008");
        mapper.put("Windows 2000 Server SP4 (32-bit)", "Windows 2000");
        mapper.put("Windows 2000 Server", "Windows 2000");
        mapper.put("Windows 2000 Advanced Server", "Windows 2000");
        mapper.put("Windows 2000 Professional", "Windows 2000");
        mapper.put("Windows Vista (32-bit)", "Windows Vista");
        mapper.put("Windows Vista (64-bit)", "Windows Vista");
        mapper.put("Windows XP SP2 (32-bit)", "Windows XP");
        mapper.put("Windows XP SP3 (32-bit)", "Windows XP");
        mapper.put("Windows XP (32-bit)", "Windows XP");
        mapper.put("Windows XP (64-bit)", "Windows XP");
        mapper.put("Windows 98", "Windows 98");
        mapper.put("Windows 95", "Windows 95");
        mapper.put("Windows NT 4", "Windows NT");
        mapper.put("Windows 3.1", "Windows 3.1");
        mapper.put("Windows PV", "Other PV");
        mapper.put("FreeBSD 10 (32-bit)", "FreeBSD 10");
        mapper.put("FreeBSD 10 (64-bits", "FreeBSD 10");
        mapper.put("Other PV (32-bit)", "Other PV");
        mapper.put("Other PV (64-bit)", "Other PV");
    }

    public static String getGuestOsName(final String guestOsName) {
        final String guestOs = mapper.get(guestOsName);
        if (guestOs == null) {
            logger.debug("Can't find the mapping of guest os: " + guestOsName);
            return "Other";
        } else {
            return guestOs;
        }
    }
}
