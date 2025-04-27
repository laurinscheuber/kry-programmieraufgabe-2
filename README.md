# MD5 Rainbow Table Implementation

This is a Java implementation of a Rainbow Table for MD5 hashes, created as per the assignment requirements.

## Implementation Details

- The program creates a Rainbow Table for the first 2,000 passwords of length 7 consisting of lowercase letters and digits.
- The password sequence starts with "0000000", "0000001", ..., "0000009", "000000a", "000000b", ..., "000000z", "0000010", ...
- Each chain has a length of 2,000, meaning the hash function and reduction function are applied 2,000 times for each starting password.
- The reduction function uses the character set Z = {0, 1, ..., 9, a, b, ..., z}.
- The program attempts to find the plaintext for the hash "1d56a37fb6b08aa709fe90e12ca59e12".

## Project Files

- `RainbowTable.java`: The core implementation of the Rainbow Table data structure. This class handles the generation of the table, the reduction function implementation, and the lookup process for finding plaintexts from hashes.

- `Main.java`: The driver class that runs the Rainbow Table implementation. It handles the initialization, verification of the first chain example, generation of the full table, and performing the lookup for the target hash. It also includes a brute force method as a fallback to verify results.

- `README.md`: This documentation file explaining the project, implementation details, and usage instructions.

## How to Run

1. Compile the Java files:

   ```
   javac RainbowTable.java Main.java
   ```

2. Run the main program:
   ```
   java Main
   ```

## Expected Output

The program will:

1. Print an example of the first chain to verify correctness
2. Generate the complete Rainbow Table
3. Search for the target hash and report whether the plaintext was found

## Implementation Notes

- The reduction function follows the construction from the lecture slide 3.27, using the character set {0-9, a-z}.
- If the target hash is found, the program outputs the plaintext.
- If the target hash is not found, the program explains potential reasons why.

## Conclusion

After running the program, the target hash `1d56a37fb6b08aa709fe90e12ca59e12` was **not found** using the generated Rainbow Table. The supplementary brute-force check on the first 5,000 passwords also failed to find the corresponding plaintext.

Based on the assignment ("oder begründen Sie, dass dies mit der zu konstruierenden Rainbow-Table nicht möglich ist"), here's a justification for why finding the hash might not be possible with this specific table:

1.  **Limited Password Space Coverage:** The table is built using only the *first 2,000* 7-character passwords (lowercase letters and digits) as starting points for the chains. The total number of possible passwords in this space is 36<sup>7</sup> (over 78 billion). Our 2,000 starting chains cover only a minuscule fraction of the potential passwords and their corresponding hashes.
    *   It is highly probable that the plaintext for the target hash is simply not derived from any of these initial 2,000 passwords within the 2000-step chain length.

2.  **Chain Collisions:** Rainbow tables are susceptible to collisions, where different passwords or intermediate hashes can lead to the same subsequent value after reduction and hashing.
    *   **Endpoint Collisions:** Multiple chains starting with different passwords might end with the same final password. Our table stores only one start-end pair. If the target hash belongs to a chain whose endpoint collides with another stored chain, the lookup might retrieve the *wrong* starting password, leading to a failed verification (`recomputeChainAndFindPassword` returns null).
    *   **Internal Merges:** Chains can merge before reaching their final step. If the target hash exists in a chain *after* it has merged with another, the lookup process (working backward from the hash) might still trace back to the endpoint of the *other* chain, again leading to a false alarm during recomputation.

3.  **Table Parameters (Chain Length/Number):** The chosen parameters (2000 chains, 2000 length) define the table's specific coverage. While following the assignment, these parameters might simply not be sufficient to include the chain containing the target hash.

4.  **Reduction Function Behavior:** While the reduction function aims to map hashes back to the password space, its distribution might not be perfectly uniform. It's theoretically possible, though less likely, that the specific sequence of reductions applied during the lookup for the target hash consistently produces intermediate passwords whose final chain endpoints are not present in our limited table, even if the original password was one of the first 2,000.

**Therefore, we conclude that finding the plaintext for `1d56a37fb6b08aa709fe90e12ca59e12` is not possible with the Rainbow Table constructed according to the assignment's specific constraints (first 2000 passwords, chain length 2000), primarily due to the extremely limited coverage of the vast password space.**
