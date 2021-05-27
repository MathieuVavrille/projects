# Bobail AI and interface

This folder contains python scripts to play the bobai agains an AI.

To run it, one simply has to enter the command `python bobai.py`

## Rules

The bobai is a 2 players game played on a $5\times 5$ board.
The goal is to bring the bobai (initially in the middle) on the player's side (the row where the pawns start).

The bobai can move in every 8 directions by one case.

The pawns can move in every 8 direction, as far as possible until they encounter an other piece, or the edge of the board.

The first player only moves a pawn, and then a player's turn consists in moving first the bobai, and then a pawn.

If the bobai ends up on the row a player (even if the opponent put it there), it is a win for this player.

If a player cannot play (most of the time, if the bobai completely blocked), they loses.

It is possible to play online at [Board Game Arena](https://fr.boardgamearena.com/gamepanel?game=bobail)