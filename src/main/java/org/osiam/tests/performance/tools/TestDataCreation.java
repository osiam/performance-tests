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

package org.osiam.tests.performance.tools;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Entitlement;
import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.Im;
import org.osiam.resources.scim.MemberRef;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.PhoneNumber;
import org.osiam.resources.scim.Photo;
import org.osiam.resources.scim.Role;
import org.osiam.resources.scim.User;
import org.osiam.resources.scim.X509Certificate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * class is used to create a number of test users and group in a database. Afterwards this data can be extracted as
 * dbunit database seed
 * 
 */
public class TestDataCreation {

    private static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    private static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    private static final String CLIENT_ID = "example-client";
    private static final String CLIENT_SECRET = "secret";
    private static OsiamConnector oConnector;
    private static AccessToken accessToken;

    private static final int NUMBER_USER = 1000;
    private static final int NUMBER_GROUPS = 50;
    private static final String IRRELEVANT = "irrelevant";
    private static final String EXTENSION_SCHEMA = "urn:scim:extension:performance";
    private static final int MIN_COUNT_BYTE_BUFFER = 5000;
    private static ArrayList<User> users = new ArrayList<User>();

    public static void main(String[] args) throws Exception {
        setupDb();
        setupConnector();
        createTestUserAndGroups();
    }

