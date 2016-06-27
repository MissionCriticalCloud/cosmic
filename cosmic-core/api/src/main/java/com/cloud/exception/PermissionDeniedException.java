package com.cloud.exception;

import com.cloud.user.Account;
import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.acl.ControlledEntity;

import java.util.List;

public class PermissionDeniedException extends CloudRuntimeException {

    private static final long serialVersionUID = SerialVersionUID.PermissionDeniedException;
    List<? extends ControlledEntity> violations;
    Account account;

    public PermissionDeniedException(final String message) {
        super(message);
    }

    public PermissionDeniedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected PermissionDeniedException() {
        super();
    }

    public PermissionDeniedException(final String message, final Account account, final List<? extends ControlledEntity> violations) {
        super(message);
        this.violations = violations;
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public List<? extends ControlledEntity> getEntitiesInViolation() {
        return violations;
    }

    public void addDetails(final Account account, final List<? extends ControlledEntity> violations) {
        this.account = account;
        this.violations = violations;
    }

    public void addViolations(final List<? extends ControlledEntity> violations) {
        this.violations = violations;
    }
}
