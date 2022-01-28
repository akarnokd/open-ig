/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The upgrades for the building type.
 * @author akarnokd, 2010.01.07.
 */
public class Upgrade {
    /** The display upgrade description. */
    public String description;
    /** The resources associated with this upgrade. */
    public final Map<String, Resource> resources = new HashMap<>();
    /**
     * Retrieves the amount of the given resource type.
     * @param resourceType the resource type
     * @return the amount
     */
    public double get(String resourceType) {
        return resources.get(resourceType).amount;
    }
    /**
     * Retrieves the resource type definition.
     * @param resourceType the resource type
     * @return the resource definition
     */
    public Resource getType(String resourceType) {
        return resources.get(resourceType);
    }
}
