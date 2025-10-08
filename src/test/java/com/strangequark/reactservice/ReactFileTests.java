// Integration file: React
// Integration file: File

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReactFileTests extends ReactTestsBase {
    // Integration function start: Auth
    @Test
    public void ensureToolbarFilesButtonRedirectsToLoginTest() {
        reactFunctions.navigateToLogin(page);

        reactFunctions.clickToolbarFilesButton(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(loginDiv.isVisible(), "Login div should be visible after clicking the Files button on the " +
                "toolbar without logging in");
    }
    // Integration function end: Auth
    @Test
    public void userCreateCollectionTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        Locator collectionIcon = page.getByText(collectionName);

        collectionIcon.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(collectionIcon.isVisible(), "Collection icon should be visible after creating new collection");
    }

    @Test
    public void userUploadFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickAndSelectUploadFile(page, textFilePath);

        Locator uploadedFile = page.getByText(textFileName);

        uploadedFile.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(uploadedFile.isVisible(), "Test file should be visible after uploading");
    }

    @Test
    public void userDownloadFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickAndSelectUploadFile(page, textFilePath);

        Download download = reactFunctions.clickFileDownloadButton(page);

        assertEquals(textFileName, download.suggestedFilename(), "Download file name should match textFileName");
    }

    @Test
    public void userDeleteFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickAndSelectUploadFile(page, textFilePath);

        reactFunctions.clickDeleteFile(page);

        Locator deletedFile = page.getByText(textFileName);

        deletedFile.waitFor(WAIT_FOR_DETACHED);
        assertFalse(deletedFile.isVisible(), "Test file should not be visible after deletion");
    }

    @Test
    public void userDeleteCollectionTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickCollectionManagementIcon(page);

        reactFunctions.handleNextAlert(page, true);
        reactFunctions.clickDeleteCollectionButton(page);

        Locator collectionPageHeader = page.getByText("Select a Collection");

        collectionPageHeader.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(collectionPageHeader.isVisible(), "User should have been navigated to collection selection after deleting a collection");
    }
}
