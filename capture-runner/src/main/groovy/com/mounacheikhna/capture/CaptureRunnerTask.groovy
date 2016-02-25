package com.mounacheikhna.capture

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

/**
 * Created by m.cheikhna on 14/02/2016.
 *
 */
public class CaptureRunnerTask extends DefaultTask implements CaptureSpec {

  private String taskPrefix = ""
  private String appApkPath
  private String testApkPath
  private String outputPath
  private String serialNumber
  private Map<String, String> instrumentationArgs
  private String testClassName
  private static String testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
  private String appPackageName
  private String testPackageName

  @TaskAction
  public void performTask() {
    //validateInput()
    //TODO(maybe): clean old screenshots
    //removeOldScreenshots()
    //TODO: choose device here and set serialNumber if not specified

    //TODO Validate app apk by checking that all needed permission have been asked for
    //installApks()
    askPermissions()
    /*runScreenshotsTests()
    pullScreenshots()*/

    //TODO open a summary of the captured screenshots ? (something that resembles spoon instead)
  }

  def removeOldScreenshots() {
    //from locale folder
    if(new File(outputPath) == null || !new File(outputPath).exists()) return
    new File(outputPath).listFiles().each {
      it.delete()
    }
    //from phone sdcard

  }

  void pullScreenshots() {
    def args = ["adb", "-s", "$serialNumber", "pull", "/sdcard/app_spoon-screenshots", "$outputPath"]
    println " pullScreenshots args : $args"
    getProject().tasks.create("${taskPrefix}PullScreenshots", Exec) {
      //not yet sure in which path does it get put but for now let's use spoon's
      commandLine args
    }.execute()
  }

  void runScreenshotsTests() {
    //TODO: run adb devices & check if empty or all are offline
    def args = ["adb", "-s", "$serialNumber", "shell", "am", "instrument", "--no-window-animation"]
    instrumentationArgs.each {
      key, value ->
        args.addAll("-e", key, value)
    }
    args.addAll("-e", "class", "$testClassName", "-w",
            "$testPackageName/$testInstrumentationRunner")
    println " runScreenshotsTests args : $args"
    getProject().tasks.create("${taskPrefix}RunScreenshotsTests", Exec) {
      commandLine args
    }.execute()
  }

  void askPermissions() {
    String sdkVersion = getStringByCommand("adb -s $serialNumber shell getprop ro.build.version.sdk");
    def apiLevel = Integer.parseInt(sdkVersion)
    if(apiLevel < 23) return

    def grantWriteStorage = ["adb", "-s", "$serialNumber", "shell", "pm", "grant", "$appPackageName", "android.permission.WRITE_EXTERNAL_STORAGE"]
    getProject().tasks.create("${taskPrefix}GrantWriteStorage", Exec) {
        commandLine grantWriteStorage
    }.execute()

    def grantReadStorage = ["adb", "-s", "$serialNumber", "shell", "pm", "grant", "$appPackageName", "android.permission.READ_EXTERNAL_STORAGE"]
    getProject().tasks.create("${taskPrefix}GrantReadStorage", Exec) {
      commandLine grantReadStorage
    }.execute()

  }

  public static String getStringByCommand(String command) throws IOException {
    Process process = Runtime.getRuntime().exec(command);
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    StringBuilder result = new StringBuilder();
    try {
      int value = 0;
      while ((value = br.read()) != -1) {
        char c = (char) value;
        result.append(c);
      }
    } finally {
      br.close();
    }
    process.waitFor();
    return result.toString().trim();
  }

  void installApks() {
    println "Installing app APK"
    def appArgs = ["adb", "-s", "$serialNumber", "install", "-r", "$appApkPath"]
    getProject().tasks.create("${taskPrefix}InstallApp", Exec) {
      commandLine appArgs
    }.execute()
    //TODO: find a way to get task result and print it
    println 'Installing tests app APK'
    def testAppArgs = ["adb", "-s", "$serialNumber", "install", "-r", "$testApkPath"]
    getProject().tasks.create("${taskPrefix}InstallTestApp", Exec) {
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
  void appPackageName(String appPackageName) {
    this.appPackageName = appPackageName
  }

  @Override
  void testPackageName(String testPackageName) {
    this.testPackageName = testPackageName
  }

  @Override
  void taskPrefix(String taskPrefix) {
    this.taskPrefix = taskPrefix
  }
}
