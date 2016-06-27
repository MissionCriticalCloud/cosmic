//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.LogLevel;
import com.cloud.agent.api.LogLevel.Log4jLevel;
import com.cloud.network.VpnUser;

import java.util.List;

public class VpnUsersCfgCommand extends NetworkElementCommand {
    UsernamePassword[] userpwds;

    protected VpnUsersCfgCommand() {

    }

    public VpnUsersCfgCommand(final List<VpnUser> addUsers, final List<VpnUser> removeUsers) {
        userpwds = new UsernamePassword[addUsers.size() + removeUsers.size()];
        int i = 0;
        for (final VpnUser vpnUser : removeUsers) {
            userpwds[i++] = new UsernamePassword(vpnUser.getUsername(), vpnUser.getPassword(), false);
        }
        for (final VpnUser vpnUser : addUsers) {
            userpwds[i++] = new UsernamePassword(vpnUser.getUsername(), vpnUser.getPassword(), true);
        }
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public UsernamePassword[] getUserpwds() {
        return userpwds;
    }

    public static class UsernamePassword {
        boolean add = true;
        private String username;
        @LogLevel(Log4jLevel.Off)
        private String password;

        public UsernamePassword(final String username, final String password) {
            super();
            this.username = username;
            this.password = password;
        }

        public UsernamePassword(final String username, final String password, final boolean add) {
            super();
            this.username = username;
            this.password = password;
            this.add = add;
        }

        protected UsernamePassword() {
            //for Gson
        }

        public boolean isAdd() {
            return add;
        }

        public void setAdd(final boolean add) {
            this.add = add;
        }

        public String getUsernamePassword() {
            return getUsername() + "," + getPassword();
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }
    }
}
