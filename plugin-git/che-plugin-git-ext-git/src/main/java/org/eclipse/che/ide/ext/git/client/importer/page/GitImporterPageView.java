/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.importer.page;

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;

/**
 * @author Roman Nikitenko
 */
@ImplementedBy(GitImporterPageViewImpl.class)
public interface GitImporterPageView extends View<GitImporterPageView.ActionDelegate> {

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having changed the project's name. */
        void projectNameChanged(@NotNull String name);

        /** Performs any actions appropriate in response to the user having changed the project's URL. */
        void projectUrlChanged(@NotNull String url);

        /** Performs any actions appropriate in response to the user having changed the project's description. */
        void projectDescriptionChanged(@NotNull String projectDescriptionValue);

        /** Performs any actions appropriate in response to the user having changed the project's visibility. */
        void projectVisibilityChanged(boolean aPublic);

        /** Perform actions when selecting Keep Directory checkbox. */
        void keepDirectorySelected(boolean keepDirectory);

        /** Perform actions when changing the name of a directory. */
        void keepDirectoryNameChanged(@NotNull String url);
    }

    /**
     * Show the name error.
     */
    void showNameError();

    /**
     * Hide the name error.
     */
    void hideNameError();

    /**
     * Show URL error.
     */
    void showUrlError(@NotNull String message);

    /**
     * Hide URL error.
     */
    void hideUrlError();

    /**
     * Set the project's URL.
     *
     * @param url
     *         the project's URL to set
     */
    void setProjectUrl(@NotNull String url);

    /**
     * Get the project's name value.
     *
     * @return {@link String} project's name
     */
    @NotNull
    String getProjectName();

    /**
     * Set the project's name value.
     *
     * @param projectName
     *         project's name to set
     */
    void setProjectName(@NotNull String projectName);

    /**
     * Focuses URL field.
     */
    void focusInUrlInput();

    /**
     * Set the enable state of the inputs.
     *
     * @param isEnabled
     *         <code>true</code> if enabled, <code>false</code> if disabled
     */
    void setInputsEnableState(boolean isEnabled);

    /**
     * Sets project description field value.
     *
     * @param projectDescription project description
     */
    void setProjectDescription(@NotNull String projectDescription);

    /**
     * Updates project visibility.
     *
     * @param visibility project visibility
     */
    void setProjectVisibility(boolean visibility);

    /**
     * Returns whether user wants to checkout a special directory.
     *
     * @return <b>true</b> if user has checked the Keep Director checkbox, otherwise returns <b>false</b>
     */
    boolean keepDirectory();

    /**
     *  Sets new value of Keep Directory checkbox.
     *
     * @param checked <b>true</b> to check the field or <b>false</b> to leave it unchecked
     */
    void setKeepDirectoryChecked(boolean checked);

    /**
     * Returns the name of a directory to checkout.
     *
     * @return name of a directory to checkout
     */
    String getDirectoryName();

    /**
     * Sets new value of Directory name field.
     *
     * @param directoryName new value of directory name field
     */
    void setDirectoryName(String directoryName);

    /**
     * Enables or disables Directory name field.
     *
     * @param enable true or false to enable or disable the field
     */
    void enableDirectoryNameField(boolean enable);

    /***
     * Highlights Directory name field to notify its value is invalid.
     *
     * @param highlight <b>true</b> to highlight the field or <b>false</b> to remove highlighting
     */
    void highlightDirectoryNameField(boolean highlight);

    /**
     * Focuses directory name field.
     */
    void focusDirectoryNameFiend();

}