    public static void setupDb() {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("context.xml")) {
            IDatabaseConnection connection = new DatabaseDataSourceConnection(
                    (DataSource) applicationContext.getBean("dataSource"));

            try {
                DatabaseOperation.CLEAN_INSERT.execute(connection,
                        new FlatXmlDataSetBuilder().build(
                                applicationContext.getResource("/database_seed_minimal.xml").getInputStream()));
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setupConnector() throws Exception {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        accessToken = oConnector.retrieveAccessToken();
    }

    public static void createTestUserAndGroups() {

        long start = System.nanoTime();

        for (int userIndex = 1; userIndex <= NUMBER_USER; userIndex++) {
            User user = getNewUser(userIndex);
            user = oConnector.createUser(user, accessToken);

            users.add(user);

            System.out.println("Created User " + userIndex + "/" + NUMBER_USER);
        }

        for (int groupIndex = 1; groupIndex <= NUMBER_GROUPS; groupIndex++) {
            Group.Builder groupBuilder = new Group.Builder("group" + groupIndex);
            groupBuilder.setExternalId("GrExternalId" + groupIndex);
            groupBuilder.setMembers(getMembers(groupIndex));

            oConnector.createGroup(groupBuilder.build(), accessToken);

            System.out.println("Created Group " + groupIndex + "/" + NUMBER_GROUPS);
        }

        long time = TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        System.out.println(time + " seconds needed to create the users and groups");
    }

    private static Set<MemberRef> getMembers(int countCurrentGroup) {

        int userPerGroup = NUMBER_USER / NUMBER_GROUPS;
        Set<MemberRef> members = new HashSet<MemberRef>();

        for (int count = 0; count < userPerGroup; count++) {
            int currentUserPosition = userPerGroup * countCurrentGroup + count;
            if (currentUserPosition < users.size()) {
                MemberRef member = new MemberRef.Builder()
                        .setValue(users.get(currentUserPosition).getId()).build();
                members.add(member);

            }
        }
        return members;
    }

    private static User getNewUser(int userCount) {
        User.Builder userBuilder = new User.Builder("user" + userCount);

        userBuilder.setActive(userCount % 2 == 0);
        userBuilder.setAddresses(getAddresses(userCount));
        userBuilder.setDisplayName("displayName" + userCount);
        userBuilder.setEmails(getEmailaddress(userCount));
        userBuilder.setEntitlements(getEntitlements(userCount));
        userBuilder.setExternalId("externalId" + userCount);
        userBuilder.setIms(getIms(userCount));
        userBuilder.setLocale("de");
        userBuilder.setName(getName(userCount));
        userBuilder.setNickName("nickName" + userCount);
        userBuilder.setPassword("password" + userCount);
        userBuilder.setPhoneNumbers(getPhoneNumbers(userCount));
        userBuilder.setPhotos(getPhotos(userCount));
        userBuilder.setPreferredLanguage("de");
        userBuilder.setProfileUrl("www.my-url" + userCount + ".com");
        userBuilder.setRoles(getRoles(userCount));
        userBuilder.setTimezone("de");
        userBuilder.setTitle("title" + userCount);
        userBuilder.setX509Certificates(getX509Certificates());
        userBuilder.addExtension(getExtension(userCount));

        return userBuilder.build();
    }

    private static Name getName(int countCurrentUser) {
        return new Name.Builder().setFamilyName("familyName" + countCurrentUser)
                .setFormatted("formatted" + countCurrentUser)
                .setGivenName("givenName" + countCurrentUser)
                .setHonorificPrefix("hPrefix" + countCurrentUser)
                .setHonorificSuffix("hSuffix" + countCurrentUser)
                .setMiddleName("middleName" + countCurrentUser)
                .build();
    }

    private static ArrayList<Address> getAddresses(int countCurrentUser) {

        ArrayList<Address> addresses = new ArrayList<Address>();
        addresses.add(getNewAddress(countCurrentUser, true, Address.Type.WORK));
        addresses.add(getNewAddress(countCurrentUser, false, Address.Type.HOME));
        addresses.add(getNewAddress(countCurrentUser, false, Address.Type.OTHER));

        return addresses;
    }

    private static Address getNewAddress(int countCurrentUser, boolean primary, Address.Type type) {
        return new Address.Builder().setCountry("de")
                .setFormatted("Address of user number " + countCurrentUser)
                .setLocality("de")
                .setPostalCode("123456-" + countCurrentUser)
                .setPrimary(primary)
                .setRegion("de")
                .setStreetAddress("Main street " + countCurrentUser)
                .setType(type)
                .build();
    }

    private static ArrayList<Email> getEmailaddress(int countCurrentUser) {

        ArrayList<Email> emails = new ArrayList<Email>();
        emails.add(getNewEmail(countCurrentUser, true, Email.Type.WORK));
        emails.add(getNewEmail(countCurrentUser, false, Email.Type.HOME));
        emails.add(getNewEmail(countCurrentUser, false, Email.Type.OTHER));

        return emails;
    }

    private static Email getNewEmail(int countCurrentUser, boolean primary, Email.Type type) {
        return new Email.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("email" + countCurrentUser + "@" + type.getValue() + ".com")
                .build();
    }

    private static ArrayList<Entitlement> getEntitlements(int countCurrentUser) {

        Entitlement entitlement = new Entitlement.Builder()
                .setPrimary(true)
                .setType(new Entitlement.Type(IRRELEVANT))
                .setValue("entitlement" + countCurrentUser)
                .build();

        ArrayList<Entitlement> entitlements = new ArrayList<Entitlement>();
        entitlements.add(entitlement);

        return entitlements;
    }

    private static ArrayList<Im> getIms(int countCurrentUser) {

        ArrayList<Im> ims = new ArrayList<Im>();
        ims.add(getNewIm(countCurrentUser, true, Im.Type.AIM));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.GTALK));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.ICQ));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.MSN));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.QQ));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.XMPP));

        return ims;
    }

    private static Im getNewIm(int countCurrentUser, boolean primary, Im.Type type) {
        return new Im.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("im-" + countCurrentUser)
                .build();
    }

    private static ArrayList<PhoneNumber> getPhoneNumbers(int countCurrentUser) {

        ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
        phoneNumbers.add(getNewPhoneNumber(countCurrentUser, true, PhoneNumber.Type.WORK));
        phoneNumbers.add(getNewPhoneNumber(countCurrentUser, false, PhoneNumber.Type.HOME));
        phoneNumbers.add(getNewPhoneNumber(countCurrentUser, false, PhoneNumber.Type.OTHER));

        return phoneNumbers;
    }

    private static PhoneNumber getNewPhoneNumber(int countCurrentUser, boolean primary, PhoneNumber.Type type) {
        return new PhoneNumber.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("049123" + countCurrentUser)
                .build();
    }

    private static ArrayList<Photo> getPhotos(int countCurrentUser) {

        ArrayList<Photo> photos = new ArrayList<Photo>();
        photos.add(getNewPhoto(countCurrentUser, true, Photo.Type.PHOTO));
        photos.add(getNewPhoto(countCurrentUser, false, Photo.Type.THUMBNAIL));

        return photos;
    }

    private static Photo getNewPhoto(int countCurrentUser, boolean primary, Photo.Type type) {
        Photo photo = null;
        try {
            photo = new Photo.Builder()
                    .setPrimary(primary)
                    .setType(type)
                    .setValue(new URI("photo-url-" + countCurrentUser + ".jpg"))
                    .build();
        } catch (URISyntaxException e) {
        }
        return photo;
    }

    private static ArrayList<Role> getRoles(int countCurrentUser) {

        Role role = new Role.Builder()
                .setPrimary(true)
                .setValue("role" + countCurrentUser)
                .build();

        ArrayList<Role> roles = new ArrayList<Role>();
        roles.add(role);

        return roles;
    }

    private static ArrayList<X509Certificate> getX509Certificates() {

        X509Certificate x509Certificate = new X509Certificate.Builder()
                .setPrimary(true)
                .setValue("MIIBrTCCARagAwIBAgIFHL6O8kAwDQYJKoZIhvcNAQEFBQAwGDEWMBQGA1UEAxMNRXhhbXBsZUlz"
                        + "c3VlcjAiGA8yMDAwMDEwMTAwMDAwMFoYDzIwNTAwMTAxMDAwMDAwWjAZMRcwFQYDVQQDEw5FeGFt"
                        + "cGxlU3ViamVjdDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEApbwnNhV4Mgn1VrQctOaSkoTg"
                        + "S6VqDulOAgGvDxwGMaAILWn0lNU50P7mkyFemGLCAWT+JGxgk7XLmaJtTGdA+9z7GnxRtxRpsnEL"
                        + "yAleVvAVNvZxecMIYyuUM2ZRBZWAnyDig4HomVAXz3rA0B32L37qmf+e3Z/Yie2ft4l41FcCAwEA"
                        + "ATANBgkqhkiG9w0BAQUFAAOBgQBE9aUWNuXhRGK225tGPzMKOa3WkltBiZOv8RNCavvKkp6/WIle"
                        + "KWtoNwSwUGjq+VhVcGqBfPEqDN8eC5DeCIxmQqkvxVQLe8hAZ4o5upSvfxvttj1NbSJMBf6NtDrB"
                        + "aVjjgqxSubteb6th+cqTsPdUsn5WfDbDjeuSa5d0fOEBzw==")
                .build();

        ArrayList<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
        x509Certificates.add(x509Certificate);

        return x509Certificates;
    }

    private static Extension getExtension(int countCurrentUser) {

        Extension extension = new Extension.Builder(EXTENSION_SCHEMA)
        .setField("stringValue", "Hello " + countCurrentUser)
        .setField("integerValue", Integer.toString(countCurrentUser))
        .setField("booleanValue", countCurrentUser % 2 == 0)
        .setField("decimalValue", new BigDecimal(countCurrentUser))
        .setField("dateValue", new Date())
        .setField("binaryValue", getBigByteBuffer(countCurrentUser))
        .setField("referenceValue", "https://example.com/Users/" + (countCurrentUser - 1))
        .build();
        return extension;
    }

    private static ByteBuffer getBigByteBuffer(Integer countCurrentUser) {
        String userId = countCurrentUser.toString();
        byte[] bytes = new byte[userId.length() + MIN_COUNT_BYTE_BUFFER];
        int actPosition;

        // first comes the id
        char[] userChars = userId.toCharArray();
        for (int count = 0; count < userChars.length; count++) {
            bytes[count] = (byte) userChars[count];
        }
        actPosition = userChars.length;

        // now we add random bytes
        Random random = new Random();
        String allowedChars = "0123456789abcdefghijklmnopqrstuvwxyz";
        int max = allowedChars.length();
        for (int i = 0; i < MIN_COUNT_BYTE_BUFFER; i++) {
            int value = random.nextInt(max);
            bytes[actPosition++] = (byte) allowedChars.charAt(value);
        }

        ByteBuffer ret = ByteBuffer.wrap(new byte[bytes.length]);
        ret.put(bytes);
        ret.flip();

        return ret;
    }
}
