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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.api.project.tree.AbstractTreeNode;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.generic.FileNode;
import org.eclipse.che.ide.api.project.tree.generic.FolderNode;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaTreeStructure;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract base class for all nodes that represent a container for Java source files and packages.
 * There are exactly two kinds of this container: {@link PackageNode}, {@link SourceFolderNode}.
 * <p/>
 * It may recognize 'empty' child packages as one 'compacted' package (e.g. org.eclipse.che.ide).
 * A package is considered 'empty' if it has only one child package.
 *
 * @author Artem Zatsarynnyy
 * @see SourceFolderNode
 * @see PackageNode
 */
public abstract class AbstractSourceContainerNode extends FolderNode {
    protected static final Comparator<TreeNode> NODE_COMPARATOR = new NodeComparator();

    public AbstractSourceContainerNode(TreeNode<?> parent,
                                       ItemReference data,
                                       JavaTreeStructure treeStructure,
                                       EventBus eventBus,
                                       ProjectServiceClient projectServiceClient,
                                       DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        super(parent, data, treeStructure, eventBus, projectServiceClient, dtoUnmarshallerFactory);
    }

    @NotNull
    @Override
    public JavaTreeStructure getTreeStructure() {
        return (JavaTreeStructure)super.getTreeStructure();
    }

    @Override
    public void refreshChildren(final AsyncCallback<TreeNode<?>> callback) {
        if (!getTreeStructure().getSettings().isCompactEmptyPackages()) {
            // refresh children as simple folder
            super.refreshChildren(callback);
        } else {
            getChildren(getData().getPath(), new AsyncCallback<List<ItemReference>>() {
                @Override
                public void onSuccess(List<ItemReference> childItems) {
                    getChildNodesForItems(childItems, new AsyncCallback<List<TreeNode<?>>>() {
                        @Override
                        public void onSuccess(List<TreeNode<?>> result) {
                            setChildren(result);
                            callback.onSuccess(AbstractSourceContainerNode.this);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }
                    });
                }

                @Override
                public void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                }
            });
        }
    }

    private void getChildNodesForItems(final List<ItemReference> childItems, final AsyncCallback<List<TreeNode<?>>> callback) {
        final List<TreeNode<?>> oldChildren = getChildren();
        final List<TreeNode<?>> newChildren = new ArrayList<>();
        if (childItems.isEmpty()) {
            callback.onSuccess(newChildren);
            return;
        }
        final int[] asyncCounter = new int[1];
        for (final ItemReference item : childItems) {
            getCompactedPackageItemReference(item, new AsyncCallback<ItemReference>() {
                @Override
                public void onSuccess(ItemReference fileItemOrCompactedPackageItem) {
                    asyncCounter[0]++;
                    final AbstractTreeNode node = createChildNode(fileItemOrCompactedPackageItem);
                    if (node != null) {
                        if (oldChildren.contains(node)) {
                            final int i = oldChildren.indexOf(node);
                            newChildren.add(oldChildren.get(i));
                        } else {
                            newChildren.add(node);
                        }
                    }
                    if (childItems.size() == asyncCounter[0]) {
                        Collections.sort(newChildren, NODE_COMPARATOR);
                        callback.onSuccess(newChildren);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    Log.error(AbstractSourceContainerNode.class, caught);
                }
            });
        }
    }

    private void getCompactedPackageItemReference(final ItemReference item, final AsyncCallback<ItemReference> callback) {
        if (!"folder".equals(item.getType())) {
            callback.onSuccess(item);
        } else {
            getChildren(item.getPath(), new AsyncCallback<List<ItemReference>>() {
                @Override
                public void onSuccess(List<ItemReference> children) {
                    if (children.size() == 1 && "folder".equals(children.get(0).getType())) {
                        final ItemReference emptyPackageItem = children.get(0);
                        getCompactedPackageItemReference(emptyPackageItem, callback);
                    } else {
                        callback.onSuccess(item);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }
            });
        }
    }

    @Nullable
    @Override
    protected AbstractTreeNode<?> createChildNode(ItemReference item) {
        if ("file".equals(item.getType()) && item.getName().endsWith(".java")) {
            return getTreeStructure().newSourceFileNode(this, item);
        } else if ("folder".equals(item.getType())) {
            return getTreeStructure().newPackageNode(this, item);
        } else {
            return super.createChildNode(item);
        }
    }

    @Override
    public boolean canContainsFolder() {
        return false;
    }

    private static class NodeComparator implements Comparator<TreeNode> {
        @Override
        public int compare(TreeNode o1, TreeNode o2) {
            if (o1 instanceof FolderNode && o2 instanceof FileNode) {
                return -1;
            }
            if (o1 instanceof FileNode && o2 instanceof FolderNode) {
                return 1;
            }
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    }
}
