import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RainbowTable {
    private static final char[] CHARSET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int CHARSET_SIZE = CHARSET.length;
    private static final int PASSWORD_LENGTH = 7;
    private static final int CHAIN_LENGTH = 2000;
    private static final int NUM_CHAINS = 2000;
    
    private Map<String, String> table = new HashMap<>();
    private Set<String> startPasswords = new HashSet<>();
    private int collisionCount = 0;
    private MessageDigest md5Digest;
    
    public RainbowTable() {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    public void generateTable() {
        String startPassword = "0000000";
        
        for (int i = 0; i < NUM_CHAINS; i++) {
            if (!startPasswords.contains(startPassword)) {
                startPasswords.add(startPassword);
                String endPassword = generateChain(startPassword);
                
                if (table.containsKey(endPassword)) {
                    collisionCount++;
                }
                
                table.put(endPassword, startPassword);
            }
            
            // Generate next starting password
            startPassword = getNextPassword(startPassword);
        }
        
        System.out.println("Rainbow table generated with " + table.size() + " chains");
        System.out.println("Detected " + collisionCount + " chain endpoint collisions");
    }
    
    public String lookupHash(String targetHash) {
        for (int i = CHAIN_LENGTH - 1; i >= 0; i--) {
            String currentHash = targetHash;
            String reducedPassword = null;
            
            // Apply reduction and hashing again starting from the specified position
            for (int j = i; j < CHAIN_LENGTH; j++) {
                reducedPassword = reduce(currentHash, j);
                
                if (j < CHAIN_LENGTH - 1) { // Don't hash the last reduction
                    currentHash = hash(reducedPassword);
                }
            }
            
            // Check if the endpoint is in our table
            if (table.containsKey(reducedPassword)) {
                // If yes, regenerate the chain from the start point
                String candidate = table.get(reducedPassword);
                String foundPassword = recomputeChainAndFindPassword(candidate, targetHash);
                
                if (foundPassword != null) {
                    return foundPassword;
                }
            }
        }
        
        return null; // Hash not found in the rainbow table
    }
    
    private String recomputeChainAndFindPassword(String startPassword, String targetHash) {
        String currentPassword = startPassword;
        String currentHash;
        
        for (int i = 0; i < CHAIN_LENGTH; i++) {
            currentHash = hash(currentPassword);
            
            if (currentHash.equals(targetHash)) {
                return currentPassword;
            }
            
            currentPassword = reduce(currentHash, i);
        }
        
        return null; // This means we had a collision in the endpoint
    }
    
    private String generateChain(String startPassword) {
        String currentPassword = startPassword;
        String currentHash;
        
        for (int i = 0; i < CHAIN_LENGTH; i++) {
            currentHash = hash(currentPassword);
            currentPassword = reduce(currentHash, i);
        }
        
        return currentPassword;
    }
    
    private String hash(String input) {
        md5Digest.reset();
        byte[] bytes = md5Digest.digest(input.getBytes());
        return bytesToHex(bytes);
    }
    
    private String reduce(String hash, int position) {
        char[] password = new char[PASSWORD_LENGTH];

        // The reduction function based on slide 3.27 (ASSUMED IMPLEMENTATION)
        // For each character position of the password:
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            // Get the index for the hash part, wrapping around if necessary
            // Ensure the index calculation stays within the bounds of the hash string (length 32)
            int hashPartStart = (i * 2 + position) % (hash.length() - 1); // Use hash.length() - 1 to avoid index out of bounds for the pair

            // Extract a 2-character hex string part
            String hashPart = hash.substring(hashPartStart, hashPartStart + 2);

            int hashValue = Integer.parseInt(hashPart, 16);

            // Map to the correct character set
            password[i] = CHARSET[hashValue % CHARSET_SIZE];
        }

        return new String(password);
    }
    
    private String getNextPassword(String password) {
        char[] chars = password.toCharArray();
        
        // Increment the password (similar to counting)
        for (int i = chars.length - 1; i >= 0; i--) {
            int index = -1;
            for (int j = 0; j < CHARSET.length; j++) {
                if (CHARSET[j] == chars[i]) {
                    index = j;
                    break;
                }
            }
            
            if (index == CHARSET.length - 1) {
                // If this digit is at max value, reset it and carry over
                chars[i] = CHARSET[0];
            } else {
                // Otherwise, just increment this digit and we're done
                chars[i] = CHARSET[index + 1];
                break;
            }
        }
        
        return new String(chars);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public void printFirstChainExample() {
        String currentPassword = "0000000";
        System.out.println(currentPassword);
        
        for (int i = 0; i < 3; i++) {
            String hash = hash(currentPassword);
            System.out.println(hash);
            
            currentPassword = reduce(hash, i);
            System.out.println(currentPassword);
        }
    }
}