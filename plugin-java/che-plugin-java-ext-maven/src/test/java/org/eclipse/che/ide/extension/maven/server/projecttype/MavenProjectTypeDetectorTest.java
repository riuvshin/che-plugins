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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.DefaultProjectManager;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.ProjectConfig;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUser;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import org.eclipse.che.api.vfs.server.impl.memory.MemoryMountPoint;
import org.eclipse.che.ide.ext.java.server.projecttype.JavaProjectType;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.GeneratorStrategy;
import org.eclipse.che.ide.extension.maven.server.projecttype.handler.MavenProjectGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.VERSION;

/**
 * @author Roman Nikitenko
 */

public class MavenProjectTypeDetectorTest {

    private static final String workspace   = "my_ws";
    private static final String vfsUserName = "dev";

    private static final String POM_XML_TEMPL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                "<project>\n" +
                                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                                "    <artifactId>artifact-id</artifactId>\n" +
                                                "    <groupId>group-id</groupId>\n" +
                                                "    <version>x.x.x</version>\n" +
                                                "    <packaging>jar</packaging\n>" +
                                                "    <modules>\n" +
                                                "        <module>firstModule</module>\n" +
                                                "        <module>secondModule</module>\n" +
                                                "    </modules>\n" +
                                                "</project>";

    private MavenProjectTypeDetector mavenProjectTypeDetector;
    private ProjectManager   projectManager;
    private MemoryMountPoint memoryMountPoint;

    @Before
    public void setUp() throws Exception {

        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));
        final EventService eventService = new EventService();
        VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
        final MemoryFileSystemProvider memoryFileSystemProvider =
                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
                    @Override
                    public VirtualFileSystemUser getVirtualFileSystemUser() {
                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
                    }
                }, vfsRegistry);
        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);

        memoryMountPoint = new MemoryMountPoint(workspace, new EventService(), null, new VirtualFileSystemUserContext() {
            @Override
            public VirtualFileSystemUser getVirtualFileSystemUser() {
                return new VirtualFileSystemUser(vfsUserName, vfsUserGroups);
            }
        });

        Set<ProjectType> projTypes = new HashSet<>();
        projTypes.add(new JavaProjectType());
        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(projTypes);

        Set<ProjectHandler> handlers = new HashSet<>();
        handlers.add(new MavenProjectGenerator(Collections.<GeneratorStrategy>emptySet()));
        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(handlers);

        projectManager = new DefaultProjectManager(vfsRegistry, eventService, projectTypeRegistry, handlerRegistry);

        mavenProjectTypeDetector = new MavenProjectTypeDetector(projectManager);
    }

    @Test
    public void shouldReturnFalseWhenPomNotFound() throws Exception {
        VirtualFile myVfRoot = memoryMountPoint.getRoot();
        VirtualFile project = myVfRoot.createFolder("my_project");
        project.createFolder("someFolder")
               .createFile("someFile", MediaType.TEXT_PLAIN, new ByteArrayInputStream("some content".getBytes()));
        FolderEntry projectFolder = new FolderEntry(workspace, project);

        Assert.assertFalse(mavenProjectTypeDetector.detect(projectFolder));
    }

    @Test
    public void shouldReturnFalseWhenProjectTypeNotSupports() throws Exception {
        VirtualFile myVfRoot = memoryMountPoint.getRoot();
        VirtualFile project = myVfRoot.createFolder("my_project");
        project.createFolder("someFolder");
        project.createFile("pom.xml", MediaType.TEXT_PLAIN, new ByteArrayInputStream("some content".getBytes()));
        FolderEntry projectFolder = new FolderEntry(workspace, project);

        Assert.assertFalse(mavenProjectTypeDetector.detect(projectFolder));
    }

    @Test
    public void shouldCreateConfig() throws Exception {
        projectManager.getProjectTypeRegistry()
                      .registerProjectType(new MavenProjectType(new MavenValueProviderFactory(), new JavaProjectType()));

        FolderEntry projectFolder = projectManager.getProjectsRoot(workspace).createFolder("my_project");
        projectFolder.createFolder("someFolder");
        projectFolder.createFile("pom.xml", new ByteArrayInputStream(POM_XML_TEMPL.getBytes()), MediaType.TEXT_PLAIN);

        Assert.assertTrue(mavenProjectTypeDetector.detect(projectFolder));

        Project project = projectManager.getProject(workspace, "my_project");
        ProjectConfig projectConfig = project.getConfig();
        Map<String, AttributeValue> attributes = projectConfig.getAttributes();

        Assert.assertNotNull(project);
        Assert.assertEquals(MAVEN_ID, projectConfig.getTypeId());
        Assert.assertEquals("maven", projectConfig.getBuilders().getDefault());
        Assert.assertEquals("artifact-id", attributes.get(ARTIFACT_ID).getString());
        Assert.assertEquals("group-id", attributes.get(GROUP_ID).getString());
        Assert.assertEquals("x.x.x", attributes.get(VERSION).getString());
        Assert.assertEquals("jar", attributes.get(PACKAGING).getString());
    }
}
