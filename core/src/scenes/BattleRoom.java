package scenes;

import classes.Bomb;
import classes.Player;
import classes.Bot;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import components.*;
import game.App;
import utils.ScreenManager;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class BattleRoom extends GameScreen {

    private final Skin skin;
    private final Background backgroundComponent = new Background(stage);
    private final CaptionBox captionBox = new CaptionBox(stage);
    private TextField textField;
    private final Button buttons = new Button(stage);
    private int numberOfPlayers;
    private int health;
    private int coolDown;
    private int difficulty;
    private boolean isRunning = true;

    private final List<Player> players = new ArrayList<>();
    private Bomb bomb;

    public BattleRoom(final App app) {
        super(app);

        System.out.println("Number of players: " + numberOfPlayers);
        System.out.println("Health: " + health);
        System.out.println("Cool down: " + coolDown);

        BitmapFont font = new BitmapFont();

        skin = new Skin();

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

    }

    public void initializePlayers() {
//        initializePlayer();
        initializeBots();
    }

    private void initializePlayer() {
        Player player = new Player(health, "Player 1", false);
        players.add(player);
        stage.addActor(player.getPlayerImage());
        for (Image heart : player.getHealth()) {
            stage.addActor(heart);
        }
        stage.addActor(player.getNameLabel());
    }

    private void initializeBots() {
        for (int i = 0; i < numberOfPlayers; i++) {
            Bot bot = new Bot(health, "Bot " + (i + 1), false, difficulty);
            players.add(bot);
            stage.addActor(bot.getPlayerImage());
            for (Image heart : bot.getHealth()) {
                stage.addActor(heart);
            }
            stage.addActor(bot.getNameLabel());
        }
    }

    private void initializeBomb() {
        this.bomb = new Bomb(false, coolDown);
        bomb.setBomb(400, 288);
        stage.addActor(bomb.getBombImage());
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setCoolDown(int coolDown) {
        this.coolDown = coolDown;
    }

    private void placePlayersOnCircle() {
        float centerX = (float) Gdx.graphics.getWidth() / 3;
        float centerY = (float) Gdx.graphics.getHeight() / 2;
        float maxRadius = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.4f;

        int maxPlayers = (int) (2 * Math.PI * maxRadius / 48);
        int numPlayers = Math.min(numberOfPlayers, maxPlayers);

        float radius = maxRadius / 2 * (1 / MathUtils.cosDeg((float) 180 / numPlayers));
        float angleStep = 360f / numPlayers;
        float currentAngle = 0f;

        // Distribute players and bots evenly on the circle
        int currentPlayerIndex = 0;
        for (int i = 0; i < numPlayers; i++) {
            float playerX = centerX + radius * MathUtils.cosDeg(currentAngle);
            float playerY = centerY + radius * MathUtils.sinDeg(currentAngle);

            Player currentPlayer = players.get(currentPlayerIndex);

            currentPlayer.setPosition(playerX, playerY);
            currentPlayer.placeHeart();
            currentPlayer.placeLabel();
            currentAngle += angleStep;

            currentPlayerIndex++;
            currentPlayerIndex %= numberOfPlayers;
        }
    }


    private void startGame() {
        Timer.Task task = new Timer.Task() {
            @Override
            public void run() {
                if (isRunning) {
                    simulateGame();
                }
            }
        };

        Timer.schedule(task, 0, coolDown * 1000f);
    }

    private void endGame() {
        isRunning = false;
    }

    private void simulateGame() {
        simulateBattle();
        updateBombCooldown();
        updateStage();

        if (players.size() == 1) {
            System.out.println(players.get(0).getName() + " wins!");
            app.gsm.setScreen(ScreenManager.STATE.SINGLE);
            endGame();
        }
    }

    private void simulateBattle() {
        if (players.size() > 1) {
            for (Player player : players) {
                if (player.isDead()) {
                    continue;
                }

                if (player instanceof Bot) {
                    Bot bot = (Bot) player;
                    String answer = bot.simulateAnswer(bomb);
                    bot.answer(bomb, answer);
                }

                if (bomb.isExploded()) {
                    player.reducePlayerHealth();
                }

                if (player.isDead()) {
                    System.out.println(player.getName() + " is dead!");
                }
            }

            players.removeIf(Player::isDead);
        }
    }

    private void updateBombCooldown() {
        if (bomb != null && !bomb.isExploded()) {
            bomb.updateCooldownAndExplode();
        }
    }

    public void updateStage() {
        System.out.println("Updating stage...");
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }
    
    @Override
    public void update(float delta) {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }
    

    @Override
    public void show() {
        backgroundComponent.addBackgroundImage("battleroom-bg.png", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        captionBox.insertCaptionBox("Main Menu", 0,0);
        captionBox.addCaptionBox("caption-box.png", Gdx.graphics.getWidth(), 64, 0, 0);

        ClickListener quitListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                app.gsm.setScreen(ScreenManager.STATE.SINGLE);
            }
        };

        buttons.placeButton("QUIT", 50, 25, quitListener);
        initializePlayers();
        placePlayersOnCircle();
        initializeBomb();
//        initializeInputField();
        startGame();
        
        Gdx.input.setInputProcessor(stage);
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        players.clear();
        stage.clear();

        app.gsm.setAllowBackNavigation(true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }


}