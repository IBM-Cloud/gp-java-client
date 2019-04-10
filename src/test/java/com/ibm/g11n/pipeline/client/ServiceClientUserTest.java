/*  
 * Copyright IBM Corp. 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.g11n.pipeline.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeFalse;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test cases for user operation APIs in ServiceClient.
 * 
 * @author Yoshito Umaoka
 */
public class ServiceClientUserTest extends AbstractServiceClientTest {

    private static final String USER_COMMENT_PREFIX = "junit-user-";
    private static final int WAIT_TIME = 100;

    // TODO: For access control tests to be added later
    private static final String BUNDLE_ID1 = "junit-bundle-1";
    private static final String BUNDLE_ID2 = "junit-bundle-2";
    private static final String SOURCE_LANG = "en";
    private static final String TARGET_LANG = "fr";
    private static final String KEY1 = "key1";
    private static final String VAL1 = "value 1";

    @BeforeClass
    public static void initBundles() throws ServiceException {
        if (client != null) {
            NewBundleData bundleData1 = new NewBundleData(SOURCE_LANG);
            bundleData1.setTargetLanguages(Collections.singleton(TARGET_LANG));
            client.createBundle(BUNDLE_ID1, bundleData1);
            client.uploadResourceStrings(BUNDLE_ID1, SOURCE_LANG,
                    Collections.singletonMap(KEY1, VAL1));

            NewBundleData bundleData2 = new NewBundleData(SOURCE_LANG);
            bundleData2.setTargetLanguages(Collections.singleton(TARGET_LANG));
            client.createBundle(BUNDLE_ID2, bundleData2);
            client.uploadResourceStrings(BUNDLE_ID2, SOURCE_LANG,
                    Collections.singletonMap(KEY1, VAL1));
        }
    }

