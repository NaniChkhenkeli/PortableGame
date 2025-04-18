## chess game validator ##
## project overview: ##
Chess Game Validator is a Java application designed to validate Portable Game Notation (PGN) chess files against standard chess rules. It parses PGN files, validates move legality, checks game results, and reports any errors or inconsistencies found in the games.

Key capabilities:
Full chess move validation including special moves (castling, en passant, promotion),
PGN header validation,
Game result verification,
Comprehensive error reporting,
Support for standard PGN features.

## features ##
PGN Parsing: Reads and parses standard PGN files,
Move Validation: Validates all chess moves according to standard rules,
Special Move Handling:
Castling (both kingside and queenside),
En passant captures,
Pawn promotion,
Piece positions,
Castling rights,
En passant targets,
Half-move clock,
Full move number,
Result Validation: Verifies game results against final board state,
Draw Detection: Identifies all standard draw conditions:
Stalemate,
Insufficient material,
Threefold repetition,
50-move rule,
Dead position,
Error Reporting: Detailed error messages with move context,
FEN Generation: Can output current board state in FEN notation,
Board Visualization: ASCII board display for debugging,
Move History: Tracks complete move history,
Position Counting: Detects threefold repetition.

## installation #
Java JDK 11+ 
verify JAVA_HOME environment variable is set. 

## output ##
validator provides console output with validation results, detailed error log file, summary statistics of validation results. 
Validating PGN file: /path/to/games.pgn
==================================
Found 3 game(s) in file: games.pgn

