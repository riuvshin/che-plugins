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
package org.eclipse.che.ide.ext.svn.client.importer;

import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Registrar for getting Subversion into the Project Importer Wizard.
 */
public class SubversionImportWizardRegistrar implements ImportWizardRegistrar {
    private final static String ID = "subversion";
    private final List<Provider<? extends WizardPage<ImportProject>>> wizardPages;

    @Inject
    public SubversionImportWizardRegistrar(final Provider<SubversionProjectImporterPresenter> provider) {
        wizardPages = new ArrayList<>();
        wizardPages.add(provider);
    }

    @NotNull
    public String getImporterId() {
        return ID;
    }

    @NotNull
    public List<Provider<? extends WizardPage<ImportProject>>> getWizardPages() {
        return wizardPages;
    }
}