    @AfterClass
    public static void cleanupbundles() throws ServiceException {
        if (client != null) {
            client.deleteBundle(BUNDLE_ID1);
            client.deleteBundle(BUNDLE_ID2);
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    @After
    public void deleteTestUsers() throws ServiceException {
        if (client != null) {
            Map<String, UserData> users = client.getUsers();
            for (UserData user : users.values()) {
                if (isTestUser(user)) {
                    client.deleteUser(user.getId());
                }
            }
        }
    }

    //
    // getUsers
    //

    @Test
    public void getUsers_Initial_ShouldContainThisUser() throws ServiceException {
        assumeFalse(account.isIamEnabled());
        String userId = account.getUserId();
        Map<String, UserData> users = client.getUsers();
        UserData user = users.get(userId);
        assertNotNull("should contain this user", user);
        assertEquals("this user should be ADMINISTRATOR",
                UserType.ADMINISTRATOR, user.getType());
    }

    @Test
    public void getUsers_Initial_ShouldNotContainTestUsers() throws ServiceException {
        Map<String, UserData> users = client.getUsers();
        for (UserData user : users.values()) {
            assertFalse("should be no test users initially",
                    isTestUser(user));
        }
    }

    @Test
    public void getUsers_NumberOfUsers_ShouldBeChanged() throws ServiceException, InterruptedException {
        Map<String, UserData> users = client.getUsers();
        int numUsers0 = users.size();

        // Create a new user
        UserData newUser = createAdmin("NumberOfUsers");

        Thread.sleep(WAIT_TIME);

        users = client.getUsers();
        int numUsers1 = users.size();

        assertEquals("number of users should be incremented",
                numUsers0 + 1, numUsers1);

        // Delete a user
        client.deleteUser(newUser.getId());

        Thread.sleep(WAIT_TIME);

        users = client.getUsers();
        int numUsers2 = users.size();

        assertEquals("number of users should be decremented",
                numUsers0, numUsers2);
    }

    //
    // createUser
    //

    @Test
    public void createUser_NewAdmin_ShouldBeAdminType() throws ServiceException, InterruptedException {
        UserData newUser = createAdmin("NewAdmin");
        Thread.sleep(WAIT_TIME);

        UserData user = client.getUser(newUser.getId());
        assertEquals("should be admin user", UserType.ADMINISTRATOR, user.getType());
    }

    public void createUser_NewAdmin_CanCreateAnotherUser() throws ServiceException, InterruptedException {
        UserData newUser = createAdmin("NewAdmin");
        Thread.sleep(WAIT_TIME);

        ServiceClient newClient = ServiceClient.getInstance(
                ServiceAccount.getInstance(account.getUrl(), account.getInstanceId(),
                        newUser.getId(), newUser.getPassword()));

        NewUserData anotherUser = new NewUserData(UserType.ADMINISTRATOR);
        anotherUser.setComment(testUserComment("NewAdmin-Another"));
        newClient.createUser(anotherUser);
    }

    @Test
    public void createUser_NewAdmin_ShouldNotBeServiceManaged() throws ServiceException, InterruptedException {
        UserData newUser = createAdmin("NewAdmin");
        assertFalse("should not be service managed", newUser.isServiceManaged());
    }

    @Test
    public void createUser_NewTranslator_ShouldBeTranslatorType()
            throws ServiceException, InterruptedException {
        UserData newUser = createTranslator("NewTranslator", null);
        Thread.sleep(WAIT_TIME);

        UserData user = client.getUser(newUser.getId());
        assertEquals("should be translator user", UserType.TRANSLATOR, user.getType());
    }

    @Test
    public void createUser_NewReader_ShouldBeReaderType()
            throws ServiceException, InterruptedException {
        UserData newUser = createReader("NewReader", null);
        Thread.sleep(WAIT_TIME);

        UserData user = client.getUser(newUser.getId());
        assertEquals("should be reader user", UserType.READER, user.getType());
    }

    @Test
    public void createUser_NewReader_Properties()
            throws ServiceException, InterruptedException {

        String comment = testUserComment("NewReader-Props");
        String dispName = "JUnit test reader";
        String externalId = "gp-reader@acme.com";
        Set<String> bundles = Collections.singleton("my.bundle");
        Map<String, String> metadata = Collections.singletonMap("m1", "v1");

        NewUserData newUserData = new NewUserData(UserType.READER);
        newUserData.setComment(comment);
        newUserData.setDisplayName(dispName);
        newUserData.setExternalId(externalId);
        newUserData.setBundles(bundles);
        newUserData.setMetadata(metadata);

        UserData newUser = client.createUser(newUserData);
        String userId = newUser.getId();

        assertNotNull("id should not be null", userId);
        assertNotNull("password should not be null", newUser.getPassword());
        assertEquals("comment should match", comment, newUser.getComment());
        assertEquals("displayName should match", dispName, newUser.getDisplayName());
        assertEquals("externalId should match", externalId, newUser.getExternalId());
        assertEquals("bundles should match", bundles, newUser.getBundles());
        assertEquals("metadata should match", metadata, newUser.getMetadata());

        Thread.sleep(WAIT_TIME);

        UserData theUser = client.getUser(userId);

        assertEquals("getUser - id should match", userId, theUser.getId());
        assertNull("getUser - password should be null", theUser.getPassword());  // must be null
        assertEquals("getUser - comment should match", comment, theUser.getComment());
        assertEquals("getUser - displayName should match", dispName, theUser.getDisplayName());
        assertEquals("getUser - externalId should match", externalId, theUser.getExternalId());
        assertEquals("getUser - bundles should match", bundles, theUser.getBundles());
        assertEquals("getUser - metadata should match", metadata, theUser.getMetadata());
    }

    //
    // getUser
    //

    @Test
    public void getUser_NonExisting_ShouldFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        client.getUser("$$$");
    }

    @Test
    public void getUser_Deleted_ShouldFail() throws ServiceException, InterruptedException {
        UserData newUser = createAdmin("NewAdmin");
        String userId = newUser.getId();

        Thread.sleep(WAIT_TIME);

        // Delete the user
        client.deleteUser(userId);

        Thread.sleep(WAIT_TIME);

        // Get the user - should fail
        expectedException.expect(ServiceException.class);
        client.getUser(userId);
    }

    //
    // updateUser
    //

    @Test
    public void updateUser_UpdateProps_ShouldChange() 
            throws ServiceException, InterruptedException {
        UserData newUser = createTranslator("NewTranslator", null);
        String userId = newUser.getId();

        Thread.sleep(WAIT_TIME);

        // update user properties
        String comment = testUserComment("UpdateProps");
        String dispName = "JUnit test reader";
        String externalId = "gp-reader@acme.com";
        Set<String> bundles = Collections.singleton("my.bundle");
        Map<String, String> metadata = Collections.singletonMap("m1", "v1");

        UserDataChangeSet changes = new UserDataChangeSet();
        changes.setComment(comment);
        changes.setDisplayName(dispName);
        changes.setExternalId(externalId);
        changes.setBundles(bundles);
        changes.setMetadata(metadata);

        UserData updUser = client.updateUser(userId, changes, false);

        assertEquals("id should match", userId, updUser.getId());
        assertNull("password should be null", updUser.getPassword());
        assertEquals("comment should be updated", comment, updUser.getComment());
        assertEquals("displayName should be updated", dispName, updUser.getDisplayName());
        assertEquals("externalId should be updated", externalId, updUser.getExternalId());
        assertEquals("bundles should be updated", bundles, updUser.getBundles());
        assertEquals("metadata should be updated", metadata, updUser.getMetadata());

        Thread.sleep(WAIT_TIME);

        // get user again
        UserData user = client.getUser(userId);

        assertEquals("getUser - id should match", userId, user.getId());
        assertNull("getUser - password should be null", user.getPassword());
        assertEquals("getUser - comment should be updated", comment, user.getComment());
        assertEquals("getUser - displayName should be updated", dispName, user.getDisplayName());
        assertEquals("getUser - externalId should be updated", externalId, user.getExternalId());
        assertEquals("getUser - bundles should be updated", bundles, user.getBundles());
        assertEquals("getUser - metadata should be updated", metadata, user.getMetadata());
    }

    @Test
    public void updateUser_ResetPassword_ShouldCreateNewPassword()
            throws ServiceException, InterruptedException {
        UserData newUser = createReader("NewReader", null);
        String password = newUser.getPassword();
        assertNotNull("password should not be empty", password);

        Thread.sleep(WAIT_TIME);

        UserData updUser = client.updateUser(newUser.getId(), null, true);
        String newPassword = updUser.getPassword();
        assertNotEquals("password should be updated", password, newPassword);
    }

    @Test
    public void updateUser_NonExisting_ShouldFail() throws ServiceException {
        UserDataChangeSet userChanges = new UserDataChangeSet();
        userChanges.setDisplayName("new display name");
        expectedException.expect(ServiceException.class);
        client.updateUser("$$$", userChanges, false);
    }


    //
    // Access control tests
    //

    // TODO - add access control tests here

    //
    // private test case helper methods
    //

    private static String testUserComment(String comment) {
        return USER_COMMENT_PREFIX + comment;
    }

    private static boolean isTestUser(UserData user) {
        String comment = user.getComment();
        if (comment != null && comment.startsWith(USER_COMMENT_PREFIX)) {
            return true;
        }
        return false;
    }

    private static UserData createAdmin(String comment) throws ServiceException {
        NewUserData newUser = new NewUserData(UserType.ADMINISTRATOR);
        newUser.setComment(testUserComment(comment));
        return client.createUser(newUser);
    }

    private static UserData createTranslator(String comment, Set<String> bundles)
            throws ServiceException {
        NewUserData newUser = new NewUserData(UserType.TRANSLATOR);
        newUser.setComment(testUserComment(comment));
        newUser.setBundles(bundles);
        return client.createUser(newUser);
    }

    private static UserData createReader(String comment, Set<String> bundles)
            throws ServiceException {
        NewUserData newUser = new NewUserData(UserType.READER);
        newUser.setComment(testUserComment(comment));
        newUser.setBundles(bundles);
        return client.createUser(newUser);
    }
}
