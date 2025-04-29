import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.math.BigInteger;

/**
 * Implements a Rainbow Table for cracking MD5 hashes of passwords
 * consisting of 7 lowercase alphanumeric characters (0-9, a-z).
 * Based on the specification from the KRY programming assignment 2 (Slide 3.27).
 * 
 * TODO: Maybe optimize the lookupHash method later for better performance
 */
public class RainbowTable {
    private static final char[] CHARSET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int CHARSET_SIZE = CHARSET.length;
    private static final int PASSWORD_LENGTH = 7;
    private static final int CHAIN_LENGTH = 2000;
    private static final int NUM_CHAINS = 2000;
    
    private Map<String, String> rainbowMap = new HashMap<>(); // changed name from 'table' to 'rainbowMap'
    private Set<String> startPasswords = new HashSet<>(); // Restored
    private int collisionCount = 0; // Restored
    private MessageDigest md5Digest;
    
    // Laurin: Had to debug this constructor for hours!!
    public RainbowTable() {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * Runs basic self-tests to verify core functions.
     */
    public void runSelfTests() {
        System.out.println("Running self-tests...");
        boolean allTestsPassed = true;

        // Test 1: Reduction function
        String hashOfZeros = hash("0000000"); // Calculate the hash needed for the test
        String expectedReduction = "87inwgn";
        String actualReduction = reduce(hashOfZeros, 0);
        if (!expectedReduction.equals(actualReduction)) {
            System.err.printf("  [FAIL] Reduce Test: reduce(\"%s\", 0) expected \"%s\", but got \"%s\"%n",
                    hashOfZeros, expectedReduction, actualReduction);
            allTestsPassed = false;
        } else {
            System.out.println("  [PASS] Reduce Test");
        }

        // Test 2: nextPassword function
        String passwordBeforeIncrement = "000000z";
        String expectedNextPassword = "0000010";
        String actualNextPassword = getNextPassword(passwordBeforeIncrement);
        if (!expectedNextPassword.equals(actualNextPassword)) {
            System.err.printf("  [FAIL] Next Password Test: nextPassword(\"%s\") expected \"%s\", but got \"%s\"%n",
                    passwordBeforeIncrement, expectedNextPassword, actualNextPassword);
            allTestsPassed = false;
        } else {
            System.out.println("  [PASS] Next Password Test");
        }

        if (allTestsPassed) {
            System.out.println("All self-tests passed.");
        } else {
            System.err.println("Some self-tests failed.");
            // Optionally exit if tests fail: System.exit(1);
        }
        System.out.println(); // Add a newline for separation
    }

    /**
     * Generates the rainbow table.
     * Time complexity: O(NUM_CHAINS * CHAIN_LENGTH) hash operations.
     * Space complexity: O(NUM_CHAINS * PASSWORD_LENGTH * 2) for storing start/end passwords.
     */
    public void generateTable() {
        long t0 = System.nanoTime(); // Start timing
        String startPassword = "0000000";
        collisionCount = 0; // Reset collision count
        startPasswords.clear(); // Ensure set is clear before generation

        // Nicolas: This loop takes forever to run on my laptop!
        for (int i = 0; i < NUM_CHAINS; i++) {
            // Use startPasswords set to avoid recomputing chains for duplicate starts (though unlikely with sequential generation)
            if (startPasswords.add(startPassword)) {
                String endPassword = generateChain(startPassword);

                // Check for endpoint collision BEFORE putting into the table
                if (rainbowMap.containsKey(endPassword)) {
                    collisionCount++;
                    // System.out.printf("DEBUG - Collision found: %s and %s both end at %s%n", 
                    //                  startPassword, rainbowMap.get(endPassword), endPassword);
                } else {
                    rainbowMap.put(endPassword, startPassword);
                }
            } // else: start password was already processed (should not happen with sequential generation, but good check)

            // Generate next starting password
            startPassword = getNextPassword(startPassword);
            
            // Print progress every 200 chains (commented out to avoid spamming console)
            //if (i % 200 == 0) {
            //    System.out.println("Progress: " + i + "/" + NUM_CHAINS + " chains generated");
            //}
        }

        // Use startPasswords.size() for the actual number of unique chains generated
        System.out.println("Rainbow table generated with " + startPasswords.size() + " unique chains (target: " + NUM_CHAINS + ")");
        // Explain collision count: Indicates how many generated chains ended with a password
        // that was already an endpoint for a previously generated chain.
        System.out.println("Detected " + collisionCount + " chain endpoint collisions during generation.");
        long t1 = System.nanoTime(); // End timing
        System.out.printf("Table generation took: %.2f s%n", (t1 - t0) / 1e9);
    }
    
    /**
     * Attempts to crack the given MD5 hash using the generated rainbow table.
     * @param targetHash The target MD5 hash string.
     * @return The found plaintext password, or null if not found.
     */
    public String lookupHash(String targetHash) {
        long t0 = System.nanoTime(); // Start timing
        String foundPassword = null;
        
        // TODO: Optimize this lookup code, it works but we could make it faster
        for (int i = CHAIN_LENGTH - 1; i >= 0; i--) {
            String currentHash = targetHash;
            String reducedPassword = null;

            // Apply reduction and hashing from the potential position 'i' to the end of the chain
            for (int j = i; j < CHAIN_LENGTH; j++) {
                reducedPassword = reduce(currentHash, j);

                // Performance optimization: Check table immediately after reduction
                // If this reduced password is an endpoint, we might have a candidate
                if (rainbowMap.containsKey(reducedPassword)) {
                    String candidateStartPassword = rainbowMap.get(reducedPassword);
                    // Verify the chain only if the potential hit position 'i' matches
                    // the position derived from the full chain recomputation.
                    // This avoids unnecessary recomputations for internal chain matches
                    // that don't align with the endpoint 'i'.
                    // (A simpler, slightly less optimized approach is to always recompute)

                    // Simple approach: Recompute and check
                    String potentialPassword = recomputeChainAndFindPassword(candidateStartPassword, targetHash);
                    if (potentialPassword != null) {
                        foundPassword = potentialPassword;
                        break; // Found it, exit inner loop
                    }
                    // If potentialPassword is null, it was a false alarm, continue checking other 'j' or 'i'
                }

                // If not found as an endpoint yet, continue the reduction/hash cycle for this 'i'
                if (j < CHAIN_LENGTH - 1) { // Don't hash the last reduction
                     currentHash = hash(reducedPassword);
                } else {
                    // This was the last reduction (j == CHAIN_LENGTH - 1),
                    // the check table.containsKey(reducedPassword) above handled it.
                }
            } // end inner loop (j)

            if (foundPassword != null) {
                break; // Password found, exit the outer loop (i)
            }
        } // end outer loop (i)


        long t1 = System.nanoTime(); // End timing
        System.out.printf("Lookup took: %.2f s%n", (t1 - t0) / 1e9);

        // Verification step (optional but good practice)
        if (foundPassword != null) {
            System.out.println("Verification: ");
            System.out.println("  Target hash: " + targetHash);
            String generatedHash = hash(foundPassword);
            System.out.println("  Generated hash from plaintext: " + generatedHash);
            System.out.println("  Match: " + targetHash.equals(generatedHash));
        }


        return foundPassword; // Return found password or null
    }
    
    /**
     * Regenerates a chain from a starting password to find the plaintext corresponding to the target hash.
     * @param startPassword The starting password of the potential chain.
     * @param targetHash The target MD5 hash.
     * @return The plaintext password if found in this chain, otherwise null.
     */
    private String recomputeChainAndFindPassword(String startPassword, String targetHash) {
        String currentPassword = startPassword;
        String currentHash;
        
        // This part was tricky! -Tugce
        for (int i = 0; i < CHAIN_LENGTH; i++) {
            currentHash = hash(currentPassword);
            
            if (currentHash.equals(targetHash)) {
                return currentPassword; // Found the password
            }
            
            currentPassword = reduce(currentHash, i);
        }
        
        return null; // Password not found in this chain (likely a false alarm due to collision)
    }
    
    /**
     * Generates a single chain of passwords starting from startPassword.
     * @param startPassword The initial password for the chain.
     * @return The final password at the end of the chain.
     */
    private String generateChain(String startPassword) {
        String currentPassword = startPassword;
        String currentHash;
        
        for (int i = 0; i < CHAIN_LENGTH; i++) {
            currentHash = hash(currentPassword);
            currentPassword = reduce(currentHash, i);
        }
        
        return currentPassword;
    }
    
    /**
     * Reduction function R_j from slide 3.27.
     * Takes the 128-bit MD5 digest (as a hex string), converts it to BigInteger,
     * adds the position index j, converts the result to a base-36 string,
     * and returns the last PASSWORD_LENGTH characters, left-padded with '0' if necessary.
     * Base-36 is used because the character set (0-9a-z) has 36 characters.
     * The last 7 digits are used to match the required PASSWORD_LENGTH.
     * @param hashHex The MD5 hash as a 32-character hexadecimal string.
     * @param j The position index in the chain (0 to CHAIN_LENGTH - 1).
     * @return The reduced password string of length PASSWORD_LENGTH.
     */
    private String reduce(String hashHex, int j) {
        final int PW_LENGTH = PASSWORD_LENGTH;           // readability
        // Convert hex hash to BigInteger
        BigInteger value = new BigInteger(hashHex, 16)
                                // Add the position index j
                                .add(BigInteger.valueOf(j));

        // Convert the resulting number to base-36
        String b36 = value.toString(36);                 
        
        // Left-pad with '0' if the base-36 string is shorter than the password length
        while (b36.length() < PW_LENGTH) {               
            b36 = "0" + b36;
        }
        // Return the last PW_LENGTH characters (substring from the end)
        return b36.substring(b36.length() - PW_LENGTH);  
    }
    
    /**
     * Computes the MD5 hash of the input string.
     * @param input The string to hash.
     * @return The MD5 hash as a 32-character lowercase hexadecimal string.
     */
    private String hash(String input) {
        md5Digest.reset(); // IMPORTANT: This was causing our bug!! - took 3 hours to find
        byte[] bytes = md5Digest.digest(input.getBytes());
        return bytesToHex(bytes);
    }
    
    /**
     * Optimized getNextPassword implementation using direct character arithmetic.
     * Increments the password string as if it were a base-36 number.
     * @param password The current password string.
     * @return The next password string in the sequence.
     */
    private String getNextPassword(String password) {
        char[] chars = password.toCharArray();
        
        // Increment the password (treat as base-36 number)
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            int index;
            
            // Optimized index calculation (char to 0-35)
            if (c >= '0' && c <= '9') {
                index = c - '0';
            } else { // Assuming characters 'a' through 'z'
                index = c - 'a' + 10;
            }
            
            // Check for carry-over
            if (index == CHARSET_SIZE - 1) {
                // If this digit is at max value ('z'), reset it to '0' and carry over
                chars[i] = CHARSET[0]; 
            } else {
                // Otherwise, just increment this digit and we're done
                chars[i] = CHARSET[index + 1];
                break; // No more carry-over needed
            }
        }
        
        return new String(chars);
    }
    
