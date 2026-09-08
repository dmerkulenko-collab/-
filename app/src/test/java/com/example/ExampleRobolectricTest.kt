package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crash.CrashHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Диспетчер задач", appName)
  }

  @Test
  fun `test crash handler saving and clipboard utilities`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testReport = "=== TEST CRASH REPORT ==="
    CrashHandler.saveCrashReport(context, testReport)

    val retrieved = CrashHandler.getLastCrashReport(context)
    assertEquals(testReport, retrieved)
    assertTrue(CrashHandler.isCrashPendingReview(context))

    val copied = CrashHandler.copyToClipboard(context, testReport)
    assertTrue(copied)

    val diagnosticReport = CrashHandler.getOrGenerateReport(context)
    assertNotNull(diagnosticReport)
    assertTrue(diagnosticReport.contains("TEST CRASH REPORT"))

    CrashHandler.clearCrashReport(context)
  }
}
