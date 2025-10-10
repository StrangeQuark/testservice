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
    public void userListenAudioFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickAndSelectUploadFile(page, audioFilePath);

        reactFunctions.clickListenToFile(page);

        Locator audioPlayer = page.getByTestId("audio-player");
        audioPlayer.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(audioPlayer.isVisible(), "Audio player should be visible after clicking Listen button");

        Locator audioSource = page.getByTestId("audio-source");
        String src = audioSource.getAttribute("src");
        Assertions.assertNotNull(src, "Audio src should not be null");
        Assertions.assertTrue(src.startsWith("blob:"), "Audio src should be a blob URL");

        String type = audioSource.getAttribute("type");
        Assertions.assertEquals("audio/mpeg", type, "Expected correct MIME type");
    }

    @Test
    public void userStreamVideoFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickAndSelectUploadFile(page, videoFilePath);

        reactFunctions.clickStreamFile(page);

        Locator videoPlayer = page.getByTestId("video-player");
        videoPlayer.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(videoPlayer.isVisible(), "Video player should be visible after clicking Stream button");

        String src = videoPlayer.getAttribute("src");
        assertNotNull(src, "Video src should not be null");
        assertTrue(src.startsWith("blob:"), "Video src should be a blob URL");
    }

    @Test
    public void userViewImageFileTest() {
        reactFunctions.registerEnableAndLogin(page, username, email, password); // Integration line: Auth
        reactFunctions.navigateToFiles(page);

        reactFunctions.fillAndSubmitCreateCollectionForm(page, collectionName);

        reactFunctions.clickCollectionIcon(page, collectionName);

        reactFunctions.clickAndSelectUploadFile(page, imageFilePath);

        reactFunctions.clickViewFile(page);

        Locator imageViewer = page.getByTestId("image");
        imageViewer.waitFor(WAIT_FOR_VISIBLE);
        assertTrue(imageViewer.isVisible(), "Image viewer should be visible after clicking View button");

        String src = imageViewer.getAttribute("src");
        assertNotNull(src, "Image src should not be null");
        assertTrue(src.startsWith("blob:"), "Image src should be a blob URL");
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
