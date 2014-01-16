package org.osiam.tests.performance;

import org.junit.Test;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.MemberRef;

public class RetrieveAllUsersOfOneGroup extends AbstractPerformanceTest {

    @Test
    public void run(){
        Group group = osiamConnector.getGroup(PerformanceTestContext.VALID_GROUP_ID, accessToken);
        for (MemberRef currMember : group.getMembers()) {
            osiamConnector.getUser(currMember.getValue(), accessToken);
        }
    }
}
