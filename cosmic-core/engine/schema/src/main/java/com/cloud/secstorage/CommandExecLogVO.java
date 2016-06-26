package com.cloud.secstorage;

import com.cloud.utils.DateUtil;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "cmd_exec_log")
public class CommandExecLogVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "host_id")
    private long hostId;

    @Column(name = "instance_id")
    private long instanceId;

    @Column(name = "command_name")
    private String commandName;

    @Column(name = "weight")
    private int weight;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    public CommandExecLogVO() {
    }

    public CommandExecLogVO(final long hostId, final long instanceId, final String commandName, final int weight) {
        this.hostId = hostId;
        this.instanceId = instanceId;
        this.commandName = commandName;
        this.weight = weight;
        this.created = DateUtil.currentGMTTime();
    }

    @Override
    public long getId() {
        return this.id;
    }

    public long getHostId() {
        return this.hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public long getInstanceId() {
        return this.instanceId;
    }

    public void setInstanceId(final long instanceId) {
        this.instanceId = instanceId;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public void setCommandName(final String commandName) {
        this.commandName = commandName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }
}
