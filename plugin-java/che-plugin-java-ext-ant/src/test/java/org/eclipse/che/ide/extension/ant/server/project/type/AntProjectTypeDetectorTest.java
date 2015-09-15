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

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.DefaultProjectManager;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUser;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import org.eclipse.che.api.vfs.server.impl.memory.MemoryMountPoint;
import org.eclipse.che.ide.ext.java.server.projecttype.JavaProjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Roman Nikitenko
 */

public class AntProjectTypeDetectorTest {

    private static final String workspace   = "my_ws";
    private static final String vfsUserName = "dev";

    private static final String BUILD_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                             "<project basedir=\".\" default=\"build\" name=\"ertrert\">\n" +
                                             "    <property name=\"name\" value=\"ertrert\"/>\n" +
                                             "    <property location=\"basedir/build\" name=\"build\"/>\n" +
                                             "    <property location=\"build/classes\" name=\"build.classes\"/>\n" +
                                             "    <property location=\"{basedir/src\" name=\"src.dir\"/>\n" +
                                             "</project>";

    private AntProjectTypeDetector antProjectTypeDetector;
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
        handlers.add(new AntProjectGenerator());
        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(handlers);

        projectManager = new DefaultProjectManager(vfsRegistry, eventService, projectTypeRegistry, handlerRegistry);

        antProjectTypeDetector = new AntProjectTypeDetector(projectManager);
    }

    @Test
    public void shouldReturnFalseWhenBuildFileNotFound() throws Exception {
        VirtualFile myVfRoot = memoryMountPoint.getRoot();
        VirtualFile project = myVfRoot.createFolder("my_project");
        project.createFolder("someFolder")
               .createFile("someFile", MediaType.TEXT_PLAIN, new ByteArrayInputStream("some content".getBytes()));
        FolderEntry projectFolder = new FolderEntry(workspace, project);

        Assert.assertFalse(antProjectTypeDetector.detect(projectFolder));
    }

    @Test
    public void shouldReturnFalseWhenProjectTypeNotSupports() throws Exception {
        VirtualFile myVfRoot = memoryMountPoint.getRoot();
        VirtualFile project = myVfRoot.createFolder("my_project");
        project.createFolder("someFolder");
        project.createFile("build.xml", MediaType.TEXT_PLAIN, new ByteArrayInputStream("some content".getBytes()));
        FolderEntry projectFolder = new FolderEntry(workspace, project);

        Assert.assertFalse(antProjectTypeDetector.detect(projectFolder));
    }
}
