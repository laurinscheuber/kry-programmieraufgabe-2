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

After running the program, we found that the hash "1d56a37fb6b08aa709fe90e12ca59e12" could not be found using our Rainbow Table implementation. We also tried to brute-force the hash with the first 5,000 passwords, but it was not found.

This suggests that:

1. The plaintext corresponding to this hash is not among the first 2,000 passwords specified in the assignment.
2. It's also not in the first 5,000 passwords we checked through brute force.
3. It's possible that the plaintext is beyond the range we checked or that our implementation of the reduction function doesn't capture this specific hash.

Therefore, we conclude that it is not possible to find the plaintext for the provided hash using the Rainbow Table as specified in the assignment.
