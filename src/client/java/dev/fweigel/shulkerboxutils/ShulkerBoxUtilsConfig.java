package dev.fweigel.shulkerboxutils;

public class ShulkerBoxUtilsConfig {
    private static boolean badgeEnabled = true;
    private static boolean worldIconEnabled = true;
    private static boolean uniformOnly = false;
    private static boolean fillIndicatorEnabled = true;
    private static boolean contentsPreviewEnabled = true;

    public static boolean isBadgeEnabled()            { return badgeEnabled; }
    public static boolean isWorldIconEnabled()         { return worldIconEnabled; }
    public static boolean isUniformOnly()              { return uniformOnly; }
    public static boolean isFillIndicatorEnabled()     { return fillIndicatorEnabled; }
    public static boolean isContentsPreviewEnabled()   { return contentsPreviewEnabled; }

    public static void setBadgeEnabled(boolean v)           { badgeEnabled = v; }
    public static void setWorldIconEnabled(boolean v)       { worldIconEnabled = v; }
    public static void setUniformOnly(boolean v)            { uniformOnly = v; }
    public static void setFillIndicatorEnabled(boolean v)   { fillIndicatorEnabled = v; }
    public static void setContentsPreviewEnabled(boolean v) { contentsPreviewEnabled = v; }

    public static void toggleBadge()            { badgeEnabled = !badgeEnabled; }
    public static void toggleWorldIcon()        { worldIconEnabled = !worldIconEnabled; }
    public static void toggleUniformOnly()      { uniformOnly = !uniformOnly; }
    public static void toggleFillIndicator()    { fillIndicatorEnabled = !fillIndicatorEnabled; }
    public static void toggleContentsPreview()  { contentsPreviewEnabled = !contentsPreviewEnabled; }

    public static void reset() {
        badgeEnabled = true;
        worldIconEnabled = true;
        uniformOnly = false;
        fillIndicatorEnabled = true;
        contentsPreviewEnabled = true;
    }
}
