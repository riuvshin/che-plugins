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
package org.eclipse.che.ide.extension.ant.server.project.type;

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectConfig;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDetector;
import org.eclipse.che.api.project.shared.Builders;
import org.eclipse.che.ide.ant.tools.AntUtils;
import org.eclipse.che.ide.extension.ant.shared.AntAttributes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains logic for detecting project type for {@link AntProjectType}.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class AntProjectTypeDetector implements ProjectTypeDetector {

    private ProjectManager projectManager;

    @Inject
    public AntProjectTypeDetector(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    /** {@inheritDoc} */
    @Override
    public boolean detect(FolderEntry projectFolder) {
        try {
            ProjectType projectType = projectManager.getProjectTypeRegistry().getProjectType(AntAttributes.ANT_ID);
            if (projectType == null) {
                return false;
            }
            VirtualFileEntry buildFile = projectFolder.getChild(AntAttributes.BUILD_FILE);
            if (buildFile == null) {
                return false;
            }

            Project project = new Project(projectFolder, projectManager);
            ProjectConfig projectConfig = createProjectConfig(buildFile, projectType);
            project.updateConfig(projectConfig);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Create new {@link ProjectConfig} for resolving the project */
    private ProjectConfig createProjectConfig(VirtualFileEntry buildFile, ProjectType projectType) throws IOException {
        Builders builders = new Builders();
        builders.setDefault(projectType.getDefaultBuilder());
        List<String> sourceDirectories = AntUtils.getSourceDirectories(buildFile.getVirtualFile());
        String sourceDir = "src";
        String testDir = "test";
        for (String directory : sourceDirectories) {
            if (directory.contains("test")) {
                testDir = directory;
                break;
            }
            if (directory.contains("src")) {
                sourceDir = directory;
            }
        }
        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(AntAttributes.SOURCE_FOLDER, new AttributeValue(sourceDir));
        attributes.put(AntAttributes.TEST_SOURCE_FOLDER, new AttributeValue(testDir));

        return new ProjectConfig(projectType.getDisplayName(), projectType.getId(), attributes, null, builders, null);
    }
}
