// Integration file: React
// Integration file: File

package com.strangequark.reactservice;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReactFileTests extends ReactTestsBase {
    @Test
    public void ensureToolbarFilesButtonRedirectsToLoginTest() {
        reactFunctions.navigateToLogin(page);

        reactFunctions.clickToolbarFilesButton(page);

        Locator loginDiv = page.locator("id=login-div");

        loginDiv.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(loginDiv.isVisible(), "Login div should be visible after clicking the Files button on the " +
                "toolbar without logging in");
    }

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

        Locator uploadedFile = page.getByText("testUploadFile.txt");

        uploadedFile.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(uploadedFile.isVisible(), "Test file should be visible after uploading");
    }
}
