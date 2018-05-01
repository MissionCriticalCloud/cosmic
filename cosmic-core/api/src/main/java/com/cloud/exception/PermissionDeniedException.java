package com.cloud.exception;

import com.cloud.acl.ControlledEntity;
import com.cloud.user.Account;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.List;

public class PermissionDeniedException extends CloudRuntimeException {
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
