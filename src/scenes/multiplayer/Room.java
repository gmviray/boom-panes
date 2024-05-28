package scenes.multiplayer;

import classes.*;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Room extends AnimationTimer {

    private GraphicsContext gc;
    private Scene scene;
    private Group root;
    private Canvas canvas;
    private TextField answerField;

    private int difficulty;
    private int health;
    private int duration;
    private int numBots;
    private double botsThinkingTime;

    private int currentPlayerIndex = 0;

    private Player player;
    private Bomb bomb;

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private boolean isRunning = true;

    private final GameTimer timer;
    private final GameTimer waitTimer;

    private final ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Bot> bots;
    private final Image background = new Image("assets/Backgrounds/battleroom-bg.png");

    public Room(int playerCount, int difficulty, int duration, int health) throws IOException {
        this.canvas = new Canvas(Room.WINDOW_WIDTH, Room.WINDOW_HEIGHT);
        this.gc = this.canvas.getGraphicsContext2D();
        this.root = new Group();
        this.answerField = new TextField();
        this.scene = new Scene(root, Room.WINDOW_WIDTH, Room.WINDOW_HEIGHT);
        this.root.getChildren().add(this.canvas);
        this.root.getChildren().add(this.answerField);
        this.timer = new GameTimer();
        this.waitTimer = new GameTimer();

        this.numBots = playerCount - 1;
        this.duration = duration;
        this.health = health;
        this.difficulty = difficulty;
        this.botsThinkingTime = ((double) duration / 3) - .1;

        this.initializeBots();
        this.initializePlayer();
        this.placePlayerInCircle();
        this.initializeBomb();
        this.initializeLobbyPlayers();
    }

    private void initializeLobbyPlayers() {
        this.players.add(this.player);
        this.players.addAll(this.bots);
    }

    private void initializePlayer() {
        this.player = new Player("Player");
        this.player.setHealth(this.health);
        this.player.setStatus(false);
    }

    public void initializeBots() {
        this.bots = new ArrayList<>();
        for (int i = 0; i < this.numBots; i++) {
            this.bots.add(new Bot("Bot " + i, this.difficulty));
            this.bots.get(i).setHealth(this.health);
            this.bots.get(i).setStatus(false);
        }
    }

    public void initializeBomb() {
        this.bomb = new Bomb(false, this.duration);
    }

    private void placePlayerInCircle() {
        double centerX = WINDOW_WIDTH / 2;
        double centerY = WINDOW_HEIGHT / 2.5;
        double radius = 240;
        Random random = new Random();
        double angle = 0 + random.nextInt(360);

        for (int i = 0; i <= this.numBots; i++) {
            double x;
            double y;
            double x1 = centerX + radius * Math.cos(Math.toRadians(angle));
            if (i < this.numBots) {
                x = x1;
                y = centerY + radius * Math.sin(Math.toRadians(angle));
                this.bots.get(i).setPosition(x, y);
            } else {
                x = x1;
                y = centerY + radius * Math.sin(Math.toRadians(angle));
                this.player.setPosition(x, y);
            }
            angle += 360 / (this.numBots + 1);
        }
    }

    @Override
    public void handle(long now) {
        initBackground();
        renderSprite();

        timer.start();
        waitTimer.start();

        if (isRunning) simulateAnswer();
    }

    private void renderBots() {
        for (Bot bot : this.bots) {
            bot.render(gc);
            bot.renderHealthBar(gc);
            bot.renderName(gc);
        }
    }

    private void renderPlayer() {
        this.player.render(gc);
        this.player.renderHealthBar(gc);
        this.player.renderName(gc);
    }

    private void renderSprite() {
        renderBots();
        renderBomb();
        renderPlayer();
    }

    private void renderBomb() {
        bomb.setPosition(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2.5);
        bomb.render(gc);
        bomb.renderHint(gc);
    }

    public void initBackground() {
        gc.drawImage(background, 0, 0);
    }

    private void endGame() {
        isRunning = false;
    }

    private void simulateAnswer() {
        currentPlayerIndex = currentPlayerIndex % players.size();
        String bufferPlayerAnswer = answerField.getText();
        if (players.get(currentPlayerIndex) instanceof Bot && waitTimer.getElapsedTimeSeconds() < botsThinkingTime) {
            return;
        }

        if (timer.getElapsedTimeSeconds() > duration) {
            timer.reset();
            players.get(currentPlayerIndex).reducePlayerHealth();
            if (players.get(currentPlayerIndex).isDead()) {
                players.remove(players.get(currentPlayerIndex));
                if (players.size() == 1) endGame();
            } else {
                currentPlayerIndex++;
            }
            return;
        }

        int result = 0;

        if (players.get(currentPlayerIndex) instanceof Bot currentBot) {
            String answer = currentBot.simulateAnswer(bomb);
            result = currentBot.answer(bomb, answer);
            waitTimer.reset();
        } else {
            Player currentPlayer = players.get(currentPlayerIndex);
            waitTimer.reset();

            answerField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    String playerAnswer = bufferPlayerAnswer;
                    currentPlayer.answer(bomb, playerAnswer);
                }
            });

            result = currentPlayer.answer(bomb, bufferPlayerAnswer);
        }

        if (result == 1) {
            timer.reset();
            currentPlayerIndex++;
        }
    }

    public Scene getScene() {
        return this.scene;
    }
}