Processing game: Tournament Game - PlayerA vs PlayerB
Initial board state:
![image](https://github.com/user-attachments/assets/fc4e9bca-d91a-482d-ae4e-9cf4735a7c71)

[...]

Validation Summary:
====================
Total games processed: 3
Valid games: 2
Games with errors: 1
Error log written to: /path/to/chess_validation_errors.log


## architecture ##
main entry point -> main class handles command-line interface
PGN processing -> PgnParser handles PGN file parsing, PgnValidator coordinates validation process.
chess logic -> board -> game state. piece classes implementing move logic. 
Error handling -> ErrorReporter manages error logging. errorReport structures validation results. 
![image](https://github.com/user-attachments/assets/6c1cb88e-d405-454e-9576-884f1e193b24)


## documentation ##

Core Classes
Board (com.portablegame.main.model.Board)

Key Methods:
- initializeBoard(): Sets up the initial chess position
- tryMove(from, to, promotion): Attempts to execute a move
- isValidMove(from, to): Checks if a move is legal
- isCheck(color): Determines if king is in check
- isCheckmate(color): Checks for checkmate
- isDraw(): Checks for draw conditions
- toFEN(): Exports current position in FEN notation

Piece Classes (King, Queen, Rook, Bishop, Knight, Pawn)
Abstract base class Piece with concrete implementations for each piece type.

Key Methods (implemented by each piece):
- isValidMove(toRow, toCol): Piece-specific move validation
- getSymbol(): Unicode symbol for display
- getFENSymbol(): FEN notation symbol

PgnParser (com.portablegame.util.PgnParser)
Parses PGN files and extracts game data.

Key Methods:
- parseGame(text): Parses a single PGN game
- readGamesFromFile(path): Reads multiple games from file
- readGamesFromStream(stream): Reads games from input stream

MoveValidation (com.portablegame.main.model.MoveValidation)
Validates chess moves in algebraic notation.

Key Methods:
- validateMove(move, color, board): Validates a single move
- parseAlgebraicNotation(): Parses standard algebraic notation

Supporting Classes
Error Handling
- ErrorReporter: Manages error logging
- ErrorReport: Contains validation results
- ErrorType: Enumeration of error categories

Utilities
- MoveCoordinates: Represents move source and destination
- PgnValidator: Coordinates full game validation

Validation Rules
The validator enforces all standard chess rules:

Basic Movement
- Each piece moves according to its standard rules
- Pieces cannot move through other pieces (except knights)
- Cannot move into check
- Must move out of check if in check

Special Moves
Castling:
- King and rook must not have moved
- No pieces between them
- King cannot castle through or into check
- King cannot be in check before castling

En Passant:
- Only valid immediately after opponent's double pawn move
- Must be executed on the very next move

Promotion:
- Automatic queen promotion if no piece specified
- Valid promotion pieces: Queen, Rook, Bishop, Knight

Game End Conditions
Checkmate:
- King is in check with no legal moves

Draw Conditions:
- Stalemate
- Insufficient material
- Threefold repetition
- 50-move rule
- Agreement (1/2-1/2 result)
- Dead position (no possible checkmate)

PGN Format Support
The validator supports standard PGN features:

Headers
Required headers:
- Event, Site, Date, Round, White, Black, Result

Optional headers:
- All standard PGN headers (ECO, WhiteElo, etc.)

Move Text
Supported notation:
- Standard Algebraic Notation (SAN)
- Castling (O-O, O-O-O)
- Pawn promotions (e8=Q)
- Captures (Nxe4)
- Checks/checkmates (Qxf7+, Qxf7#)

Additional PGN Features
- Comments ({} or ;)
- Numeric Annotation Glyphs (NAGs) ($1, $2, etc.)
- Variations (())
- Move numbers (1. e4 e5 2. Nf3)

Error Handling
Error Types
The validator detects and reports several error categories:

Header Errors:
- Missing required headers
- Invalid header values

Syntax Errors:
- Malformed move notation
- Invalid move numbers

Chess Rule Violations:
- Illegal moves
- Invalid castling
- Invalid en passant
- Moving into check
- Wrong player to move

Game State Errors:
- Incorrect game result
- Invalid board state
- Impossible checkmate claims

Error Reporting
Errors are reported with:
- Error type and description
- Move number and notation
- Board state context
- Suggested corrections




## Tests ## 
Piece Tests
Pawn Tests (PawnTest.java)
Tests basic pawn movement and special rules:

Forward movement (1 and 2 squares)
Diagonal captures
Blocked movement
Color-specific movement rules

Key Test Cases:
testWhitePawnMoves: Verifies white pawn movement rules
testBlackPawnMoves: Verifies black pawn movement rules
testPawnCaptures: Tests pawn capture mechanics
testBlockedPawn: Ensures pawns can't move through pieces

Rook Tests (RookTest.java)
Tests rook movement and castling status:

Horizontal and vertical movement
Blocked path detection
Castling status tracking

Key Test Cases:
testInvalidMoves: Verifies non-straight moves are rejected
testBlockedPath: Tests piece blocking mechanics
testCastlingStatus: Verifies castling status updates

Knight Tests (KnightTest.java)
Tests knight's unique movement:

L-shaped movement patterns
Ability to jump over pieces
Invalid move detection

Key Test Cases:
testValidMoves: Verifies all 8 possible L-shapes
testInvalidMoves: Rejects non-L-shaped moves
testJumpOverPieces: Confirms knights can jump

Bishop Tests (BishopTest.java)
Tests diagonal movement:

All diagonal directions
Blocked path detection
Invalid move patterns

Key Test Cases:
testValidMoves: Tests all diagonal directions
testBlockedPath: Verifies blocking mechanics
testInvalidMoves: Rejects non-diagonal moves

Queen Tests (QueenTest.java)
Tests combined rook+bishop movement:

All straight and diagonal directions
Blocked path detection
Invalid move patterns

Key Test Cases:
testInvalidMoves: Rejects non-straight/diagonal moves
testBlockedPath: Tests blocking in all directions

King Tests (KingTest.java)
Tests king movement and check mechanics:

Single square movement
Check detection
Invalid moves

Key Test Cases:
testValidMoves: Verifies 1-square movement
testInvalidMoves: Rejects longer moves
testCheckAvoidance: Tests check response

Board Tests (BoardTest.java)
Tests board operations and game rules:

Initial setup
Move execution
Special moves (castling, en passant, promotion)
Check detection

Key Test Cases:
testInitialBoardSetup: Verifies starting position
testTryMoveValid: Tests basic move execution
testCastling: Validates castling mechanics
testEnPassant: Tests en passant captures
testPromotion: Verifies pawn promotion
testCheckDetection: Tests check recognition

Game Mechanics Tests (GameMechanicsTest.java)
Tests high-level game rules:

Check detection
Checkmate recognition
Draw conditions

Key Test Cases:
testCheckDetection: Verifies check identification
testCheckmate: Tests checkmate recognition
testFiftyMoveRule: Validates draw by 50-move rule

Move Validation Tests (MoveValidationTest.java)
Tests algebraic notation parsing and validation:

Standard move parsing
Special move notation (castling, captures, etc.)
Invalid move rejection

Key Test Cases:
testPawnMoves: Tests basic pawn moves
testPawnCaptures: Tests capture notation
testKnightMoves: Tests knight notation
testDisambiguatedMoves: Tests ambiguous move resolution
testCastling: Tests castling notation
testChecks: Tests check/checkmate notation
testPromotions: Tests promotion notation
testEnPassant: Tests en passant notation
testInvalidMoves: Rejects illegal moves

PGN Parser Tests (PgnParserTest.java)
Tests PGN file parsing:

Header validation
Move text parsing
Required tag checking

Key Test Cases:
testHeaderValidation: Tests header parsing
testMoveValidation: Tests move text parsing
testRequiredTags: Verifies mandatory headers


## Limitations ##
PGN Extensions:
Does not support all PGN extensions (like Chess960)
Limited support for non-standard annotations

Interface:
Currently command-line only
No graphical board display

Performance:
Not optimized for extremely large databases
No parallel processing
