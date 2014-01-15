package org.osiam.performance.tools;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.Im;
import org.osiam.resources.scim.MemberRef;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.PhoneNumber;
import org.osiam.resources.scim.Photo;
import org.osiam.resources.scim.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * class is used to create a number of test users and group in a database. 
 * Afterwards this data can be extracted as dbunit database seed 
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
    private static final String EXTENSION_SCHEMA = "urn:scim:extension:perfomance";
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
                                applicationContext.getResource("/database_seed.xml").getInputStream()));
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void setupConnector() throws Exception {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
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

        Date start = new Date();
        for (int count = 1; count <= NUMBER_USER; count++) {
            User user = getNewUser(count);
             user = oConnector.createUser(user, accessToken);
            users.add(user);
        }

        for (int count = 1; count <= NUMBER_GROUPS; count++) {
            Group.Builder groupBuilder = new Group.Builder();
            groupBuilder.setDisplayName("group" + count);
            groupBuilder.setExternalId("GrExternalId" + count);
            groupBuilder.setMembers(getMembers(count));
            oConnector.createGroup(groupBuilder.build(), accessToken);
        }
        Date end = new Date();
        long time = (end.getTime() - start.getTime()) / 1000;
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
        addresses.add(getNewAddress(countCurrentUser, true, Address.Type.WORK.getValue()));
        addresses.add(getNewAddress(countCurrentUser, false, Address.Type.HOME.getValue()));
        addresses.add(getNewAddress(countCurrentUser, false, Address.Type.OTHER.getValue()));

        return addresses;
    }

    private static Address getNewAddress(int countCurrentUser, boolean primary, String type) {
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

    private static ArrayList<MultiValuedAttribute> getEmailaddress(int countCurrentUser) {

        ArrayList<MultiValuedAttribute> emails = new ArrayList<MultiValuedAttribute>();
        emails.add(getNewEmail(countCurrentUser, true, Email.Type.WORK.getValue()));
        emails.add(getNewEmail(countCurrentUser, false, Email.Type.HOME.getValue()));
        emails.add(getNewEmail(countCurrentUser, false, Email.Type.OTHER.getValue()));

        return emails;
    }

    private static MultiValuedAttribute getNewEmail(int countCurrentUser, boolean primary, String type) {
        return new MultiValuedAttribute.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("email" + countCurrentUser + "@" + type + ".com")
                .build();
    }

    private static ArrayList<MultiValuedAttribute> getEntitlements(int countCurrentUser) {

        MultiValuedAttribute entitlement = new MultiValuedAttribute.Builder()
                .setPrimary(true)
                .setType(IRRELEVANT)
                .setValue("entitlement" + countCurrentUser)
                .build();

        ArrayList<MultiValuedAttribute> entitlements = new ArrayList<MultiValuedAttribute>();
        entitlements.add(entitlement);

        return entitlements;
    }

    private static ArrayList<MultiValuedAttribute> getIms(int countCurrentUser) {

        ArrayList<MultiValuedAttribute> ims = new ArrayList<MultiValuedAttribute>();
        ims.add(getNewIm(countCurrentUser, true, Im.Type.AIM.getValue()));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.GTALK.getValue()));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.ICQ.getValue()));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.MSN.getValue()));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.QQ.getValue()));
        ims.add(getNewIm(countCurrentUser, false, Im.Type.XMPP.getValue()));

        return ims;
    }

    private static MultiValuedAttribute getNewIm(int countCurrentUser, boolean primary, String type) {
        return new MultiValuedAttribute.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("im-" + countCurrentUser)
                .build();
    }

    private static ArrayList<MultiValuedAttribute> getPhoneNumbers(int countCurrentUser) {

        ArrayList<MultiValuedAttribute> phoneNumbers = new ArrayList<MultiValuedAttribute>();
        phoneNumbers.add(getNewPhoneNumber(countCurrentUser, true, PhoneNumber.Type.WORK.getValue()));
        phoneNumbers.add(getNewPhoneNumber(countCurrentUser, false, PhoneNumber.Type.HOME.getValue()));
        phoneNumbers.add(getNewPhoneNumber(countCurrentUser, false, PhoneNumber.Type.OTHER.getValue()));

        return phoneNumbers;
    }

    private static MultiValuedAttribute getNewPhoneNumber(int countCurrentUser, boolean primary, String type) {
        return new MultiValuedAttribute.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("049123" + countCurrentUser)
                .build();
    }

    private static ArrayList<MultiValuedAttribute> getPhotos(int countCurrentUser) {

        ArrayList<MultiValuedAttribute> photos = new ArrayList<MultiValuedAttribute>();
        photos.add(getNewPhoto(countCurrentUser, true, Photo.Type.PHOTO.getValue()));
        photos.add(getNewPhoto(countCurrentUser, false, Photo.Type.THUMBNAIL.getValue()));

        return photos;
    }

    private static MultiValuedAttribute getNewPhoto(int countCurrentUser, boolean primary, String type) {
        return new MultiValuedAttribute.Builder()
                .setPrimary(primary)
                .setType(type)
                .setValue("photo-url-" + countCurrentUser + ".jpg")
                .build();
    }

    private static ArrayList<MultiValuedAttribute> getRoles(int countCurrentUser) {

        MultiValuedAttribute role = new MultiValuedAttribute.Builder()
                .setPrimary(true)
                .setValue("role" + countCurrentUser)
                .build();

        ArrayList<MultiValuedAttribute> roles = new ArrayList<MultiValuedAttribute>();
        roles.add(role);

        return roles;
    }

    private static ArrayList<MultiValuedAttribute> getX509Certificates() {

        MultiValuedAttribute role = new MultiValuedAttribute.Builder()
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

        ArrayList<MultiValuedAttribute> roles = new ArrayList<MultiValuedAttribute>();
        roles.add(role);

        return roles;
    }

    private static Extension getExtension(int countCurrentUser) {

        Extension extension = new Extension(EXTENSION_SCHEMA);
        extension.addOrUpdateField("stringValue", "Hello " + countCurrentUser);
        extension.addOrUpdateField("integerValue", Integer.toString(countCurrentUser));
        extension.addOrUpdateField("booleanValue", countCurrentUser % 2 == 0);
        extension.addOrUpdateField("decimalValue", new BigDecimal(countCurrentUser));
        extension.addOrUpdateField("dateValue", new Date());
        extension.addOrUpdateField("binaryValue", getBigByteBuffer(countCurrentUser));
        extension.addOrUpdateField("referenceValue", "https://example.com/Users/" + (countCurrentUser - 1));
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
