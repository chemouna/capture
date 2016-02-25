package com.mounacheikhna.capture

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

/**
 * Created by cheikhnamouna on 2/25/16.
 */
class CaptureRunnerTest {

  public static final String FIXTURE_WORKING_DIR = new File("src/test/fixtures/app")
  private Project project

  @Before
  public void setUp() throws Exception {
    project = ProjectBuilder.builder().withProjectDir(new File(FIXTURE_WORKING_DIR)).build()
    project.apply plugin: 'com.android.application'
    project.android {
      compileSdkVersion 23
      buildToolsVersion '23.0.1'

      defaultConfig {
        versionCode 1
        versionName '1.0'
        minSdkVersion 23
        targetSdkVersion 23
      }
    }
    project.evaluate()
  }

  @Test
  public void runsWhenAllInputProvided() {
    Task captureTask = project.tasks.create("captureTask", CaptureRunnerTask.class)
    captureTask.taskPrefix("test")
    captureTask.appApkPath("build/outputs/apk/dummy.apk")
    captureTask.testApkPath("build/outputs/apk/dummy-androidTest.apk")
    captureTask.appPackageName("com.mounacheikhna.capture")
    captureTask.testPackageName("com.mounacheikhna.capture.test")
    captureTask.serialNumber("192.168.56.101:5555")
    captureTask.testClassName("com.mounacheikhna.capture.TestClass")
    captureTask.execute()
  }

}
