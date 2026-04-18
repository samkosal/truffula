import java.io.PrintStream;
import java.util.List;
import java.io.File;

/**
 * TruffulaPrinter is responsible for printing a directory tree structure
 * with optional colored output. It supports sorting files and directories
 * in a case-insensitive manner and cycling through colors for visual clarity.
 */
public class TruffulaPrinter {  
  /**
   * Configuration options that determine how the tree is printed.
   */
  private TruffulaOptions options;
  
  /**
   * The sequence of colors to use when printing the tree.
   */
  private List<ConsoleColor> colorSequence;
  
  /**
   * The output printer for displaying the tree.
   */
  private ColorPrinter out;

  /**
   * Default color sequence used when no custom colors are provided.
   */
  private static final List<ConsoleColor> DEFAULT_COLOR_SEQUENCE = List.of(
      ConsoleColor.WHITE, ConsoleColor.PURPLE, ConsoleColor.YELLOW
  );

  /**
   * Constructs a TruffulaPrinter with the given options, using the default
   * output stream and the default color sequence.
   *
   * @param options the configuration options for printing the tree
   */
  public TruffulaPrinter(TruffulaOptions options) {
    this(options, System.out, DEFAULT_COLOR_SEQUENCE);
  }

  /**
   * Constructs a TruffulaPrinter with the given options and color sequence,
   * using the default output stream.
   *
   * @param options the configuration options for printing the tree
   * @param colorSequence the sequence of colors to use when printing
   */
  public TruffulaPrinter(TruffulaOptions options, List<ConsoleColor> colorSequence) {
    this(options, System.out, colorSequence);
  }

  /**
   * Constructs a TruffulaPrinter with the given options and output stream,
   * using the default color sequence.
   *
   * @param options the configuration options for printing the tree
   * @param outStream the output stream to print to
   */
  public TruffulaPrinter(TruffulaOptions options, PrintStream outStream) {
    this(options, outStream, DEFAULT_COLOR_SEQUENCE);
  }

  /**
   * Constructs a TruffulaPrinter with the given options, output stream, and color sequence.
   *
   * @param options the configuration options for printing the tree
   * @param outStream the output stream to print to
   * @param colorSequence the sequence of colors to use when printing
   */
  public TruffulaPrinter(TruffulaOptions options, PrintStream outStream, List<ConsoleColor> colorSequence) {
    this.options = options;
    this.colorSequence = colorSequence;
    out = new ColorPrinter(outStream);
  }

  /**
   * WAVE 4: Prints a tree representing the directory structure, with directories and files
   * sorted in a case-insensitive manner. The tree is displayed with 3 spaces of
   * indentation for each directory level.
   * 
   * WAVE 5: If hidden files are not to be shown, then no hidden files/folders will be shown.
   *
   * WAVE 6: If color is enabled, the output cycles through colors at each directory level
   * to visually differentiate them. If color is disabled, all output is displayed in white.
   *
   * WAVE 7: The sorting is case-insensitive. If two files have identical case-insensitive names,
   * they are sorted lexicographically (Cat.png before cat.png).
   *
   * Example Output:
   *
   * myFolder/
   *    Apple.txt
   *    banana.txt
   *    Documents/
   *       images/
   *          Cat.png
   *          cat.png
   *          Dog.png
   *       notes.txt
   *       README.md
   *    zebra.txt
   */
  public void printTree() {
    printHelper(options.getRoot(), 0);

    // TESTING OUT: .getName(), .isDirectory, .listFiles(), java.io.File
    // System.out.println(options.getRoot());
    // System.out.println(options.getRoot().isDirectory());
    // java.io.File[] files = options.getRoot().listFiles();
    // if (files != null) {
    //     for (java.io.File file : files) {
    //         System.out.println(file); // prints the File object's path
    //     }
    // }
  }

  private void printHelper(File root, int level) {
    // Start: java src/App.java -nc -h src
    if (root.isHidden() && !options.isShowHidden()) {
      return;
    }
    if (level % 3 == 0) {
      out.setCurrentColor(ConsoleColor.WHITE);
    }
    if (level % 3 == 1) {
      out.setCurrentColor(ConsoleColor.PURPLE);
    }
    if (level % 3 == 2) {
      out.setCurrentColor(ConsoleColor.YELLOW);
    }
    
    
    
    String indent = "   ";
    //Grabs the last part of the path. for Ex: new File("C:/foo/bar.txt"), getName() returns "bar.txt" 
    String name = root.getName();

   //.isDirectory if and only if the file exists and is a directory.
   // basically checks if root is a 

   //if root is a directory that exist
    if (root.isDirectory()) {
      // print out the root directory in this format:
      out.println(indent.repeat(level) + name + "/");
      //.listFiles() returns an array of files inside root, only works on directory, hence why we checked it in the first place.
      File[] files = root.listFiles();
      
      //create a new AlphabeticalFileSorter class
      AlphabeticalFileSorter sorter = new AlphabeticalFileSorter();
      
      //if the root directory is NOT empty
      if (files != null) {
        File[] filesSorted = sorter.sort(files);
        //start looping over each file and recursion
        // for (File file : files) {
        for (File file : filesSorted) {
          printHelper(file, level + 1);
        }
      }
    //if root is NOT a directory, meaning this is just a normal, then just print it out with the correct indentation
    } else {
      out.println(indent.repeat(level) + name);
    }
  }

}
