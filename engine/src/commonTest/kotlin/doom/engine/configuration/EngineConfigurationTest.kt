package doom.engine.configuration

import doom.engine.DoomStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

class EngineConfigurationTest {
    @Test
    fun typedValuesKeepTheLegacyFormatAndReadValidDecimalHexAndQuotedValues() {
        var volume = -1
        var key = -1
        var text = ""
        val files = EngineConfigurationMemoryStorage()
        files.content = " volume 0xf\r\nkey -1\nmessage \"hello world\"\n".encodeToByteArray()
        val config = EngineConfiguration(listOf(
            IntegerSetting("volume", { volume }, { volume = it }, 8, 0..15),
            IntegerSetting("key", { key }, { key = it }, 4, -1..255),
            StringSetting("message", { text }, { text = it }, "default"),
        ), files)

        config.load("custom.cfg")
        assertEquals(15, volume)
        assertEquals(-1, key)
        assertEquals("hello world", text)
        config.save()
        assertEquals("custom.cfg", files.lastRead)
        assertEquals("custom.cfg", files.lastWrite)
        assertEquals("volume\t\t15\nkey\t\t-1\nmessage\t\t\"hello world\"\n", files.content!!.decodeToString())
    }

    @Test
    fun invalidValuesAndWrongTypesCannotReplaceDefaults() {
        var number = 99
        var text = "changed"
        val files = EngineConfigurationMemoryStorage()
        files.content = """
            number 2147483648
            number 11
            number -1
            number nope
            number "5"
            text 10
            text 0xff
            missing "ignored"
            number
        """.trimIndent().encodeToByteArray()
        val config = EngineConfiguration(listOf(
            IntegerSetting("number", { number }, { number = it }, 5, 0..10),
            StringSetting("text", { text }, { text = it }, "default"),
        ), files)

        config.load("default.cfg")
        assertEquals(5, number)
        assertEquals("default", text)
        number = 9
        text = "modified"
        files.content = null
        config.load("another.cfg")
        assertEquals(5, number)
        assertEquals("default", text)
    }

    @Test
    fun quoteCompatibilityAndRepeatedEntriesArePreserved() {
        var text = "default"
        val files = EngineConfigurationMemoryStorage()
        val config = EngineConfiguration(listOf(StringSetting("text", { text }, { text = it }, "default")), files)
        for ((source, expected) in listOf(
            "text \"\n" to "",
            "text \"abc\n" to "ab",
            "text \"a\"\ntext \"b\"\n" to "b",
        )) {
            files.content = source.encodeToByteArray()
            config.load("default.cfg")
            assertEquals(expected, text)
        }
    }

    @Test
    fun numericDefaultsDoNotUseMagnitudeAsAStringTypeTag() {
        var value = 0
        val files = EngineConfigurationMemoryStorage()
        val config = EngineConfiguration(listOf(IntegerSetting("large", { value }, { value = it }, Int.MAX_VALUE)), files)
        config.load("default.cfg")
        config.save()
        assertEquals(Int.MAX_VALUE, value)
        assertEquals("large\t\t2147483647\n", files.content!!.decodeToString())
    }

    @Test
    fun storageFailuresPropagateWithoutChangingTheirIdentity() {
        val failure = IllegalStateException("storage unavailable")
        var value = 77
        val files = object : DoomStorage {
            override fun read(name: String): ByteArray? = throw failure
            override fun write(name: String, data: ByteArray) = throw failure
        }
        val config = EngineConfiguration(listOf(IntegerSetting("value", { value }, { value = it }, 5)), files)
        assertSame(failure, assertFails { config.load("default.cfg") })
        assertEquals(5, value, "Defaults are applied before reading the persisted file")
        assertSame(failure, assertFails { config.save() })
    }

}
