# Truffula Notes
As part of Wave 0, please fill out notes for each of the below files. They are in the order I recommend you go through them. A few bullet points for each file is enough. You don't need to have a perfect understanding of everything, but you should work to gain an idea of how the project is structured and what you'll need to implement. Note that there are programming techniques used here that we have not covered in class! You will need to do some light research around things like enums and and `java.io.File`.

PLEASE MAKE FREQUENT COMMITS AS YOU FILL OUT THIS FILE.

## App.java
- we have to create a TruffulaOptions object using ARGS:
- it will prop look something like this:
    - TruffulaOptions options = TruffulaOptions.fromArgs(args);
- args will hold user preference like:
    - whether to show hidden files
    - user color,
    - and the root directory path.

- then we create another object, a new TruffulaPrinter object this time that takes two parameters:
    - System.out
    - the the TruffaleOptions that we just created.
- it should look like: TruffulaPrinter printer = new TruffulaPrinter(System.out, options);

- then, call the print on the TruffulaPrinter we just made: printer.printTree();

## ConsoleColor.java
- very interesting, so the constructors takes in a string code, but the param in this case is the enumeration of different colors
like BLACK("\033[0;30m") . 
- so when you make an object ConsoleColor, you do this:
    - ConsoleColor color = ConsoleColor.BLUE; instead of:
    - ConsoleColor color = ConsoleColor("hi"); this will not work because the enum reference types decleration only allows to use the existing enum constant.
- there are two methods in this file that do the same shit, no fucking idea why. except one has override on it, overrides what?


## ColorPrinter.java / ColorPrinterTest.java
- this file is a utility for printing colored text to a PrintStream.
- there is example of how to use that can be very useful.
- two constructor, one takes two params: (PrintStream printStream, ConsoleColor color), the other takes only one param (PrintStream printStream):
    - printstream is a java class for outputting data like test to places such as console, files, or other output streams. System,out is a common example of printstream.
    - currentColor, sets a initial Consolecolor.
- different methods:
    - getCurrentColor returns the currentColor field for the printer.
    - setCurretnColor changes the currenColor field to a different value.
    - println has two methods, it prints the given message followed by a newline in the current color.
        - for ex: when you println(string message) -> println(String message, boolean reset) -> print(String message, boolean reset).


## TruffulaOptions.java / TruffulaOptionsTest.java
- this file defines a class that stores configuration of the how the directory tree-printing program should behave.
- the constructors takes in (File root, boolean showHidden, boolean useColor).
    - File root: is the directory to start printing from
    - Whether to show hidden files.
    - Whether to use colored output.
- there are three methods that return each of its individual fields.
- there are one method that return all three fields.

## TruffulaPrinter.java / TruffulaPrinterTest.java
- this file defines a class that is responsible for printing a directory tree structure to the console. using options for color and hidden files.
- the constructor takes three params: (TruffulaOptions options, PrintStream outStream, List<ConsoleColor> colorSequence)

## AlphabeticalFileSorter.java
- its a class that sorts an array of files Alphabetical by name.
- already complete.