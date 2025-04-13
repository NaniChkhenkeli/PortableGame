steps to implement:

1. set up the chess board - very first to do.
- PGN parsing.
- board, 8x8 array.
- standard moves for figures. 
- handling special moves like castling, pawn promotion, en passant.
- track whose turn is. 
- check for checkmate/check/stalemate - - also track. 



2. two-phase evaluation
- next step is to separate parsing phase from the game replay phase;
- there is no comprehensive syntax error detection for malformed PGN notation. 

3. handle standard PGN notation; parse PGN headers or comments. 

4. error reposrting 
- make sure code collect or report multiple errors. 
- for the game replay, it should stop at the first illegal move. 

5. match the game result to an actual board state. 