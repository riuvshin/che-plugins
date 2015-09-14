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
package org.eclipse.che.ide.extension.maven.server.projecttype;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectConfig;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeDetector;
import org.eclipse.che.api.project.shared.Builders;
import org.eclipse.che.ide.maven.tools.Model;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * Contains logic for detecting project type for {@link MavenProjectType}.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class MavenProjectTypeDetector implements ProjectTypeDetector {

    private ProjectManager projectManager;

    @Inject
    public MavenProjectTypeDetector(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    /** {@inheritDoc} */
    @Override
    public boolean detect(FolderEntry projectFolder) {
        try {
            VirtualFileEntry pom = projectFolder.getChild("pom.xml");
            if (pom == null) {
                return false;
            }

            ProjectType projectType = projectManager.getProjectTypeRegistry().getProjectType(MAVEN_ID);
            if (projectType == null) {
                return false;
            }

            Project project = new Project(projectFolder, projectManager);
            ProjectConfig projectConfig = createProjectConfig(pom, projectType);
            project.updateConfig(projectConfig);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Create new {@link ProjectConfig} for resolving the project */
    private ProjectConfig createProjectConfig(VirtualFileEntry pom, ProjectType projectType)
            throws ServerException, IOException, ForbiddenException {
        Builders builders = new Builders();
        builders.setDefault(projectType.getDefaultBuilder());

        Model model = Model.readFrom(pom.getVirtualFile());
        Map<String, AttributeValue> attributes = new HashMap<>();
        attributes.put(ARTIFACT_ID, new AttributeValue(model.getArtifactId()));
        attributes.put(GROUP_ID, new AttributeValue(model.getGroupId()));
        attributes.put(VERSION, new AttributeValue(model.getVersion()));
        attributes.put(PACKAGING, new AttributeValue(model.getPackaging()));

        return new ProjectConfig(projectType.getDisplayName(), projectType.getId(), attributes, null, builders, null);
    }
}
