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
package org.eclipse.che.ide.extension.maven.client.wizard;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.GeneratorDescription;
import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.maven.client.MavenArchetype;
import org.eclipse.che.ide.extension.maven.client.MavenExtension;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringMapListUnmarshaller;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.CREATE_MODULE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode.UPDATE;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.PROJECT_PATH_KEY;
import static org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar.WIZARD_MODE_KEY;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_ARTIFACT_ID_OPTION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GENERATION_STRATEGY;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_GROUP_ID_OPTION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_REPOSITORY_OPTION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARCHETYPE_VERSION_OPTION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.DEFAULT_VERSION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GENERATION_STRATEGY_OPTION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.PARENT_VERSION;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.SOURCE_FOLDER;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyy
 */
public class MavenPagePresenter extends AbstractWizardPage<ImportProject> implements MavenPageView.ActionDelegate {

    protected final MavenPageView        view;
    protected final EventBus             eventBus;
    private final   ProjectServiceClient projectServiceClient;
    private final   DtoFactory           dtoFactory;
    private DialogFactory dialogFactory;

    @Inject
    public MavenPagePresenter(MavenPageView view,
                              EventBus eventBus,
                              ProjectServiceClient projectServiceClient,
                              DtoFactory dtoFactory,
                              DialogFactory dialogFactory) {
        super();
        this.view = view;
        this.eventBus = eventBus;
        this.projectServiceClient = projectServiceClient;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        view.setDelegate(this);
    }

    @Override
    public void init(ImportProject dataObject) {
        super.init(dataObject);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        if (CREATE == wizardMode) {
            // set default values
            setAttribute(VERSION, DEFAULT_VERSION);
            setAttribute(PACKAGING, "jar");
            setAttribute(SOURCE_FOLDER, DEFAULT_SOURCE_FOLDER);
            setAttribute(TEST_SOURCE_FOLDER, DEFAULT_TEST_SOURCE_FOLDER);
        } else if (CREATE_MODULE == wizardMode || UPDATE == wizardMode && getAttribute(ARTIFACT_ID).isEmpty()) {
            estimateAndSetAttributes();
        }
    }

    private void estimateAndSetAttributes() {
        projectServiceClient.estimateProject(
                context.get(PROJECT_PATH_KEY), MAVEN_ID,
                new AsyncRequestCallback<Map<String, List<String>>>(new StringMapListUnmarshaller()) {
                    @Override
                    protected void onSuccess(Map<String, List<String>> result) {
                        List<String> artifactIdValues = result.get(ARTIFACT_ID);
                        if (artifactIdValues != null && !artifactIdValues.isEmpty()) {
                            setAttribute(ARTIFACT_ID, artifactIdValues.get(0));
                        }

                        List<String> groupIdValues = result.get(GROUP_ID);
                        List<String> parentGroupIdValues = result.get(PARENT_GROUP_ID);
                        if (groupIdValues != null && !groupIdValues.isEmpty()) {
                            setAttribute(GROUP_ID, groupIdValues.get(0));
                        } else if (parentGroupIdValues != null && !parentGroupIdValues.isEmpty()) {
                            setAttribute(GROUP_ID, parentGroupIdValues.get(0));
                        }

                        List<String> versionValues = result.get(VERSION);
                        List<String> parentVersionValues = result.get(PARENT_VERSION);
                        if (versionValues != null && !versionValues.isEmpty()) {
                            setAttribute(VERSION, versionValues.get(0));
                        } else if (parentVersionValues != null && !parentVersionValues.isEmpty()) {
                            setAttribute(VERSION, parentVersionValues.get(0));
                        }

                        List<String> packagingValues = result.get(PACKAGING);
                        if (packagingValues != null && !packagingValues.isEmpty()) {
                            setAttribute(PACKAGING, packagingValues.get(0));
                        }

                        updateDelegate.updateControls();
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        final String message = dtoFactory.createDtoFromJson(exception.getMessage(), ServiceError.class).getMessage();
                        dialogFactory.createMessageDialog("Not valid Maven project", message, null).show();
                        Log.error(MavenPagePresenter.class, exception);
                    }
                });
    }

    @Override
    public boolean isCompleted() {
        return isCoordinatesCompleted();
    }

