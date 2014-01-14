/*
 * Copyright (C) 2013 tarent AG
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestCase1 {

    @Before
    public void setupDb() {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("context.xml")) {
            IDatabaseConnection connection = new DatabaseDataSourceConnection(
                    (DataSource) applicationContext.getBean("dataSource"));

            try {
                DatabaseOperation.CLEAN_INSERT.execute(connection,
                        new FlatXmlDataSetBuilder().build(
                                applicationContext.getResource("/database_seed.xml").getInputStream()));
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void teardownDb() throws SQLException, MalformedURLException, IOException, DatabaseUnitException {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("context.xml")) {
            IDatabaseConnection connection = new DatabaseDataSourceConnection(
                    (DataSource) applicationContext.getBean("dataSource"));

            try {
                DatabaseOperation.DELETE_ALL.execute(connection,
                        new FlatXmlDataSetBuilder().build(
                                applicationContext.getResource("/database_tear_down.xml").getInputStream()));
            } finally {
                connection.close();
            }
        } catch (SQLException | DatabaseUnitException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        System.out.println("Starting Test");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Stopping Test");
    }

}
