package parser.services

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class ReadDirectoryServiceTest {

    private val directoryService = ReadDirectoryService()

    @Test
    fun readSingleFile() {
        val unparsedFile = directoryService.readFile("testDirectory/testClass1.java")
        assertTrue(unparsedFile.getLine(2).contains("testClass1"))
        assertSame(unparsedFile.numberOfLines(), 4)
    }

    @Test
    fun readDirectory() {
        val unparsedDirectory = directoryService.readDirectory("testDirectory")
        assertEquals(unparsedDirectory.getAllFiles().size, 3)
        assertEquals(unparsedDirectory.getMap().size, 3)
        assertTrue(unparsedDirectory.getFile("testDirectory/testClass1.java").getLine(2).contains("testClass1"))

        assertTrue(unparsedDirectory.getFile("testDirectory/testSubDirectory/testSubClass.java").getLine(2).contains("testSubClass"))

    }

}
