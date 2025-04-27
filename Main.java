import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        // Target hash to find
        String targetHash = "1d56a37fb6b08aa709fe90e12ca59e12";
        
        System.out.println("Generating Rainbow Table...");
        RainbowTable rainbowTable = new RainbowTable();
        
        // Print the first chain example for verification
        System.out.println("Verifying first chain example:");
        rainbowTable.printFirstChainExample();
        
        // Generate the rainbow table
        System.out.println("\nGenerating full rainbow table...");
        long startTime = System.currentTimeMillis();
        rainbowTable.generateTable();
        long endTime = System.currentTimeMillis();
        System.out.println("Rainbow table generation took " + (endTime - startTime) + " ms");
        
        // Look up the target hash
        System.out.println("\nLooking up target hash: " + targetHash);
        startTime = System.currentTimeMillis();
        String plaintext = rainbowTable.lookupHash(targetHash);
        endTime = System.currentTimeMillis();
        
        if (plaintext != null) {
            System.out.println("Found plaintext: " + plaintext);
            
            // Verify that the hash of the found plaintext matches the target hash
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md5.digest(plaintext.getBytes());
                String generatedHash = bytesToHex(hashBytes);
                
                System.out.println("Verification: ");
                System.out.println("  Target hash: " + targetHash);
                System.out.println("  Generated hash from plaintext: " + generatedHash);
                System.out.println("  Match: " + generatedHash.equals(targetHash));
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Could not verify result: " + e.getMessage());
            }
        } else {
            System.out.println("Plaintext not found in the rainbow table");
            System.out.println("This could be because:");
            System.out.println("1. The password is not in the first 2000 passwords");
            System.out.println("2. There is a chain collision");
            System.out.println("3. The specific reduction function used doesn't capture this hash");
            
            // Try to see if we can compute the hash's plaintext by brute force to confirm
            System.out.println("\nAttempting to find the plaintext by brute force...");
            String bruteForceResult = bruteForceHash(targetHash, 5000); // Try first 5000 passwords
            
            if (bruteForceResult != null) {
                System.out.println("Found plaintext by brute force: " + bruteForceResult);
                System.out.println("This password was not found by the rainbow table due to limitations");
            } else {
                System.out.println("Could not find the plaintext in the first 5000 passwords");
            }
        }
        System.out.println("Lookup took " + (endTime - startTime) + " ms");
    }
    
    private static String bruteForceHash(String targetHash, int limit) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String password = "0000000";
            
            for (int i = 0; i < limit; i++) {
                byte[] hashBytes = md5.digest(password.getBytes());
                String generatedHash = bytesToHex(hashBytes);
                
                if (generatedHash.equals(targetHash)) {
                    return password;
                }
                
                password = getNextPassword(password);
            }
            
            return null; // Not found in the first limit passwords
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Could not perform brute force: " + e.getMessage());
            return null;
        }
    }
    
    private static String getNextPassword(String password) {
        char[] charset = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] chars = password.toCharArray();
        
        // Increment the password (similar to counting)
        for (int i = chars.length - 1; i >= 0; i--) {
            int index = -1;
            for (int j = 0; j < charset.length; j++) {
                if (charset[j] == chars[i]) {
                    index = j;
                    break;
                }
            }
            
            if (index == charset.length - 1) {
                // If this digit is at max value, reset it and carry over
                chars[i] = charset[0];
            } else {
                // Otherwise, just increment this digit and we're done
                chars[i] = charset[index + 1];
                break;
            }
        }
        
        return new String(chars);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
} 