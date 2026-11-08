# FreeCell

A Java implementation of FreeCell with a JavaFX graphical interface.

## Requirements

- Java Development Kit (JDK) 21 or newer
- Apache Maven 3.9 or newer

JavaFX is managed by Maven and does not need to be installed separately.

## Run the game

```sh
mvn javafx:run
```

The Maven build copies the card images from `assets/cards/` automatically.

The welcome screen includes the rules. Click a stack to select its top card,
then click another stack to try the move. Use **Undo** to reverse the last
successful move, or **New deal** to start a new game.
Use **Stats** to see wins, abandoned deals, win rate, and moves in the current
deal for the current app session.
The toolbar timer shows elapsed minutes and seconds; it resets on a new deal
and pauses when you win. At launch, the rules appear over the board; choose
**I understand — Start playing** to begin. Completing all foundations opens a
large victory dialog with your move count and time.

The card-face images are stored in `assets/cards/`. Their creator, source, and
license information is documented in [CARD-ART-CREDITS.txt](CARD-ART-CREDITS.txt).

## Run the tests

Run the test suite from this directory:

```sh
sh test.sh
```

The script runs the model and game checks through Maven.
