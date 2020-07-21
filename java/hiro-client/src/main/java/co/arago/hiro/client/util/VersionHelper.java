package co.arago.hiro.client.util;

import org.apache.commons.io.IOUtils;

/**
 *
 */
public final class VersionHelper {
    private static final String VERSION;

    static {
        String version;

        try {
            version = IOUtils.toString(VersionHelper.class.getClassLoader().getResourceAsStream("_hiro.version"))
                    .replaceAll("\n", "");
            if (version == null || version.isEmpty()) {
                throw new IllegalArgumentException("hiro version is empty");
            }
        } catch (Exception e) {
            version = "unit-test";
        }

        VERSION = version;
    }

    public static String version() {
        return VERSION;
    }

    private VersionHelper() {
    }
}
