import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FreecellWindow extends Application {
	private static final boolean DEMO_ONE_MOVE_FROM_WIN = true;
	private static final Color FELT = Color.rgb(28, 91, 71);
	private static final Color WARM_WHITE = Color.rgb(250, 248, 239);
	private static final Color GOLD = Color.rgb(232, 190, 104);
	private static final Color DARK_GREEN = Color.rgb(20, 54, 44);
	private static final Color INK = Color.rgb(38, 53, 45);
	private static final Color MUTED_INK = Color.rgb(92, 108, 96);
	private static final Color RED_ACCENT = Color.rgb(177, 52, 48);
	private static final String[] SUIT_SYMBOLS = {"♣", "♦", "♥", "♠"};

	private FreecellGame game;
	private int selectedStack = -1;
	private int moveCount;
	private int gamesStarted = 1;
	private int gamesWon;
	private int dealsAbandoned;
	private long elapsedSeconds;
	private boolean gameCompleted;
	private boolean closing;
	private boolean boardDrawPending;
	private Stage stage;
	private Canvas board;
	private TextFlow statusLabel;
	private StackPane statusPanel;
	private Label movesLabel;
	private Label timeLabel;
	private Label statisticsTimeLabel;
	private Button undoButton;
	private Timeline clockTimer;

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		game = DEMO_ONE_MOVE_FROM_WIN
				? FreecellGame.oneMoveFromWin()
				: new FreecellGame(new Random().nextLong());
		clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			elapsedSeconds++;
			updateTimeLabel();
		}));
		clockTimer.setCycleCount(Timeline.INDEFINITE);

		BorderPane content = new BorderPane();
		content.setStyle("-fx-background-color: #1c5b47;");
		content.setTop(createToolbar());
		statusLabel = createStatusLabel();
		content.setBottom(createStatusPanel(statusLabel));
		StackPane boardArea = new StackPane();
		board = new Canvas();
		board.widthProperty().bind(boardArea.widthProperty());
		board.heightProperty().bind(boardArea.heightProperty());
		board.widthProperty().addListener((observable, oldValue, newValue) -> requestBoardDraw());
		board.heightProperty().addListener((observable, oldValue, newValue) -> requestBoardDraw());
		
		board.setOnMouseClicked(event -> {
			if (event.getButton() != MouseButton.PRIMARY) {
				return;
			}
			int stackNum = stackAt(event.getX(), event.getY());
			if (stackNum != -1) {
				selectOrMove(stackNum);
			}
		});
		boardArea.getChildren().add(board);
		content.setCenter(boardArea);

		StackPane root = new StackPane(content);
		Scene scene = new Scene(root, 1240, 800);
		stage.setTitle("Freecell");
		stage.setResizable(true);
		stage.setMinWidth(840);
		stage.setMinHeight(620);
		stage.setScene(scene);
		stage.setOnCloseRequest(event -> {
			if (closing) {
				clockTimer.stop();
				return;
			}
			event.consume();
			quitGame();
		});
		stage.show();
		requestBoardDraw();
		showWelcomeOverlay(root);
	}

	private TextFlow createStatusLabel() {
		TextFlow label = new TextFlow();
		label.setTextAlignment(TextAlignment.LEFT);
		label.setLineSpacing(3);
		label.setMinHeight(Region.USE_PREF_SIZE);
		label.setMaxWidth(Double.MAX_VALUE);
		label.setPadding(new Insets(14, 22, 14, 22));
		populateStatusText(label, "Rank order: K (highest) > Q > J > 10 > 9 > 8 > 7 > 6 > 5 > 4 > 3 > 2 > A (lowest).\n"
				+ "On cascades, move one lower card onto one higher card, in the opposite color (2 onto 3, not 3 onto 2).");
		return label;
	}

	private StackPane createStatusPanel(TextFlow label) {
		StackPane panel = new StackPane(label);
		panel.setAlignment(Pos.CENTER_LEFT);
		panel.setMinHeight(74);
		panel.setMaxHeight(Double.MAX_VALUE);
		label.prefWidthProperty().bind(panel.widthProperty().subtract(40));
		panel.setStyle("-fx-background-color: #14362c; -fx-border-color: #416858 transparent transparent transparent;");
		statusPanel = panel;
		return panel;
	}

	private void setStatusText(String message) {
		statusPanel.setManaged(true);
		statusPanel.setVisible(true);
		TextFlow replacement = new TextFlow();
		replacement.setTextAlignment(TextAlignment.LEFT);
		replacement.setLineSpacing(3);
		replacement.setMinHeight(Region.USE_PREF_SIZE);
		replacement.setMaxWidth(Double.MAX_VALUE);
		replacement.setPadding(new Insets(14, 22, 14, 22));
		replacement.prefWidthProperty().bind(statusPanel.widthProperty().subtract(40));
		populateStatusText(replacement, message);
		statusPanel.getChildren().setAll(replacement);
		statusLabel = replacement;
		statusPanel.requestLayout();
	}

	private static void populateStatusText(TextFlow label, String message) {
		label.getChildren().clear();
		String highlightedPrefix = message.startsWith("Selected") ? "Selected"
				: message.startsWith("Can't move") ? "Can't move"
						: message.startsWith("Move complete") ? "Move complete" : "";
		if (!highlightedPrefix.isEmpty()) {
			Color prefixColor = switch (highlightedPrefix) {
				case "Selected" -> GOLD;
				case "Can't move" -> RED_ACCENT;
				default -> Color.rgb(155, 221, 175);
			};
			label.getChildren().add(statusRun(highlightedPrefix, prefixColor, true));
			message = message.substring(highlightedPrefix.length());
		}
		if (!message.isEmpty()) {
			label.getChildren().add(statusRun(message, WARM_WHITE, false));
		}
	}

	private static Text statusRun(String value, Color color, boolean emphasized) {
		Text text = new Text(value);
		text.setFill(color);
		text.setFont(Font.font("System", emphasized ? FontWeight.BOLD : FontWeight.MEDIUM, 17.5));
		return text;
	}

	private HBox createToolbar() {
		HBox toolbar = new HBox(8);
		toolbar.setAlignment(Pos.CENTER_LEFT);
		toolbar.setPadding(new Insets(12, 20, 12, 22));
		toolbar.setStyle("-fx-background-color: #14362c; -fx-border-color: transparent transparent #416858 transparent;");

		Label title = new Label("FREECELL");
		title.setFont(Font.font("Georgia", FontWeight.BOLD, 25));
		title.setTextFill(WARM_WHITE);
		title.setStyle("-fx-letter-spacing: 1.2px;");
		toolbar.getChildren().add(title);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		toolbar.getChildren().add(spacer);

		Button rulesButton = createButton("Rules", false);
		rulesButton.setOnAction(event -> showRules());
		rulesButton.setTooltip(new javafx.scene.control.Tooltip("Review the FreeCell rules"));
		Button statsButton = createButton("Stats", false);
		statsButton.setOnAction(event -> showStatistics());
		statsButton.setTooltip(new javafx.scene.control.Tooltip("Show this session's game results"));
		undoButton = createButton("Undo", false);
		undoButton.setDisable(true);
		undoButton.setOnAction(event -> undoMove());
		undoButton.setTooltip(new javafx.scene.control.Tooltip("Undo the last successful move"));

		movesLabel = new Label("0 MOVES");
		movesLabel.setTextFill(Color.rgb(207, 220, 208));
		movesLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
		movesLabel.setStyle("-fx-padding: 7 3 7 3;");
		timeLabel = new Label("TIME 00:00");
		timeLabel.setTextFill(Color.rgb(207, 220, 208));
		timeLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
		timeLabel.setStyle("-fx-padding: 7 3 7 3;");

		Button newDealButton = createButton("New deal", true);
		newDealButton.setOnAction(event -> startNewDeal());
		newDealButton.setTooltip(new javafx.scene.control.Tooltip("Start a new game"));
		Button quitButton = createButton("Quit", false);
		quitButton.setOnAction(event -> quitGame());
		quitButton.setTooltip(new javafx.scene.control.Tooltip("Quit FreeCell"));
		toolbar.getChildren().addAll(rulesButton, statsButton, undoButton, movesLabel,
				timeLabel, newDealButton, quitButton);
		return toolbar;
	}

	private Button createButton(String text, boolean primary) {
		Button button = new Button(text);
		button.setFont(Font.font("System", FontWeight.BOLD, 13));
		button.setTextFill(primary ? Color.rgb(36, 48, 38) : WARM_WHITE);
		button.setStyle(primary
				? "-fx-background-color: #e8be68; -fx-border-color: #e8be68; -fx-background-radius: 6; -fx-border-radius: 6;"
				: "-fx-background-color: #2a4e40; -fx-border-color: #65856f; -fx-background-radius: 6; -fx-border-radius: 6;");
		button.setPadding(new Insets(8, 11, 8, 11));
		button.setFocusTraversable(false);
		return button;
	}

	private void showStatistics() {
		int concludedDeals = gamesWon + dealsAbandoned;
		Label eyebrow = new Label("FREECELL  /  SESSION STATS");
		eyebrow.setFont(Font.font("System", FontWeight.BOLD, 11));
		eyebrow.setTextFill(Color.rgb(119, 88, 34));
		Label heading = new Label("Your progress");
		heading.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
		heading.setTextFill(DARK_GREEN);
		Label subtitle = new Label("A summary of your games in this session.");
		subtitle.setFont(Font.font("System", 14));
		subtitle.setTextFill(MUTED_INK);
		VBox header = new VBox(5, eyebrow, heading, subtitle);

		HBox highlights = new HBox(12,
				createStatCard("WIN RATE", formatWinRate(gamesWon, concludedDeals), true),
				createStatCard("GAMES WON", Integer.toString(gamesWon), false),
				createStatCard("DEALS STARTED", Integer.toString(gamesStarted), false));
		highlights.setAlignment(Pos.CENTER);
		VBox activity = new VBox(0);
		activity.setPadding(new Insets(4, 16, 4, 16));
		activity.setStyle("-fx-background-color: #f0f3e9; -fx-background-radius: 10;");
		addStat(activity, "Deals abandoned", Integer.toString(dealsAbandoned));
		addStat(activity, "Moves in this deal", Integer.toString(moveCount));

		statisticsTimeLabel = new Label("Time in this deal: " + formatElapsedTime());
		statisticsTimeLabel.setMaxWidth(Double.MAX_VALUE);
		statisticsTimeLabel.setPadding(new Insets(16, 18, 16, 18));
		statisticsTimeLabel.setStyle("-fx-background-color: #14362c; -fx-background-radius: 10; "
				+ "-fx-text-fill: #e8be68; -fx-font-size: 15px; -fx-font-weight: bold; "
				+ "-fx-font-family: monospace;");
		VBox content = new VBox(18, header, highlights, activity, statisticsTimeLabel);
		content.setPadding(new Insets(18));
		content.setStyle("-fx-background-color: #faf8ef;");

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initOwner(stage);
		dialog.setTitle("Session statistics");
		dialog.setHeaderText(null);
		dialog.getDialogPane().setGraphic(null);
		dialog.getDialogPane().setContent(content);
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
		dialog.getDialogPane().setMinWidth(560);
		dialog.getDialogPane().setPrefWidth(560);
		dialog.getDialogPane().setStyle("-fx-background-color: #faf8ef;");
		Button doneButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
		doneButton.setText("Done");
		doneButton.setStyle("-fx-background-color: #14362c; -fx-text-fill: #faf8ef; "
				+ "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20 8 20;");
		try {
			dialog.showAndWait();
		} finally {
			statisticsTimeLabel = null;
		}
	}

	private VBox createStatCard(String title, String value, boolean featured) {
		Label titleLabel = new Label(title);
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
		titleLabel.setTextFill(featured ? Color.rgb(237, 207, 142) : Color.rgb(181, 202, 187));
		Label valueLabel = new Label(value);
		valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 25));
		valueLabel.setTextFill(featured ? WARM_WHITE : Color.rgb(237, 242, 229));
		VBox card = new VBox(9, titleLabel, valueLabel);
		card.setAlignment(Pos.CENTER_LEFT);
		card.setPadding(new Insets(16, 14, 16, 14));
		card.setMaxWidth(Double.MAX_VALUE);
		card.setStyle(featured
				? "-fx-background-color: #1c5b47; -fx-background-radius: 10;"
				: "-fx-background-color: #254c3e; -fx-background-radius: 10;");
		HBox.setHgrow(card, Priority.ALWAYS);
		return card;
	}

	private void addStat(VBox panel, String name, String value) {
		HBox row = new HBox(12);
		row.setAlignment(Pos.CENTER_LEFT);
		row.setPadding(new Insets(13, 2, 13, 2));
		Label nameLabel = new Label(name);
		nameLabel.setFont(Font.font("System", 14));
		nameLabel.setTextFill(MUTED_INK);
		Label valueLabel = new Label(value);
		valueLabel.setMinWidth(Region.USE_PREF_SIZE);
		valueLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
		valueLabel.setTextFill(DARK_GREEN);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		row.getChildren().addAll(nameLabel, spacer, valueLabel);
		if (!panel.getChildren().isEmpty()) {
			row.setStyle("-fx-border-color: #dce4d8 transparent transparent transparent;");
		}
		panel.getChildren().add(row);
	}

	static String formatWinRate(int gamesWon, int concludedDeals) {
		if (gamesWon < 0 || concludedDeals < 0 || gamesWon > concludedDeals) {
			throw new IllegalArgumentException("Win and concluded deal counts must be valid.");
		}
		long percentage = concludedDeals == 0
				? 0 : Math.round(gamesWon * 100.0 / concludedDeals);
		return percentage + "%";
	}

	private VBox createRulesPanel() {
		VBox panel = new VBox(14);
		panel.setPadding(new Insets(22));
		panel.setStyle("-fx-background-color: #faf8ef;");

		Label heading = new Label("A little strategy goes a long way");
		heading.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
		heading.setTextFill(DARK_GREEN);
		Label intro = new Label("The essentials for a smooth deal");
		intro.setFont(Font.font("System", 14));
		intro.setTextFill(MUTED_INK);
		panel.getChildren().addAll(heading, intro);

		addRule(panel, "THE GOAL", "Move all 52 cards to the four foundations, building each suit from Ace up to King.");
		addRule(panel, "RANK ORDER", "King is highest and Ace is lowest: K, Q, J, 10, 9, 8, 7, 6, 5, 4, 3, 2, A.");
		addRule(panel, "MOVE A CARD", "First click the card you want to move, then click where it should go. "
				+ "To put 2 on 3, select 2 first and then click 3. Click the selected stack again to clear the selection.");
		addRule(panel, "BUILD THE CASCADES", "Move a card exactly one rank lower onto a card one rank higher, "
				+ "and alternate colors. For example, put a black 7 on a red 8, not an 8 on a 7. "
				+ "Any card can start an empty cascade.");
		addRule(panel, "USE THE FREE CELLS", "Each of the four free cells holds one card. Use them to make room and uncover cards.");
		addRule(panel, "BUILD THE FOUNDATIONS", "Start each foundation with its matching Ace, then build upward in the same suit.");
		addRule(panel, "START PLAYING", "Your deal starts when you choose “I understand — Start playing”. "
				+ "Use Rules any time for a reminder; choose Quit whenever you want to leave.");
		return panel;
	}

	private void addRule(VBox panel, String title, String description) {
		VBox rule = new VBox(5);
		rule.setPadding(new Insets(9, 12, 10, 12));
		rule.setStyle("-fx-background-color: #f0f3e9; -fx-background-radius: 7; "
				+ "-fx-border-color: transparent transparent transparent #b18a3d; -fx-border-width: 0 0 0 3;");
		Label titleLabel = new Label(title);
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
		titleLabel.setTextFill(Color.rgb(119, 88, 34));
		Label descriptionLabel = new Label(description);
		descriptionLabel.setWrapText(true);
		descriptionLabel.setMaxWidth(Double.MAX_VALUE);
		descriptionLabel.setFont(Font.font("System", 14));
		descriptionLabel.setTextFill(INK);
		rule.getChildren().addAll(titleLabel, descriptionLabel);
		panel.getChildren().add(rule);
	}

	private void showWelcomeOverlay(StackPane root) {
		StackPane overlay = new StackPane();
		overlay.setStyle("-fx-background-color: rgba(5, 24, 19, 0.62);");
		VBox welcomeCard = new VBox(14);
		welcomeCard.setMaxWidth(620);
		welcomeCard.setMaxHeight(520);
		welcomeCard.setPadding(new Insets(12));
		welcomeCard.setStyle("-fx-background-color: #faf8ef; -fx-border-color: #e8be68; -fx-border-width: 2;");

		ScrollPane rules = new ScrollPane(createRulesPanel());
		rules.setFitToWidth(true);
		rules.setPrefViewportHeight(440);
		rules.setStyle("-fx-background-color: transparent; -fx-background: #faf8ef;");
		welcomeCard.getChildren().add(rules);

		HBox actions = new HBox(12);
		actions.setAlignment(Pos.CENTER_RIGHT);
		actions.setPadding(new Insets(0, 4, 4, 4));
		Button quitButton = createButton("Quit", false);
		quitButton.setOnAction(event -> {
			clockTimer.stop();
			closing = true;
			stage.close();
		});
		Button startButton = createButton("I understand — Start playing", true);
		startButton.setOnAction(event -> {
			root.getChildren().remove(overlay);
			clockTimer.playFromStart();
		});
		actions.getChildren().addAll(quitButton, startButton);
		welcomeCard.getChildren().add(actions);
		overlay.getChildren().add(welcomeCard);
		root.getChildren().add(overlay);
	}

	private void showRules() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.initOwner(stage);
		alert.setTitle("How to play FreeCell");
		alert.setHeaderText(null);
		ScrollPane rules = new ScrollPane(createRulesPanel());
		rules.setFitToWidth(true);
		rules.setPrefViewportHeight(440);
		alert.getDialogPane().setContent(rules);
		alert.showAndWait();
	}

	private void showWinDialog() {
		Label eyebrow = new Label("FREECELL  /  DEAL COMPLETE");
		eyebrow.setFont(Font.font("System", FontWeight.BOLD, 11));
		eyebrow.setTextFill(Color.rgb(119, 88, 34));
		Label heading = new Label("You won!");
		heading.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
		heading.setTextFill(DARK_GREEN);
		Label subtitle = new Label("A summary of your completed deal.");
		subtitle.setFont(Font.font("System", 14));
		subtitle.setTextFill(MUTED_INK);
		VBox header = new VBox(5, eyebrow, heading, subtitle);

		HBox highlights = new HBox(12,
				createStatCard("MOVES", Integer.toString(moveCount), true),
				createStatCard("TIME", formatElapsedTime(), false));
		highlights.setAlignment(Pos.CENTER);
		VBox resultSummary = new VBox(0);
		resultSummary.setPadding(new Insets(4, 16, 4, 16));
		resultSummary.setStyle("-fx-background-color: #f0f3e9; -fx-background-radius: 10;");
		addStat(resultSummary, "Cards home", "52 / 52");
		addStat(resultSummary, "Deal status", "Complete");

		Label congratulations = new Label("All 52 cards are home. Well played!");
		congratulations.setMaxWidth(Double.MAX_VALUE);
		congratulations.setPadding(new Insets(16, 18, 16, 18));
		congratulations.setStyle("-fx-background-color: #14362c; -fx-background-radius: 10; "
				+ "-fx-text-fill: #e8be68; -fx-font-size: 15px; -fx-font-weight: bold; "
				+ "-fx-font-family: monospace;");
		VBox content = new VBox(18, header, highlights, resultSummary, congratulations);
		content.setPadding(new Insets(18));
		content.setStyle("-fx-background-color: #faf8ef;");

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initOwner(stage);
		dialog.setTitle("FreeCell complete!");
		dialog.setHeaderText(null);
		dialog.getDialogPane().setGraphic(null);
		dialog.getDialogPane().setContent(content);
		ButtonType keepBoard = new ButtonType("Keep looking at the board", ButtonBar.ButtonData.CANCEL_CLOSE);
		ButtonType newDeal = new ButtonType("Play another deal", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().setAll(keepBoard, newDeal);
		dialog.getDialogPane().setMinWidth(560);
		dialog.getDialogPane().setPrefWidth(560);
		dialog.getDialogPane().setStyle("-fx-background-color: #faf8ef;");
		Button keepBoardButton = (Button) dialog.getDialogPane().lookupButton(keepBoard);
		keepBoardButton.setStyle("-fx-background-color: #f0f3e9; -fx-text-fill: #14362c; "
				+ "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 14 8 14;");
		Button newDealButton = (Button) dialog.getDialogPane().lookupButton(newDeal);
		newDealButton.setStyle("-fx-background-color: #14362c; -fx-text-fill: #faf8ef; "
				+ "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 20 8 20;");
		Optional<ButtonType> result = dialog.showAndWait();
		if (result.isPresent() && result.get() == newDeal) {
			startNewDeal();
		}
	}

	private void quitGame() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Quit FreeCell now?",
				ButtonType.YES, ButtonType.NO);
		alert.initOwner(stage);
		alert.setTitle("Quit FreeCell");
		alert.setHeaderText(null);
		Optional<ButtonType> choice = alert.showAndWait();
		if (choice.isPresent() && choice.get() == ButtonType.YES) {
			abandonCurrentDeal();
			clockTimer.stop();
			closing = true;
			stage.close();
		}
	}

	private void startNewDeal() {
		if (!gameCompleted) {
			dealsAbandoned++;
		}
		game = new FreecellGame(new Random().nextLong());
		gamesStarted++;
		gameCompleted = false;
		elapsedSeconds = 0;
		updateTimeLabel();
		clockTimer.playFromStart();
		selectedStack = -1;
		moveCount = 0;
		movesLabel.setText("0 MOVES");
		undoButton.setDisable(true);
		setStatusText("New deal ready.");
		requestBoardDraw();
	}

	private void abandonCurrentDeal() {
		if (!gameCompleted) {
			dealsAbandoned++;
			gameCompleted = true;
		}
	}

	private void undoMove() {
		if (!game.undo()) {
			setStatusText("There are no moves to undo.");
			return;
		}
		if (gameCompleted) {
			gamesWon--;
			gameCompleted = false;
			clockTimer.play();
		}
		selectedStack = -1;
		moveCount--;
		movesLabel.setText(moveCount + (moveCount == 1 ? " MOVE" : " MOVES"));
		undoButton.setDisable(!game.canUndo());
		setStatusText("Last move undone. Select a card to move, then click its destination.");
		requestBoardDraw();
	}

	private void selectOrMove(int stackNum) {
		if (gameCompleted) {
			setStatusText("This game is complete. Start a new deal to play again.");
			return;
		}
		if (selectedStack < 0) {
			if (game.getStack(stackNum).length == 0) {
				setStatusText("That stack is empty.");
				return;
			}
			selectedStack = stackNum;
			Card selectedCard = topCard(game.getStack(stackNum));
			setStatusText("Selected " + formatCard(selectedCard)
					+ " — click a destination, or click this card again to clear selection.");
		} else if (selectedStack == stackNum) {
			selectedStack = -1;
			setStatusText("Selection cleared.");
		} else {
			try {
				if (game.play(selectedStack, stackNum)) {
					selectedStack = -1;
					moveCount++;
					movesLabel.setText(moveCount + (moveCount == 1 ? " MOVE" : " MOVES"));
					undoButton.setDisable(!game.canUndo());
					if (game.isGameOver()) {
						gameCompleted = true;
						gamesWon++;
						clockTimer.stop();
						statusPanel.setVisible(false);
						statusPanel.setManaged(false);
						requestBoardDraw();
						showWinDialog();
						return;
					}
					setStatusText("Move complete.");
				} else {
					setStatusText("That move is not legal.");
				}
			} catch (IllegalPlayException e) {
				Card[] sourceCards = game.getStack(selectedStack);
				Card[] destinationCards = game.getStack(stackNum);
				Card movingCard = topCard(sourceCards);
				Card destinationCard = destinationCards.length == 0
						? null : topCard(destinationCards);
				setStatusText(explainIllegalMove(movingCard, destinationCard, stackNum, e));
			}
		}
		requestBoardDraw();
	}

	private static Card topCard(Card[] cards) {
		return cards[cards.length - 1];
	}

	private static String formatCard(Card card) {
		return Card.getRankName(card.getRank()) + SUIT_SYMBOLS[card.getSuit()];
	}

	private String explainIllegalMove(Card movingCard, Card destinationCard, int destinationStack,
			IllegalPlayException error) {
		if (destinationCard != null && isCascade(selectedStack) && isCascade(destinationStack)
				&& movingCard.getRank() - destinationCard.getRank() == 1
				&& Card.isRedSuit(destinationCard.getSuit()) != Card.isRedSuit(movingCard.getSuit())) {
			return formatCard(movingCard) + " is higher than " + formatCard(destinationCard)
					+ ". To build down, put " + formatCard(destinationCard) + " on " + formatCard(movingCard)
					+ ", select " + formatCard(destinationCard) + " first, then click "
					+ formatCard(movingCard) + ". Colors must alternate.";
		}
		String destination = destinationCard == null ? "an empty stack" : formatCard(destinationCard);
		return "Can't move " + formatCard(movingCard) + " onto " + destination + ": "
				+ error.getMessage();
	}

	private boolean isCascade(int stackNum) {
		return stackNum >= 9 && stackNum <= 16;
	}

	private void updateTimeLabel() {
		timeLabel.setText("TIME " + formatElapsedTime());
		if (statisticsTimeLabel != null) {
			statisticsTimeLabel.setText("Time in this deal: " + formatElapsedTime());
		}
	}

	private String formatElapsedTime() {
		return formatElapsedTime(elapsedSeconds);
	}

	static String formatElapsedTime(long elapsedSeconds) {
		return String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
	}

	static boolean containsStackPoint(double mouseX, double mouseY, double x, double y,
			double width, double height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private void requestBoardDraw() {
		if (boardDrawPending) {
			return;
		}
		boardDrawPending = true;
		Platform.runLater(() -> {
			boardDrawPending = false;
			drawBoard();
		});
	}

	private void drawBoard() {
		if (board == null || board.getWidth() <= 0 || board.getHeight() <= 0 || game == null) {
			return;
		}
		GraphicsContext graphics = board.getGraphicsContext2D();
		double width = board.getWidth();
		double height = board.getHeight();
		graphics.setFill(FELT);
		graphics.fillRect(0, 0, width, height);
		graphics.setStroke(Color.rgb(255, 255, 255, 0.05));
		graphics.setLineWidth(1);
		for (int y = 0; y < height; y += 5) {
			graphics.strokeLine(0, y, width, y);
		}

		Layout layout = new Layout(width);
		drawSectionLabel(graphics, "FREE CELLS", layout.x[0], 24, 4, layout);
		drawSectionLabel(graphics, "FOUNDATIONS", layout.x[4], 24, 4, layout);
		for (int index = 0; index < 8; index++) {
			drawStack(graphics, index + 1, layout.x[index], layout.topY, layout, false);
		}
		graphics.setFill(Color.rgb(244, 239, 219, 0.8));
		graphics.setFont(Font.font("System", FontWeight.BOLD, 11));
		graphics.fillText("CASCADES", layout.startX, layout.cascadeY - 16);
		for (int index = 0; index < 8; index++) {
			drawStack(graphics, index + 9, layout.x[index], layout.cascadeY, layout, true);
		}
	}

	private void drawSectionLabel(GraphicsContext graphics, String label, int x, int y, int count,
			Layout layout) {
		graphics.setFill(Color.rgb(244, 239, 219, 0.8));
		graphics.setFont(Font.font("System", FontWeight.BOLD, 11));
		double groupWidth = layout.cardW * count + layout.gap * (count - 1);
		graphics.fillText(label, x + (groupWidth - textWidth(label, graphics.getFont())) / 2, y);
	}

	private void drawStack(GraphicsContext graphics, int stackNum, int x, int y,
			Layout layout, boolean cascade) {
		Card[] cards = game.getStack(stackNum);
		if (cards.length == 0) {
			drawSlot(graphics, x, y, layout.cardW, layout.cardH);
			if (stackNum >= 5 && stackNum <= 8) {
				int suit = stackNum - 5;
				graphics.setFill(Color.color(Card.isRedSuit(suit) ? 0.75 : 0.11,
						Card.isRedSuit(suit) ? 0.35 : 0.21,
						Card.isRedSuit(suit) ? 0.36 : 0.17, 0.56));
				graphics.setFont(Font.font("Serif", layout.cardW / 2.0));
				centerText(graphics, SUIT_SYMBOLS[suit], x, y, layout.cardW, layout.cardH);
			}
		} else if (cascade) {
			for (int cardIndex = 0; cardIndex < cards.length; cardIndex++) {
				int cardY = y + cardIndex * layout.cascadeStep;
				drawCard(graphics, cards[cardIndex], x, cardY, layout.cardW, layout.cardH,
						selectedStack == stackNum && cardIndex == cards.length - 1);
			}
		} else {
			drawCard(graphics, cards[cards.length - 1], x, y, layout.cardW, layout.cardH,
					selectedStack == stackNum);
		}
		graphics.setFill(Color.rgb(244, 239, 219, 0.72));
		graphics.setFont(Font.font("System", 10));
		String number = Integer.toString(stackNum);
		graphics.fillText(number, x + (layout.cardW - textWidth(number, graphics.getFont())) / 2,
				cascade ? y - 4 : y + layout.cardH + 14);
	}

	private void drawSlot(GraphicsContext graphics, int x, int y, int width, int height) {
		graphics.setFill(Color.rgb(8, 48, 37, 0.16));
		graphics.fillRoundRect(x, y, width, height, 11, 11);
		graphics.setLineWidth(1.3);
		graphics.setStroke(Color.rgb(226, 238, 224, 0.45));
		graphics.strokeRoundRect(x, y, width, height, 11, 11);
	}

	private void drawCard(GraphicsContext graphics, Card card, int x, int y,
			int width, int height, boolean selected) {
		graphics.setFill(Color.rgb(0, 0, 0, 0.22));
		graphics.fillRoundRect(x + 3, y + 4, width, height, 11, 11);
		graphics.drawImage(cardImages[card.getRank()][card.getSuit()], x, y, width, height);
		graphics.setLineWidth(selected ? 2 : 1);
		graphics.setStroke(selected ? GOLD : Color.rgb(218, 215, 203));
		graphics.strokeRoundRect(x, y, width, height, 11, 11);
	}

	private void centerText(GraphicsContext graphics, String text, int x, int y,
			int width, int height) {
		Font font = graphics.getFont();
		double textWidth = textWidth(text, font);
		double textHeight = font.getSize();
		graphics.fillText(text, x + (width - textWidth) / 2, y + (height + textHeight * 0.72) / 2);
	}

	private static double textWidth(String value, Font font) {
		Text text = new Text(value);
		text.setFont(font);
		return text.getLayoutBounds().getWidth();
	}

	private int stackAt(double mouseX, double mouseY) {
		Layout layout = new Layout(board.getWidth());
		for (int index = 0; index < 8; index++) {
			int left = layout.x[index];
			if (containsStackPoint(mouseX, mouseY, left, layout.topY,
					layout.cardW, layout.cardH)) {
				return index + 1;
			}
			if (containsStackPoint(mouseX, mouseY, left, layout.cascadeY,
					layout.cardW, layout.cardH
							+ Math.max(0, game.getStack(index + 9).length - 1)
									* layout.cascadeStep)) {
				return index + 9;
			}
		}
		return -1;
	}

	private final Image[][] cardImages = loadCardImages();

	private static Image[][] loadCardImages() {
		Image[][] images = new Image[13][4];
		for (int rank = 0; rank < images.length; rank++) {
			for (int suit = 0; suit < images[rank].length; suit++) {
				BufferedImage source = CardArt.get(rank, suit);
				WritableImage image = new WritableImage(source.getWidth(), source.getHeight());
				PixelWriter pixels = image.getPixelWriter();
				for (int y = 0; y < source.getHeight(); y++) {
					for (int x = 0; x < source.getWidth(); x++) {
						pixels.setArgb(x, y, source.getRGB(x, y));
					}
				}
				images[rank][suit] = image;
			}
		}
		return images;
	}

	private final class Layout {
		final int cardW;
		final int cardH;
		final int gap;
		final int startX;
		final int topY = 38;
		final int cascadeY;
		final int cascadeStep;
		final int[] x = new int[8];

		Layout(double availableWidth) {
			cardW = Math.max(76, Math.min(96, ((int) availableWidth - 140) / 9));
			cardH = cardW * 364 / 250;
			gap = Math.max(14, Math.min(28, ((int) availableWidth - 8 * cardW - 64) / 7));
			startX = Math.max(24, ((int) availableWidth - (8 * cardW + 7 * gap)) / 2);
			for (int index = 0; index < x.length; index++) {
				x[index] = startX + index * (cardW + gap);
			}
			cascadeY = topY + cardH + 68;
			cascadeStep = Math.max(22, cardH / 5);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
