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
package org.eclipse.che.ide.ext.java.client.project.node.jar;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType.PACKAGE;

/**
 * @author Vlad Zhukovskiy
 */
public class JarFolderNode extends AbstractJarEntryNode {
    @Inject
    public JarFolderNode(@Assisted JarEntry jarEntry,
                         @Assisted int libId,
                         @Assisted ProjectDescriptor projectDescriptor,
                         @Assisted NodeSettings nodeSettings,
                         @NotNull JavaNodeManager nodeManager) {
        super(jarEntry, libId, projectDescriptor, nodeSettings, nodeManager);
    }

    @NotNull
    @Override
    protected Promise<List<Node>> getChildrenImpl() {
        return nodeManager.getJarChildren(getProjectDescriptor(), libId, getData().getPath(), getSettings());
    }

    @Override
    public void updatePresentation(@NotNull NodePresentation presentation) {
        presentation.setPresentableText(getData().getName());
        presentation.setPresentableIcon(getData().getType() == PACKAGE ? nodeManager.getJavaNodesResources().packageFolder()
                                                                       : nodeManager.getNodesResources().simpleRoot());
    }

    @NotNull
    @Override
    public String getName() {
        return getData().getName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
