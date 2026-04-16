
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TruffulaPrinterTest {
    /**
     * Checks if the current operating system is Windows.
     *
     * This method reads the "os.name" system property and checks whether it
     * contains the substring "win", which indicates a Windows-based OS.
     * 
     * You do not need to modify this method.
     *
     * @return true if the OS is Windows, false otherwise
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    /**
     * Creates a hidden file in the specified parent folder.
     * 
     * The filename MUST start with a dot (.).
     *
     * On Unix-like systems, files prefixed with a dot (.) are treated as hidden.
     * On Windows, this method also sets the DOS "hidden" file attribute.
     * 
     * You do not need to modify this method, but you SHOULD use it when creating hidden files
     * for your tests. This will make sure that your tests work on both Windows and UNIX-like systems.
     *
     * @param parentFolder the directory in which to create the hidden file
     * @param filename the name of the hidden file; must start with a dot (.)
     * @return a File object representing the created hidden file
     * @throws IOException if an I/O error occurs during file creation or attribute setting
     * @throws IllegalArgumentException if the filename does not start with a dot (.)
     */
    private static File createHiddenFile(File parentFolder, String filename) throws IOException {
        if(!filename.startsWith(".")) {
            throw new IllegalArgumentException("Hidden files/folders must start with a '.'");
        }
        File hidden = new File(parentFolder, filename);
        hidden.createNewFile();
        if(isWindows()) {
            Path path = Paths.get(hidden.toURI());
            Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
        }
        return hidden;
    }

    @Test
    public void testPrintTree_ExactOutput_WithCustomPrintStream(@TempDir File tempDir) throws IOException {
        // Build the example directory structure:
        // myFolder/
        //    .hidden.txt
        //    Apple.txt
        //    banana.txt
        //    Documents/
        //       images/
        //          Cat.png
        //          cat.png
        //          Dog.png
        //       notes.txt
        //       README.md
        //    zebra.txt

        // Create "myFolder"
        File myFolder = new File(tempDir, "myFolder");
        assertTrue(myFolder.mkdir(), "myFolder should be created");

        // Create visible files in myFolder
        File apple = new File(myFolder, "Apple.txt");
        File banana = new File(myFolder, "banana.txt");
        File zebra = new File(myFolder, "zebra.txt");
        apple.createNewFile();
        banana.createNewFile();
        zebra.createNewFile();

        // Create a hidden file in myFolder
        createHiddenFile(myFolder, ".hidden.txt");

        // Create subdirectory "Documents" in myFolder
        File documents = new File(myFolder, "Documents");
        assertTrue(documents.mkdir(), "Documents directory should be created");

        // Create files in Documents
        File readme = new File(documents, "README.md");
        File notes = new File(documents, "notes.txt");
        readme.createNewFile();
        notes.createNewFile();

        // Create subdirectory "images" in Documents
        File images = new File(documents, "images");
        assertTrue(images.mkdir(), "images directory should be created");

        // Create files in images
        File cat = new File(images, "cat.png");
        File dog = new File(images, "Dog.png");
        cat.createNewFile();
        dog.createNewFile();

        // Set up TruffulaOptions with showHidden = false and useColor = true
        TruffulaOptions options = new TruffulaOptions(myFolder, false, true);

        // Capture output using a custom PrintStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);

        // Instantiate TruffulaPrinter with custom PrintStream
        TruffulaPrinter printer = new TruffulaPrinter(options, printStream);

        // Call printTree (output goes to printStream)
        printer.printTree();

        // Retrieve printed output
        String output = baos.toString();
        String nl = System.lineSeparator();

        // Build expected output with exact colors and indentation
        ConsoleColor reset = ConsoleColor.RESET;
        ConsoleColor white = ConsoleColor.WHITE;
        ConsoleColor purple = ConsoleColor.PURPLE;
        ConsoleColor yellow = ConsoleColor.YELLOW;

        StringBuilder expected = new StringBuilder();
        expected.append(white).append("myFolder/").append(nl).append(reset);
        expected.append(purple).append("   Apple.txt").append(nl).append(reset);
        expected.append(purple).append("   banana.txt").append(nl).append(reset);
        expected.append(purple).append("   Documents/").append(nl).append(reset);
        expected.append(yellow).append("      images/").append(nl).append(reset);
        expected.append(white).append("         cat.png").append(nl).append(reset);
        expected.append(white).append("         Dog.png").append(nl).append(reset);
        expected.append(yellow).append("      notes.txt").append(nl).append(reset);
        expected.append(yellow).append("      README.md").append(nl).append(reset);
        expected.append(purple).append("   zebra.txt").append(nl).append(reset);

        // Assert that the output matches the expected output exactly
        assertEquals(expected.toString(), output);
    }

    @Test
    public void testPrintTree_SimpleDirectory_NoColorNoHidden(@TempDir File tempDir) throws IOException {
        // Create a simple directory with two files and one subdirectory
        File root = new File(tempDir, "root");
        assertTrue(root.mkdir(), "root should be created");

        File fileA = new File(root, "A.txt");
        File fileB = new File(root, "B.txt");
        fileA.createNewFile();
        fileB.createNewFile();

        File subDir = new File(root, "sub");
        assertTrue(subDir.mkdir(), "sub directory should be created");

        // TruffulaOptions: showHidden = false, useColor = false
        TruffulaOptions options = new TruffulaOptions(root, false, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        TruffulaPrinter printer = new TruffulaPrinter(options, printStream);
        printer.printTree();

        String output = baos.toString();
        String nl = System.lineSeparator();

        // Build expected output (no color, no order enforced)
        StringBuilder expected = new StringBuilder();
        expected.append("root/").append(nl);
        expected.append("   A.txt").append(nl);
        expected.append("   B.txt").append(nl);
        expected.append("   sub/").append(nl);

        String cleanOutput = output.replaceAll("\\u001B\\[[;\\d]*m", "");
        assertEquals(expected.toString(), cleanOutput);
    }

    @Test
    public void testPrintTree_HiddenFilesRespected_Simple(@TempDir File tempDir) throws IOException {
        // Create directory structure: myFolder/ with one visible and one hidden file
        File myFolder = new File(tempDir, "myFolder");
        assertTrue(myFolder.mkdir(), "myFolder should be created");

        File visible = new File(myFolder, "visible.txt");
        visible.createNewFile();
        createHiddenFile(myFolder, ".hidden.txt");

        String nl = System.lineSeparator();

        // Test with showHidden = false
        TruffulaOptions optionsNoHidden = new TruffulaOptions(myFolder, false, false);
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        PrintStream ps1 = new PrintStream(baos1);
        TruffulaPrinter printer1 = new TruffulaPrinter(optionsNoHidden, ps1);
        printer1.printTree();
        String output1 = baos1.toString();

        StringBuilder expected1 = new StringBuilder();
        expected1.append("myFolder/").append(nl);
        expected1.append("   visible.txt").append(nl);

        //Works if this hidden files are not showns
        String cleanOutput1 = output1.replaceAll("\\u001B\\[[;\\d]*m", "");
        assertEquals(expected1.toString(), cleanOutput1);

        // Test with showHidden = true
        TruffulaOptions optionsWithHidden = new TruffulaOptions(myFolder, true, false);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream ps2 = new PrintStream(baos2);
        TruffulaPrinter printer2 = new TruffulaPrinter(optionsWithHidden, ps2);
        printer2.printTree();
        String output2 = baos2.toString();

        StringBuilder expected2 = new StringBuilder();
        expected2.append("myFolder/").append(nl);
        expected2.append("   .hidden.txt").append(nl);
        expected2.append("   visible.txt").append(nl);

        //Works if this hidden files are showns
        String cleanOutput2 = output2.replaceAll("\\u001B\\[[;\\d]*m", "");
        assertEquals(expected2.toString(), cleanOutput2);
        
    }

    @Test
    public void testPrintTree_HiddenFilesRespected_LargerTree(@TempDir File tempDir) throws IOException {
        // Create directory structure: myFolder/ with one visible and one hidden file
        File myFolder = new File(tempDir, "myFolder");
        assertTrue(myFolder.mkdir(), "myFolder should be created");

        // Create visible files in myFolder
        File apple = new File(myFolder, "Apple.txt");
        File banana = new File(myFolder, "banana.txt");
        File zebra = new File(myFolder, "zebra.txt");
        apple.createNewFile();
        banana.createNewFile();
        zebra.createNewFile();

        // Create a hidden file in myFolder
        createHiddenFile(myFolder, ".hidden.txt");

        // Create subdirectory "Documents" in myFolder
        File documents = new File(myFolder, "Documents");
        assertTrue(documents.mkdir(), "Documents directory should be created");

        // Create files in Documents
        File readme = new File(documents, "README.md");
        File notes = new File(documents, "notes.txt");
        readme.createNewFile();
        notes.createNewFile();

        // Create subdirectory "images" in Documents
        File images = new File(documents, "images");
        assertTrue(images.mkdir(), "images directory should be created");

        // Create files in images
        File cat = new File(images, "cat.png");
        File dog = new File(images, "Dog.png");
        cat.createNewFile();
        dog.createNewFile();

        String nl = System.lineSeparator();

        // Test with showHidden = false
        TruffulaOptions optionsNoHidden = new TruffulaOptions(myFolder, false, false);
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        PrintStream ps1 = new PrintStream(baos1);
        TruffulaPrinter printer1 = new TruffulaPrinter(optionsNoHidden, ps1);
        printer1.printTree();
        String output1 = baos1.toString();

        // EX:
        // StringBuilder expected1 = new StringBuilder();
        // expected1.append("myFolder/").append(nl);
        // expected1.append("   visible.txt").append(nl);
        StringBuilder expected1 = new StringBuilder();
        expected1.append("myFolder/").append(nl);
        expected1.append("   Apple.txt").append(nl);
        expected1.append("   banana.txt").append(nl);
        expected1.append("   Documents/").append(nl);
        expected1.append("      images/").append(nl);
        expected1.append("         cat.png").append(nl);
        expected1.append("         Dog.png").append(nl);
        expected1.append("      notes.txt").append(nl);
        expected1.append("      README.md").append(nl);
        expected1.append("   zebra.txt").append(nl);

        //Works if this hidden files are not showns
        String cleanOutput1 = output1.replaceAll("\\u001B\\[[;\\d]*m", "");
        assertEquals(expected1.toString(), cleanOutput1);

        // Test with showHidden = true
        TruffulaOptions optionsWithHidden = new TruffulaOptions(myFolder, true, false);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream ps2 = new PrintStream(baos2);
        TruffulaPrinter printer2 = new TruffulaPrinter(optionsWithHidden, ps2);
        printer2.printTree();
        String output2 = baos2.toString();

        // EX:
        // StringBuilder expected2 = new StringBuilder();
        // expected2.append("myFolder/").append(nl);
        // expected2.append("   .hidden.txt").append(nl);
        // expected2.append("   visible.txt").append(nl);
        StringBuilder expected2 = new StringBuilder();
        expected2.append("myFolder/").append(nl);
        expected2.append("   .hidden.txt").append(nl);
        expected2.append("   Apple.txt").append(nl);
        expected2.append("   banana.txt").append(nl);
        expected2.append("   Documents/").append(nl);
        expected2.append("      images/").append(nl);
        expected2.append("         cat.png").append(nl);
        expected2.append("         Dog.png").append(nl);
        expected2.append("      notes.txt").append(nl);
        expected2.append("      README.md").append(nl);
        expected2.append("   zebra.txt").append(nl);

        //Works if this hidden files are showns
        String cleanOutput2 = output2.replaceAll("\\u001B\\[[;\\d]*m", "");
        assertEquals(expected2.toString(), cleanOutput2);
        
    }
    
}
