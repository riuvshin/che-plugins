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
package org.eclipse.che.ide.ext.runner.client.util;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import org.eclipse.che.ide.ext.runner.client.models.Environment;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The class contains business logic which allows us to generate names for environments
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class NameGenerator {
    private static final String CUSTOM_ENV_PREFIX = "ENV";

    /** Utility class */
    private NameGenerator() {

    }


    protected static String removeCopyPrefix(String name) {
        RegExp regexp = RegExp.compile("Copy\\d* of (.*)");
        MatchResult matchResult = regexp.exec(name);
        // do not find prefix, return as this
        if (matchResult == null || matchResult.getGroupCount() != 2) {
            return name;
        }
        return matchResult.getGroup(1);
    }

    /**
     * @return environment name which consists of string 'Copy of ' and existing name with a current date. If there is an existing name,
     * add a number suffix like "Copy2 of", "Copy3 of", etc.
     */
    @NotNull
    public static String generateCopy(@NotNull String name, @NotNull List<Environment> projectEnvironments) {
        List<String> existingNames = new ArrayList<>();

        for (Environment environment : projectEnvironments) {
            existingNames.add(environment.getName());
        }

        name = removeCopyPrefix(name);
        name = name.replace("+", "");

        String copyName = "Copy of ".concat(name);
        boolean alreadyExists = existingNames.contains(copyName);
        int index = 2;
        while (alreadyExists) {
            copyName = "Copy".concat(String.valueOf(index)).concat(" of ").concat(name);
            alreadyExists = existingNames.contains(copyName);
            index++;
        }

        //TODO copy name mustn't contain + or ++. It's variant for C++, but in future need add more suitable solving.
        return copyName.endsWith("++") ? copyName.replace("++", "") : copyName;
    }

    /**
     * Gets project environments name which is creating from scratch.
     *
     * @param environments
     *         list of existing environments
     * @param projectName
     *         name of current project
     * @return name of new custom environment
     */
    public static String generateCustomEnvironmentName(@NotNull List<Environment> environments, @NotNull String projectName) {
        int counter = 1;
        String name = CUSTOM_ENV_PREFIX + counter + '-' + projectName;
        for (int i = 0; i < environments.size(); i++) {
            if (environments.get(i).getName().equals(name)) {
                counter++;
                name = CUSTOM_ENV_PREFIX + String.valueOf(counter) + '-' + projectName;
                i = 0;
            }
        }

        return name;
    }

}