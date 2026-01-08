package com.cericatto.skillpulse.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UiTextTest {

	// ==================== DynamicString Tests ====================

	@Test
	fun `DynamicString stores value correctly`() {
		val text = UiText.DynamicString("Hello World")

		assertThat(text.value).isEqualTo("Hello World")
	}

	@Test
	fun `DynamicString with empty string`() {
		val text = UiText.DynamicString("")

		assertThat(text.value).isEmpty()
	}

	@Test
	fun `DynamicString with special characters`() {
		val text = UiText.DynamicString("Hello! @#\$%^&*()")

		assertThat(text.value).isEqualTo("Hello! @#\$%^&*()")
	}

	@Test
	fun `DynamicString with unicode characters`() {
		val text = UiText.DynamicString("Olá Mundo 🌍")

		assertThat(text.value).isEqualTo("Olá Mundo 🌍")
	}

	@Test
	fun `DynamicString equality works correctly`() {
		val text1 = UiText.DynamicString("Same value")
		val text2 = UiText.DynamicString("Same value")

		assertThat(text1).isEqualTo(text2)
	}

	@Test
	fun `DynamicString inequality with different values`() {
		val text1 = UiText.DynamicString("Value 1")
		val text2 = UiText.DynamicString("Value 2")

		assertThat(text1).isNotEqualTo(text2)
	}

	@Test
	fun `DynamicString is UiText subtype`() {
		val text: UiText = UiText.DynamicString("Test")

		assertThat(text).isInstanceOf(UiText::class.java)
		assertThat(text).isInstanceOf(UiText.DynamicString::class.java)
	}

	// ==================== StringResource Tests ====================

	@Test
	fun `StringResource stores id correctly`() {
		val resourceId = 12345
		val text = UiText.StringResource(resourceId)

		assertThat(text.id).isEqualTo(resourceId)
	}

	@Test
	fun `StringResource with empty args`() {
		val text = UiText.StringResource(12345)

		assertThat(text.args).isEmpty()
	}

	@Test
	fun `StringResource with args`() {
		val args = arrayOf<Any>("arg1", 42, true)
		val text = UiText.StringResource(12345, args)

		assertThat(text.args).hasLength(3)
		assertThat(text.args[0]).isEqualTo("arg1")
		assertThat(text.args[1]).isEqualTo(42)
		assertThat(text.args[2]).isEqualTo(true)
	}

	@Test
	fun `StringResource is UiText subtype`() {
		val text: UiText = UiText.StringResource(12345)

		assertThat(text).isInstanceOf(UiText::class.java)
		assertThat(text).isInstanceOf(UiText.StringResource::class.java)
	}

	@Test
	fun `StringResource with single arg`() {
		val text = UiText.StringResource(12345, arrayOf("single"))

		assertThat(text.args).hasLength(1)
	}

	// ==================== Type Checking Tests ====================

	@Test
	fun `can distinguish between DynamicString and StringResource`() {
		val dynamic: UiText = UiText.DynamicString("Dynamic")
		val resource: UiText = UiText.StringResource(12345)

		assertThat(dynamic is UiText.DynamicString).isTrue()
		assertThat(dynamic is UiText.StringResource).isFalse()

		assertThat(resource is UiText.StringResource).isTrue()
		assertThat(resource is UiText.DynamicString).isFalse()
	}

	@Test
	fun `when expression works with UiText subtypes`() {
		val dynamic: UiText = UiText.DynamicString("Test")
		val resource: UiText = UiText.StringResource(12345)

		val dynamicResult = when (dynamic) {
			is UiText.DynamicString -> "dynamic"
			is UiText.StringResource -> "resource"
		}

		val resourceResult = when (resource) {
			is UiText.DynamicString -> "dynamic"
			is UiText.StringResource -> "resource"
		}

		assertThat(dynamicResult).isEqualTo("dynamic")
		assertThat(resourceResult).isEqualTo("resource")
	}

	// ==================== Edge Cases ====================

	@Test
	fun `DynamicString with very long string`() {
		val longString = "a".repeat(10000)
		val text = UiText.DynamicString(longString)

		assertThat(text.value).hasLength(10000)
	}

	@Test
	fun `DynamicString with multiline string`() {
		val multiline = """
			Line 1
			Line 2
			Line 3
		""".trimIndent()
		val text = UiText.DynamicString(multiline)

		assertThat(text.value).contains("Line 1")
		assertThat(text.value).contains("Line 2")
		assertThat(text.value).contains("Line 3")
	}

	@Test
	fun `StringResource with many args`() {
		val args = arrayOf<Any>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
		val text = UiText.StringResource(12345, args)

		assertThat(text.args).hasLength(10)
	}
}