    /**
     * Converts a byte array to its hexadecimal string representation.
     * @param bytes The byte array (e.g., from an MD5 digest).
     * @return The hexadecimal string (lowercase).
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Prints the first few steps of the first chain (starting from "0000000")
     * to verify the hash and reduction functions against the assignment example.
     */
    public void printFirstChainExample() {
        String currentPassword = "0000000";
        System.out.println("First chain example (Password -> Hash -> Reduced Password):");
        System.out.println(currentPassword);

        // Print first 3 steps (hash -> reduce -> hash -> reduce -> hash -> reduce)
        for (int i = 0; i < 3; i++) {
            String hash = hash(currentPassword);
            System.out.println(" -> " + hash);

            currentPassword = reduce(hash, i);
             // Add expected value only for the first reduction as per feedback
            System.out.println(" -> " + currentPassword + (i == 0 ? " (Expected reduction: 87inwgn)" : ""));
        }

    }

    /**
     * Main method to run the Rainbow Table generation and lookup.
     * Includes self-tests, verification of the first chain, and the final lookup result.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
            RainbowTable rt = new RainbowTable();

            rt.runSelfTests(); // Run self-tests first

            System.out.println(); // Add newline
            System.out.println("Verifying first chain example:");
            rt.printFirstChainExample(); // Verify reduction function matches example

            System.out.println(); // Add newline
            System.out.println("Generating table...");
            rt.generateTable(); // Generate the actual table

            String target = "1d56a37fb6b08aa709fe90e12ca59e12";
            System.out.println(); // Add newline
            System.out.println("Looking up hash: " + target);
            String pwd = rt.lookupHash(target);

            // Print result
            System.out.println(); // Add newline
            System.out.println(pwd != null
                ? "Found plaintext: " + pwd
                : "Not found in table");
        }
}