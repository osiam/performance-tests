package org.osiam.tests.performance;

import org.junit.Test;

public class RetrieveSingleUser extends AbstractPerformanceTest {

    @Test
    public void run(){
        osiamConnector.getUser(PerformanceTestContext.VALID_USER_ID, accessToken);
    }
}
