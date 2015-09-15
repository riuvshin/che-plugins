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
package org.eclipse.che.ide.ext.java.client.projecttree.nodes;

import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaTreeStructure;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * @author Evgen Vidolob
 */
public class JarClassNode extends JarEntryNode implements VirtualFile {

    /**
     * Creates new node with the specified parent, associated data and display name.
     *
     * @param parent
     *         parent node
     * @param data
     *         an object this node encapsulates
     * @param treeStructure
     *         {@link org.eclipse.che.ide.ext.java.client.projecttree.JavaTreeStructure} which this node belongs
     * @param libId
     * @param eventBus
     * @param service
     * @param dtoUnmarshallerFactory
     * @param iconRegistry
     */
    @Inject
    public JarClassNode(@Assisted TreeNode<?> parent, @Assisted JarEntry data, @Assisted JavaTreeStructure treeStructure,
                        @Assisted int libId, EventBus eventBus, JavaNavigationService service,
                        DtoUnmarshallerFactory dtoUnmarshallerFactory, IconRegistry iconRegistry) {
        super(parent, data, treeStructure, eventBus, libId, service, dtoUnmarshallerFactory);
        setDisplayIcon(iconRegistry.getIcon("java.class").getSVGImage());
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {

    }

    @Override
    public void processNodeAction() {
        eventBus.fireEvent(new FileEvent(this, FileEvent.FileOperation.OPEN));
    }

    @NotNull
    @Override
    public String getPath() {
        return getData().getPath();
    }

    @NotNull
    @Override
    public String getName() {
        return getData().getName();
    }

    @Nullable
    @Override
    public String getMediaType() {
        return "application/java-class";
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getContentUrl() {
        return null;
    }

    @Override
    public void getContent(final AsyncCallback<String> callback) {
        service.getContent(getProject().getPath(), libId, getData().getPath(), new AsyncRequestCallback<String>(new StringUnmarshaller()) {
            @Override
            protected void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void updateContent(String content, AsyncCallback<Void> callback) {
        throw new UnsupportedOperationException("Update content on class file is not supported.");
    }
}
