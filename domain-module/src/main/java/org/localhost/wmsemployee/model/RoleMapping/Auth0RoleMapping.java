package org.localhost.wmsemployee.model.RoleMapping;

public class Auth0RoleMapping {

    public static String getRoleId(int roleId) {
        switch (roleId) {
            case 1:
                return "rol_wCwM9eBOsCgW8lkl";
            case 2:
                return "rol_WMcuv2cr35rdgqO2";
            case 3:
                return "rol_4fDK1THRxP1vDiD5";
            case 4:
                return "rol_pS5juyD9muG5klve";
            default:
                return "UNKNOWN";
        }
    }
}
