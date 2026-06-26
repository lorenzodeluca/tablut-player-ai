# M A T R I X - Tablut AI Player



This project was developed for the **Tablut Competition 2025/26** as part of the *Foundations of Artificial Intelligence (M)* course at the **Alma Mater Studiorum – University of Bologna**.



Our team reached the **final round** of the competition, successfully implementing a highly optimized, parallelized AI agent.



## Team

* **Lorenzo De Luca**

* **Nicole Ferrari**

* **Martina Lippo**

* **Lorenzo Vannicola**



---



## Technical Approach



The core of our AI agent is based on an optimized search algorithm designed to handle the complexities of the Tablut board game.



### Search Engine

* **Minimax:** The foundation of our decision-making process, assuming optimal play from both sides.

* **Iterative Deepening:** Gradually increases the search depth. If the time limit is reached, the agent returns the best move found in the deepest completed iteration.

* **Alpha-Beta Pruning:** Significantly reduces the number of nodes explored by cutting off branches that cannot influence the final decision, ensuring faster performance.

* **Parallelism:**

&#x20;   * **Root Distribution:** Moves are queued at the root level and processed by multiple threads in parallel.

&#x20;   * **Local Pruning:** To maintain thread safety, Alpha-Beta pruning is implemented locally within each thread, allowing for a more efficient search.

&#x20;   * **Result:** This architecture allowed us to increase the exploration depth by up to 2 levels compared to sequential implementations.



### Heuristic Evaluation

The `evaluateState()` function assigns a score between **0.0 and 1.0** to any given board state. The strategy adapts dynamically based on the game phase and the side being played.



#### White Strategy (The King)

The White agent balances defensive and offensive factors based on three active states:

1.  **Eat Enemies:** Active at the start of the game (until at least 5 black pawns are captured) to thin out enemy forces.

2.  **Protect King:** A defensive stance activated when the King is in danger, focusing on surrounding the sovereign with defenders.

3.  **Escape/Default:** The standard strategy, prioritizing the King's movement toward escape routes.



*Key factors:* Pawn survival, enemy captures, positioning the King near escapes, and maintaining tactical unit density.



#### Black Strategy (The Attacker)

The Black agent focuses on containment and suppression:

* **Control Escape Routes:** Occupying strategic tiles near exits.

* **Capture Strategy:** Systematically removing white pawns to expose the King.

* **Pressure:** Minimizing the King's mobility and forcing him into vulnerable board areas.



*Key factors:* Material superiority, King mobility, proximity of attackers to the King, and immediate tactical threats.



---



## Repository

Source code available at: [https://github.com/lorenzodeluca/tablut-player-ai](https://github.com/lorenzodeluca/tablut-player-ai)



---

*University of Bologna | A.A. 2025/2026*

=======
Our core philosophy is "don't reinvent the wheel"...
In this project, we used iterative deepening/min-max/alpha-beta optimization and focused on heuristics based on the rules of the game, knowing that this intelligence can challenge both humans and other artificial intelligences.

Authors:
- Martina Lippo
- Nicole Ferrari
- Lorenzo Vannicola
- Lorenzo De Luca
