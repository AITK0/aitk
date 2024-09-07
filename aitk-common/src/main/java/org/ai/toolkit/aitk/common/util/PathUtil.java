package org.ai.toolkit.aitk.common.util;

import java.io.File;

public class PathUtil {

    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    public static String getParentUserDir() {
        return new File(getUserDir()).getParent();
    }
}
