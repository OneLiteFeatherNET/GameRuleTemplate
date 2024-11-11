package net.onelitefeather.gameruletemplate.utils;

import org.incendo.cloud.permission.Permission;

public final class PermissionsList {

    private PermissionsList() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static Permission CREATE_TEMPLATE = Permission.of("gameruletemplate.create");
    public static Permission APPLY_TEMPLATE = Permission.of("gameruletemplate.apply");
    public static Permission DELETE_TEMPLATE = Permission.of("gameruletemplate.delete");
    public static Permission OVERRIDE = Permission.of("gameruletemplate.override");
}
