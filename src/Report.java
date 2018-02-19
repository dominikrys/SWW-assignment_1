// This is to handle logging of normal and erroneous behaviours.

public class Report {

  // Report a behaviour by printing a message to the console
  public static void behaviour(String message) {
    System.err.println(message);
  }

  // Report an error by printing a message to the console
  public static void error(String message) {
    System.err.println(message);
  }

  // Print message to the console and exit
  public static void errorAndGiveUp(String message) {
    Report.error(message);
    System.exit(1);
  }
}
