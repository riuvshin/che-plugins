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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.project.node.HasDataObject;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.JavaUtils;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Action to create new Java package.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class NewPackageAction extends AbstractNewResourceAction {
    private final InputValidator nameValidator = new NameValidator();

    @Inject
    public NewPackageAction(JavaResources javaResources, JavaLocalizationConstant localizationConstant) {
        super(localizationConstant.actionNewPackageTitle(),
              localizationConstant.actionNewPackageDescription(),
              javaResources.packageIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        InputDialog inputDialog = dialogFactory.createInputDialog("New " + title, "Name:", new InputCallback() {
            @Override
            public void accepted(String value) {
                onAccepted(value);
            }
        }, null).withValidator(nameValidator);
        inputDialog.show();
    }

    private void onAccepted(String value) {
        final FolderReferenceNode parent = (FolderReferenceNode)getResourceBasedNode();
        if (parent == null) {
            throw new IllegalStateException("No selected parent.");
        }

        final String path = parent.getStorablePath() + '/' + value.replace('.', '/');

        projectServiceClient.createFolder(path, createCallback(parent));
    }

    @Override
    protected AsyncRequestCallback<ItemReference> createCallback(final ResourceBasedNode<?> parent) {
        return new AsyncRequestCallback<ItemReference>(dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class)) {
            @Override
            protected void onSuccess(final ItemReference itemReference) {
                parent.getChildren(false).then(new Operation<List<Node>>() {
                    @Override
                    public void apply(List<Node> cachedChildren) throws OperationException {
                        HasDataObject dataObject = new HasDataObject() {
                            @NotNull
                            @Override
                            public Object getData() {
                                return itemReference;
                            }

                            @Override
                            public void setData(@NotNull Object data) {

                            }
                        };


                        if (cachedChildren.size() == 1 && cachedChildren.get(0) instanceof PackageNode) {
                            projectExplorer.reloadChildren(parent.getParent(), dataObject, false, false);
                        } else {
                            projectExplorer.reloadChildren(parent, dataObject, false, false);
                        }
                    }
                });


            }

            @Override
            protected void onFailure(Throwable exception) {
                dialogFactory.createMessageDialog("", JsonHelper.parseJsonMessage(exception.getMessage()), null).show();
            }
        };
    }

    @Override
    public void updateProjectAction(ActionEvent e) {
        Selection<?> selection = projectExplorer.getSelection();

        if (selection == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        List<?> elements = selection.getAllElements();

        if (elements == null || elements.isEmpty() || elements.size() > 1) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Object o = elements.get(0);

        e.getPresentation().setEnabledAndVisible(isSourceFolder(o) || o instanceof PackageNode);
    }

    private boolean isSourceFolder(Object o) {
        if (!(o instanceof FolderReferenceNode)) {
            return false;
        }

        Map<String, List<String>> attributes = ((FolderReferenceNode)o).getAttributes();
        return attributes.containsKey("javaContentRoot");
    }

    private class NameValidator implements InputValidator {
        @Nullable
        @Override
        public Violation validate(String value) {
            try {
                JavaUtils.checkPackageName(value);
            } catch (final IllegalStateException e) {
                return new Violation() {
                    @Nullable
                    @Override
                    public String getMessage() {
                        return e.getMessage();
                    }

                    @Nullable
                    @Override
                    public String getCorrectedValue() {
                        return null;
                    }
                };
            }
            return null;
        }
    }
}