    private boolean isCoordinatesCompleted() {
        final String artifactId = getAttribute(ARTIFACT_ID);
        final String groupId = getAttribute(GROUP_ID);
        final String version = getAttribute(VERSION);

        return !(artifactId.isEmpty() || groupId.isEmpty() || version.isEmpty());
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        final ProjectWizardMode wizardMode = ProjectWizardMode.parse(context.get(WIZARD_MODE_KEY));
        final String projectName = dataObject.getProject().getName();

        // use project name for artifactId and groupId for new project
        if (CREATE == wizardMode && projectName != null) {
            if (getAttribute(ARTIFACT_ID).isEmpty()) {
                setAttribute(ARTIFACT_ID, projectName);
            }
            if (getAttribute(GROUP_ID).isEmpty()) {
                setAttribute(GROUP_ID, projectName);
            }
            updateDelegate.updateControls();
        }

        updateView();
        validateCoordinates();

        view.setArchetypeSectionVisibility(CREATE == wizardMode);
        view.enableArchetypes(view.isGenerateFromArchetypeSelected());
    }

    /** Updates view from data-object. */
    private void updateView() {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();

        final String artifactId = getAttribute(ARTIFACT_ID);
        if (!artifactId.isEmpty()) {
            view.setArtifactId(artifactId);
        }

        if (attributes.get(GROUP_ID) != null) {
            view.setGroupId(getAttribute(GROUP_ID));
        } else {
            view.setGroupId(getAttribute(PARENT_GROUP_ID));
        }

        if (attributes.get(VERSION) != null) {
            view.setVersion(getAttribute(VERSION));
        } else {
            view.setVersion(getAttribute(PARENT_VERSION));
        }

        final String packaging = getAttribute(PACKAGING);
        if (!packaging.isEmpty()) {
            view.setPackaging(packaging);
        }
    }

    @Override
    public void onCoordinatesChanged() {
        setAttribute(ARTIFACT_ID, view.getArtifactId());
        setAttribute(GROUP_ID, view.getGroupId());
        setAttribute(VERSION, view.getVersion());

        packagingChanged(view.getPackaging());
        validateCoordinates();
        updateDelegate.updateControls();
    }

    @Override
    public void packagingChanged(String packaging) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(PACKAGING, Arrays.asList(packaging));
        if ("pom".equals(packaging)) {
            attributes.remove(SOURCE_FOLDER);
            attributes.remove(TEST_SOURCE_FOLDER);
        } else {
            attributes.put(SOURCE_FOLDER, Arrays.asList(DEFAULT_SOURCE_FOLDER));
            attributes.put(TEST_SOURCE_FOLDER, Arrays.asList(DEFAULT_TEST_SOURCE_FOLDER));
        }

        updateDelegate.updateControls();
    }

    @Override
    public void generateFromArchetypeChanged(boolean isGenerateFromArchetype) {
        view.setPackagingVisibility(!isGenerateFromArchetype);
        view.enableArchetypes(isGenerateFromArchetype);
        if (!isGenerateFromArchetype) {
            view.clearArchetypes();
        } else {
            view.setArchetypes(MavenExtension.getAvailableArchetypes());
        }

        final GeneratorDescription generatorDescription = dtoFactory.createDto(GeneratorDescription.class);
        if (isGenerateFromArchetype) {
            fillGeneratorDescription(generatorDescription);
        }
        dataObject.getProject().setGeneratorDescription(generatorDescription);

        updateDelegate.updateControls();
    }

    @Override
    public void archetypeChanged(MavenArchetype archetype) {
        fillGeneratorDescription(dataObject.getProject().getGeneratorDescription());
        updateDelegate.updateControls();
    }

    private void fillGeneratorDescription(GeneratorDescription generatorDescription) {
        MavenArchetype archetype = view.getArchetype();
        HashMap<String, String> options = new HashMap<>();
        options.put(GENERATION_STRATEGY_OPTION, ARCHETYPE_GENERATION_STRATEGY);
        options.put(ARCHETYPE_GROUP_ID_OPTION, archetype.getGroupId());
        options.put(ARCHETYPE_ARTIFACT_ID_OPTION, archetype.getArtifactId());
        options.put(ARCHETYPE_VERSION_OPTION, archetype.getVersion());
        if (archetype.getRepository() != null) {
            options.put(ARCHETYPE_REPOSITORY_OPTION, archetype.getRepository());
        }
        generatorDescription.setOptions(options);
    }

    private void validateCoordinates() {
        view.showArtifactIdMissingIndicator(view.getArtifactId().isEmpty());
        view.showGroupIdMissingIndicator(view.getGroupId().isEmpty());
        view.showVersionMissingIndicator(view.getVersion().isEmpty());
    }

    /** Reads single value of attribute from data-object. */
    @NotNull
    private String getAttribute(String attrId) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        List<String> values = attributes.get(attrId);
        if (!(values == null || values.isEmpty())) {
            return values.get(0);
        }
        return "";
    }

    /** Sets single value of attribute of data-object. */
    private void setAttribute(String attrId, String value) {
        Map<String, List<String>> attributes = dataObject.getProject().getAttributes();
        attributes.put(attrId, Arrays.asList(value));
    }
}
