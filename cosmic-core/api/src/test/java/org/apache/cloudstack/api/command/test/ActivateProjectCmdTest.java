package org.apache.cloudstack.api.command.test;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.projects.Project;
import com.cloud.projects.ProjectService;
import com.cloud.user.Account;
import org.apache.cloudstack.api.command.user.project.ActivateProjectCmd;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class ActivateProjectCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ActivateProjectCmd activateProjectCmd;

    @Override
    @Before
    public void setUp() {

        activateProjectCmd = new ActivateProjectCmd() {

            @Override
            public Long getId() {
                return 2L;
            }
        };
    }

    @Test
    public void testGetEntityOwnerIdForNullProject() {
        final ProjectService projectService = Mockito.mock(ProjectService.class);
        Mockito.when(projectService.getProject(Matchers.anyLong())).thenReturn(null);
        activateProjectCmd._projectService = projectService;

        try {
            activateProjectCmd.getEntityOwnerId();
        } catch (final InvalidParameterValueException exception) {
            Assert.assertEquals("Unable to find project by id 2", exception.getLocalizedMessage());
        }
    }

    @Test
    public void testGetEntityOwnerIdForProject() {
        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.getId()).thenReturn(2L);
        final ProjectService projectService = Mockito.mock(ProjectService.class);
        final Account account = Mockito.mock(Account.class);
        Mockito.when(account.getId()).thenReturn(2L);
        Mockito.when(projectService.getProject(Matchers.anyLong())).thenReturn(project);

        Mockito.when(projectService.getProjectOwner(Matchers.anyLong())).thenReturn(account);
        activateProjectCmd._projectService = projectService;

        Assert.assertEquals(2L, activateProjectCmd.getEntityOwnerId());
    }
}
