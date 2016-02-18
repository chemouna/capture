package com.mounacheikhna.capture
/**
 * Created by m.cheikhna on 14/02/2016.
 *
 * temp here just to test them -> TODO: move them to their own module
 */
interface CaptureSpec {

    void appApkPath(String appApkPath)
    void testApkPath(String testApkPath)
    void outputPath(String outputPath)
    void instrumentationArgs(Map<String, String> args)
    void testClassName(String testClassName)
    void serialNumber(String serialNumber)
    void testInstrumentationRunner(String testInstrumentationRunner)
    void appPackageName(String testPackageName)
    void testPackageName(String testPackageName)
    void taskPrefix(String taskPrefix)

}