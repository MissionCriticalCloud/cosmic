//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class VpnUser {
    private String user;
    private String password;
    private boolean add;

    public VpnUser() {
        // Empty constructor for serialization
    }

    public VpnUser(final String user, final String password, final boolean add) {
        super();
        this.user = user;
        this.password = password;
        this.add = add;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(final boolean add) {
        this.add = add;
    }
}
