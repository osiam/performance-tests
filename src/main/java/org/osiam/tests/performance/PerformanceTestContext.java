/*
 * Copyright (C) 2015 tarent solutions GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.osiam.tests.performance;

import org.osiam.client.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.osiam.tests.performance.tools.TestDataCreation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTestContext {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestContext.class);

    public static final String VALID_USER_ID = "cef9452e-00a9-4cec-a086-d171374ffbef";
    public static final String VALID_GROUP_ID = "098b0e9c-d51b-4103-8222-b5c3f74249ff";

    public static final String OSIAM_HOST = "http://localhost:8280/osiam";
    public static final String CLIENT_ID = "example-client";
    public static final String CLIENT_SECRET = "secret";

    public static final OsiamConnector OSIAM_CONNECTOR;
    public static final AccessToken ACCESS_TOKEN;

    static {
        logger.info("Setting up database");
        TestDataCreation.setupDatabase();

        logger.info("Setting up Connector");
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder()
                .withEndpoint(OSIAM_HOST)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET);
        OSIAM_CONNECTOR = oConBuilder.build();

        logger.info("Retrieving access token");
        ACCESS_TOKEN = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        logger.info("Setting up test data");
        TestDataCreation.createTestUserAndGroups();
    }
}
