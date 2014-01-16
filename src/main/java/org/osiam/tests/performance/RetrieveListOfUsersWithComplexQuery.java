package org.osiam.tests.performance;

import org.junit.Test;
import org.junit.runner.RunWith;
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
public class RetrieveListOfUsersWithComplexQuery extends AbstractIntegrationTestBase {

    @Test
    public void run(){
        String queryString = "filter=meta.created gt \"2011-10-10 00:00:00\""
                + " and (userName sw \"user\" or userName eq \"irrelevant\")"
                + " and (email sw \"email\" and email.type eq \"work\")";
        oConnector.searchUsers(queryString, accessToken);
    }
}
