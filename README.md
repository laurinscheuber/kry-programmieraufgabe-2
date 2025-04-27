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

\
## Result

After implementing and running the program, our Rainbow Table **successfully found** the plaintext for the target MD5 hash `1d56a37fb6b08aa709fe90e12ca59e12`.

**The found plaintext is: `0bgec3d`** 

### Lookup Process Explanation:

1.  **Target Hash Reduction:** The lookup process started with the target hash `1d56a37fb6b08aa709fe90e12ca59e12`.
2.  **Iterative Reduction and Lookup:** The target hash was repeatedly reduced (using the reduction function `R_i`) and hashed (`H`) for each possible position `i` in a chain (from `t-1` down to `0`, where `t=2000` is the chain length). For each generated candidate endpoint hash, the Rainbow Table (HashMap) was checked.
3.  **Potential Match Found:** At some position `k`, applying the reduction `R_k` to the hash derived from the target hash produced a candidate password whose hash matched an endpoint stored in the table.
4.  **Chain Verification:** The starting password associated with that endpoint was retrieved from the table. This starting password was then used to recompute the entire chain (applying `H` and `R_i` iteratively).
5.  **Plaintext Discovery:** During the recomputation of the chain, the target hash `1d56a37fb6b08aa709fe90e12ca59e12` was encountered. The password that produced this hash within the chain is the desired plaintext.

**Conclusion:** Despite the relatively small size of the Rainbow Table (2,000 starting passwords, chain length 2,000) compared to the total password space, it was sufficient to contain a chain that included the target hash `1d56a37fb6b08aa709fe90e12ca59e12`, allowing for the successful recovery of its corresponding plaintext.
