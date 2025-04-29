/**
 * @author Laurin Scheuber, Nicolas Staub, Tugce Nur Tas
 * @date April 27, 2025
 * 
 * Demo application class.
 * Serves as the entry point and delegates execution to RainbowTable.main().
 * 
 * This is a simple wrapper class so we don't have to change much if we 
 * ever want to add command line arguments or other functionality.
 */
public class Demo {
    /**
     * Main entry point of the application.
     * Delegates execution to RainbowTable.main().
     * @param args Command line arguments (passed to RainbowTable.main).
     */
    public static void main(String[] args) {
        // This is a bit redundant but it's cleaner this way - Laurin
        // Maybe we can add more args processing here later
        RainbowTable.main(args);
    }
}