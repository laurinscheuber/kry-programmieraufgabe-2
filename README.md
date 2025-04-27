# Kryptographie Bonusaufgabe: MD5 Rainbow Table

**Team Members:**
- Tugce Nur Tas
- Nicolas Staub
- Laurin Scheuber

**Course:** Kryptographie (und Informationssicherheit) - 6iCa/6iCb/8iCbb

## Assignment Task

The goal of this bonus assignment was to create a Rainbow Table in Java for MD5 hashes. The specific requirements were:
- Use the first 2,000 passwords of length 7 consisting of lowercase letters ('a'-'z') and digits ('0'-'9').
- The password sequence starts from "0000000".
- Use a chain length of 2,000 (apply hash and reduction 2,000 times per starting password).
- Implement the reduction function based on the character set Z = {0, ..., 9, a, ..., z} as specified (using the construction from slide 3.27).
- Use the generated table to find the plaintext for the MD5 hash `1d56a37fb6b08aa709fe90e12ca59e12`.
- If the plaintext cannot be found, provide a justification why.

## Project Files

- `RainbowTable.java`: Contains the main logic for the Rainbow Table, including chain generation, hashing (MD5), the reduction function, and the table storage (HashMap).
- `Main.java`: The main program entry point. It initializes the `RainbowTable`, generates the table, prints a verification example, attempts the lookup for the target hash, and prints the results.
- `README.md`: This file, documenting the project and results.

## How to Run

1.  **Compile:**
    ```bash
    javac RainbowTable.java Main.java
    ```
2.  **Run:**
    ```bash
    java Main
    ```

## Result and Justification

After implementing and running the program, our Rainbow Table **could not find** the plaintext for the target hash `1d56a37fb6b08aa709fe90e12ca59e12`.

The assignment requires a justification if the hash cannot be found. Here is ours:

1.  **Very Limited Coverage:** The primary reason is the extremely limited scope of the table as defined by the assignment. We only used the *first 2,000* passwords (out of over 78 billion possible 7-character passwords in the given character set) as starting points for our chains. It is highly likely that the password corresponding to the target hash does not originate from one of these first 2,000 passwords within the 2000-step chain generation process. The table simply doesn't cover enough of the possible password space.

2.  **Chain Collisions:** While our run detected 0 endpoint collisions, Rainbow Tables can still suffer from internal collisions or merges. If the target hash existed in a chain that merged with another, or if its chain's endpoint collided with one already in the table (from a different starting password), the lookup process might fail during the verification step (`recomputeChainAndFindPassword`), leading to a false negative.

3.  **Fixed Parameters:** The fixed chain length (2000) and number of chains (2000) might inherently not be sufficient to capture this specific hash within the defined starting password set.

**Conclusion:** Finding the plaintext for `1d56a37fb6b08aa709fe90e12ca59e12` is not possible with the Rainbow Table constructed under the strict constraints of this assignment (first 2000 passwords, length 2000 chains), mainly because these constraints result in insufficient coverage of the password space.
