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
- `Demo.java`: The main program entry point. It initializes the `RainbowTable`, generates the table, prints a verification example, attempts the lookup for the target hash, and prints the results.
- `README.md`: This file, documenting the project and results.

## How to Run

1.  **Compile:**
    ```bash
    javac RainbowTable.java Demo.java
    ```
2.  **Run:**
    ```bash
    java Demo
    ```

## Challenges We Faced

We spent quite a lot of time on this project. Some of the challenges we ran into:

- At first our chain generation didn't match the example at all! We spent hours debugging only to realize we forgot to reset the MD5 digest between hash calls 🤦‍♂️
- Had a tough time figuring out the exact reduction function implementation - the slide wasn't super clear about how to handle the base-36 conversion with BigInteger
- Nicolas's laptop would heat up like crazy when running the full table generation - we had to optimize our HashMap usage
- Initially had an off-by-one error in our chain indexing logic (starting from 1 instead of 0)

Overall, this was a pretty cool assignment once we got it working!

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

### Note on Table Coverage

It's important to note that while the lookup was successful for the _specific_ target hash given in the assignment, the coverage of this Rainbow Table relative to the entire possible password space is extremely small.

- **Total Password Space:** With 7 characters chosen from 36 possibilities (0-9, a-z), there are 36<sup>7</sup> = 78,364,164,096 (over 78 billion) potential passwords.
- **Table Coverage (Theoretical Maximum):** The table consists of 2,000 chains, each with 2,000 steps (links). In the best-case scenario (no chain merges or collisions), this covers 2,000 \* 2,000 = 4,000,000 unique password/hash pairs.
- **Fraction Covered:** The fraction of the total password space covered by this table is approximately 4,000,000 / 78,364,164,096 ≈ 5.1 x 10<sup>-5</sup>, or about 0.005%.

This means that if the target hash had been chosen randomly from _any_ 7-character password within the defined character set, the probability of finding it with this specific table would be roughly 1 in 20,000.

For the purpose of this assignment, the target hash `1d56a37fb6b08aa709fe90e12ca59e12` was likely chosen specifically because it _is_ contained within one of the chains generated from the first 2,000 starting passwords, allowing students to demonstrate a successful lookup without needing the immense resources required for a large-scale Rainbow Table.

## Future Improvements

If we had more time we would have tried:

- Implementing multi-threading to speed up table generation
- Using a more efficient data structure than HashMap
- Experimenting with different reduction functions to see if we can reduce collisions

## Declaration

We hereby declare that this implementation was completed by our team without using any "mitlauschen" (cheating) techniques. The algorithm was implemented based solely on the lecture materials and without looking at the implementation during the table construction phase, as required by the assignment.
