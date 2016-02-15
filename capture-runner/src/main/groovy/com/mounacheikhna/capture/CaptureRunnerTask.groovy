package com.mounacheikhna.capture

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction

/**
 * Created by m.cheikhna on 14/02/2016.
 *
 * This class is temporarely here but it belongs to capture lib -> will be moved to it later
 * temp here just to test them -> TODO: move them to their own module
 */
public class CaptureRunnerTask extends DefaultTask implements CaptureSpec {

    private String appApkPath
    private String testApkPath
    private String outputPath
    private String serialNumber
    private Map<String, String> instrumentationArgs
    private String testClassName

    private String testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    private String testPackageName

    @TaskAction
    public void performTask() {
        validateInput()
//        - (maybe) clean old screenshots

//        - (later) Validate app apk by checking that all needed permission have been asked for
//        - install both app apk & test app apk
        installApks()
//        - grant the needed permissions to the app
        askPermissions()
//        - run screenshots tests with adb shell instruments
        runScreenshotsTests()
//        - pull screenshots imgs from device
        pullScreenshots()
//        - (later maybe) open a summary of the captured screenshots ? (something that resembles spoon instead)
//

    }

    void pullScreenshots() {
        def args = ["adb", "-s", "$serialNumber", "pull", "/sdcard/app_spoon-screenshots", "$outputPath"]
        println " pullScreenshots args : $args"
        getProject().tasks.create("pullScreenshots", Exec) {
            //not yet sure in which path does it get put but for now let's use spoon's
            commandLine args
        }.execute()
    }

    void runScreenshotsTests() {
        def args = ["adb", "-s", "$serialNumber", "shell", "am", "instrument", "--no-window-animation"]
        instrumentationArgs.each {
            key, value ->
                args.addAll("-e", key, value)
        }
        args.addAll("-e", "class", "$testClassName", "-w", "$testPackageName/$testInstrumentationRunner")
        println " runScreenshotsTests args : $args"
        getProject().tasks.create("runScreenshotsTests", Exec) {
            commandLine args
        }.execute()
    }

    void askPermissions() {
        //TODO: later
    }

    void installApks() {
        println "Installing app APK"
        def appArgs = ["adb", "-s", "$serialNumber", "install", "-r", "$appApkPath"]
        getProject().tasks.create("installApp", Exec) {
            commandLine appArgs
        }.execute()
        //TODO: find a way to get task result and print it
        println 'Installing tests app APK'
        def testAppArgs = ["adb", "-s", "$serialNumber", "install", "-r", "$testApkPath"]
        getProject().tasks.create("installTestApp", Exec) {
            commandLine testAppArgs
        }.execute()
    }

    private void validateInput() {
        if (!new File(appApkPath).exists()) {
            throw new IllegalArgumentException("You must provide a valid path for app apk.")
        }
        if (!new File(testApkPath).exists()) {
            throw new IllegalArgumentException("You must provide a valid path for test app apk.")
        }
        if (!outputPath?.trim()) {
            throw new IllegalArgumentException("You must provide a valid output directory.")
        } else {
            def outputDir = new File(outputPath)
            if (!outputDir.exists() || !outputDir.isDirectory()) {
                throw new IllegalArgumentException("You must provide an existing directory for the output.")
            }
        }
        //TODO: check if no serialNumber select the first one by default
    }

    @Override
    void appApkPath(String appApkPath) {
        this.appApkPath = appApkPath
    }

    @Override
    void testApkPath(String testApkPath) {
        this.testApkPath = testApkPath
    }

    @Override
    void outputPath(String outputPath) {
        this.outputPath = outputPath
    }

    @Override
    void instrumentationArgs(Map<String, String> args) {
        this.instrumentationArgs = args
    }

    @Override
    void testClassName(String testClassName) {
        this.testClassName = testClassName
    }

    @Override
    void serialNumber(String deviceSerialNumber) {
        this.serialNumber = deviceSerialNumber
    }

    @Override
    void testInstrumentationRunner(String testInstrumentationRunner) {
        this.testInstrumentationRunner = testInstrumentationRunner
    }

    @Override
    void testPackageName(String testPackageName) {
        this.testPackageName = testPackageName
    }
}
