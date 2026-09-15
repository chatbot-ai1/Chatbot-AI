package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.supabase.SupabaseMessageDto
import com.example.data.remote.supabase.SupabaseSessionDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("Social AI", appName)
  }

  @Test
  fun `test supabase session dto serialization`() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(SupabaseSessionDto::class.java)

    val session = SupabaseSessionDto(
      id = 101L,
      title = "Test Session",
      createdAt = 1000L,
      updatedAt = 2000L,
      model = "openrouter/free"
    )

    val json = adapter.toJson(session)
    val parsed = adapter.fromJson(json)

    assertNotNull(parsed)
    assertEquals(101L, parsed?.id)
    assertEquals("Test Session", parsed?.title)
    assertEquals("openrouter/free", parsed?.model)
  }

  @Test
  fun `test supabase message dto serialization`() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(SupabaseMessageDto::class.java)

    val message = SupabaseMessageDto(
      id = 202L,
      sessionId = 101L,
      role = "user",
      content = "Hello Supabase!",
      timestamp = 3000L,
      isError = false
    )

    val json = adapter.toJson(message)
    val parsed = adapter.fromJson(json)

    assertNotNull(parsed)
    assertEquals(202L, parsed?.id)
    assertEquals(101L, parsed?.sessionId)
    assertEquals("Hello Supabase!", parsed?.content)
  }
}
