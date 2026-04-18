package com.example.engine.shader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PreferredTextureResolverTest {

	@Test
	fun resolve_cachesOriginalFallbackPath_afterMissingWebpProbe() {
		val resolver = PreferredTextureResolver()
		val callCounts = linkedMapOf<String, Int>()
		val loader: (String) -> ByteArray? = { path ->
			callCounts[path] = (callCounts[path] ?: 0) + 1
			when (path) {
				"backgrounds/city.png" -> byteArrayOf(1, 2, 3)
				else -> null
			}
		}

		val first = resolver.resolve("backgrounds/city.png", loader)
		val second = resolver.resolve("backgrounds/city.png", loader)

		assertNotNull(first)
		assertNotNull(second)
		assertEquals(1, callCounts["backgrounds/city.webp"])
		assertEquals(2, callCounts["backgrounds/city.png"])
	}

	@Test
	fun resolve_prefersWebp_andReusesCachedResolvedPath() {
		val resolver = PreferredTextureResolver()
		val callCounts = linkedMapOf<String, Int>()
		val loader: (String) -> ByteArray? = { path ->
			callCounts[path] = (callCounts[path] ?: 0) + 1
			when (path) {
				"backgrounds/city.webp" -> byteArrayOf(9, 8, 7)
				"backgrounds/city.png" -> byteArrayOf(1, 2, 3)
				else -> null
			}
		}

		val first = resolver.resolve("backgrounds/city.png", loader)
		val second = resolver.resolve("backgrounds/city.png", loader)

		assertEquals("backgrounds/city.webp", first?.path)
		assertEquals("backgrounds/city.webp", second?.path)
		assertEquals(2, callCounts["backgrounds/city.webp"])
		assertEquals(0, callCounts["backgrounds/city.png"] ?: 0)
	}
}
