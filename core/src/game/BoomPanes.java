package game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import scenes.MainMenu;

public class BoomPanes extends Game {

	private SpriteBatch batch;

	public SpriteBatch getSpriteBatch() {
		return batch;
	}

	@Override
	public void create() {
		batch = new SpriteBatch();
		setScreen(new MainMenu(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

}