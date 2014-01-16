package org.osiam.tests.performance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.MemberRef;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seed.xml")
public class RetrieveAllUsersOfOneGroup extends AbstractIntegrationTestBase {

    @Test
    public void run(){
        Group group = oConnector.getGroup("f4859783-d938-4179-9b94-502f94011211", accessToken);
        for (MemberRef currMember : group.getMembers()) {
            oConnector.getUser(currMember.getValue(), accessToken);
        }
    }
}
