package org.apache.cloudstack.api.command.test;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.projects.Project;
import com.cloud.projects.ProjectService;
import com.cloud.user.Account;
import org.apache.cloudstack.api.command.user.account.AddAccountToProjectCmd;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class AddAccountToProjectCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AddAccountToProjectCmd addAccountToProjectCmd;

    @Override
    @Before
    public void setUp() {
        addAccountToProjectCmd = new AddAccountToProjectCmd() {

            @Override
            public Long getProjectId() {
                return 2L;
            }

            @Override
            public String getAccountName() {

                // to run the test testGetEventDescriptionForAccount set the
                // accountName
                // return "accountName";
                // to run the test the testGetEventDescriptionForNullAccount
                // return accountname as null
                return null;
            }

            @Override
            public String getEmail() {
                // return "customer@abc.com";
                return null;
            }
        };
    }

    /****
     * Condition not handled in the code
     *****/

    /*
     * @Test public void testGetEntityOwnerIdForNullProject() {
     *
     * ProjectService projectService = Mockito.mock(ProjectService.class);
     * Mockito
     * .when(projectService.getProject(Mockito.anyLong())).thenReturn(null);
     * addAccountToProjectCmd._projectService = projectService;
     *
     * try { addAccountToProjectCmd.getEntityOwnerId(); }
     * catch(InvalidParameterValueException exception) {
     * Assert.assertEquals("Unable to find project by id 2",
     * exception.getLocalizedMessage()); }
     *
     * }
     */
    @Test
    public void testGetEntityOwnerIdForProject() {

        final Project project = Mockito.mock(Project.class);
        Mockito.when(project.getId()).thenReturn(2L);

        final ProjectService projectService = Mockito.mock(ProjectService.class);
        final Account account = Mockito.mock(Account.class);

        Mockito.when(account.getId()).thenReturn(2L);
        Mockito.when(projectService.getProject(Matchers.anyLong())).thenReturn(project);

        Mockito.when(projectService.getProjectOwner(Matchers.anyLong())).thenReturn(account);
        addAccountToProjectCmd._projectService = projectService;

        Assert.assertEquals(2L, addAccountToProjectCmd.getEntityOwnerId());
    }

    /**
     * To run the test uncomment the return statement for getAccountName() in
     * setup() and return null
     *
     * **/

    /*
     * @Test public void testGetEventDescriptionForNullAccount() {
     *
     * String result = addAccountToProjectCmd.getEventDescription(); String
     * expected = "Sending invitation to email null to join project: 2";
     * Assert.assertEquals(expected, result);
     *
     * }
     */

    /***
     *
     *
     *
     * ***/

    /*
     * @Test public void testGetEventDescriptionForAccount() {
     *
     * String result = addAccountToProjectCmd.getEventDescription(); String
     * expected = "Adding account accountName to project: 2";
     * Assert.assertEquals(expected, result);
     *
     * }
     */
    @Test
    public void testExecuteForNullAccountNameEmail() {

        try {
            addAccountToProjectCmd.execute();
        } catch (final InvalidParameterValueException exception) {
            Assert.assertEquals("Either accountName or email is required", exception.getLocalizedMessage());
        }
    }

    /*
     * @Test public void testExecuteForAccountNameEmail() {
     *
     * try {
     *
     * ComponentLocator c = Mockito.mock(ComponentLocator.class); UserContext
     * userContext = Mockito.mock(UserContext.class);
     *
     * // Mockito.when(userContext.current()).thenReturn(userContext);
     *
     *
     * addAccountToProjectCmd.execute(); } catch(InvalidParameterValueException
     * exception) {
     * Assert.assertEquals("Either accountName or email is required",
     * exception.getLocalizedMessage()); }
     *
     * }
     */
}
