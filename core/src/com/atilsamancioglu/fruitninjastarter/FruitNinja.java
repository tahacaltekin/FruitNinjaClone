package com.atilsamancioglu.fruitninjastarter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FruitNinja extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture background;
	Texture apple;
	Texture bill;
	Texture cherry;
	Texture bomb;

	BitmapFont font;
	FreeTypeFontGenerator fontGenerator;

	Random random = new Random();
	Array<Fruit> fruitArray = new Array<Fruit>();

	int lives = 0;
	int score = 0;

	float genCounter = 0;
	private final float startGenSpeed = 1.1f;
	float genSpeed = startGenSpeed;

	private double currentTime;
	private double gameOverTime = -1.0f;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("ninjabackground.png");
		apple = new Texture("apple.png");
		bill = new Texture("bill.png");
		cherry = new Texture("cherry.png");
		bomb = new Texture("bomb.png");

		Fruit.radius = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) / 20f;

		Gdx.input.setInputProcessor(this);

		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.color = Color.WHITE;
		parameter.size = 60;
		parameter.characters = "0123456789 ScreCutoplay:.+-";
		font = fontGenerator.generateFont(parameter);
	}

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		double newTime = TimeUtils.millis() / 1000.0;
		System.out.println("newTime: " + newTime);

		double frameTime = Math.min(newTime - currentTime, 0.3);
		System.out.println("frameTime: " + frameTime);

		float deltaTime = (float) frameTime;
		System.out.println("deltaTime: " + deltaTime);

		currentTime = newTime;

		if (lives <= 0 && gameOverTime == 0) {
			//game over
			gameOverTime = currentTime;
		}

		if (lives > 0) {
			//game

			genSpeed -= deltaTime * 0.015f;

			if (genCounter <= 0) {
				genCounter = genSpeed;
				addItem();
			} else {
				genCounter -= deltaTime;
			}

			for (int i = 0; i < lives; i++) {
				batch.draw(apple, i * 40f + 20f, Gdx.graphics.getHeight() - 40f, 40f, 40f);
			}

			for (Fruit fruit : fruitArray) {
				fruit.update(deltaTime);

				switch (fruit.type) {
					case REGULAR:
						batch.draw(apple, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
					case EXTRA:
						batch.draw(cherry, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
					case ENEMY:
						batch.draw(bomb, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
					case LIFE:
						batch.draw(bill, fruit.getPos().x, fruit.getPos().y, Fruit.radius, Fruit.radius);
						break;
				}
			}

			boolean holdlives = false;
			Array<Fruit> toRemove = new Array<Fruit>();

			for (Fruit fruit : fruitArray) {
				if (fruit.outOfScreen()) {
					toRemove.add(fruit);

					if (fruit.living && fruit.type == Fruit.Type.REGULAR) {
						lives--;
						holdlives = true;
						break;
					}
				}
			}

			if (holdlives) {
				for (Fruit f : fruitArray) {
					f.living = false;
				}
			}

			for (Fruit f : toRemove) {
				fruitArray.removeValue(f, true);
			}
		}

		font.draw(batch, "Score: " + score, 50, 50);
		if (lives <= 0 ) {
			font.draw(batch, "Cut to play", Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
		}
		batch.end();
	}

	private void addItem() {

		float pos = random.nextFloat() * Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth());
		Fruit item = new Fruit(new Vector2(pos, -Fruit.radius), new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * (0.3f +(random.nextFloat() - 0.5f)), Gdx.graphics.getHeight() * 0.5f));

		float type = random.nextFloat();
		if (type > 0.98) {
			item.type = Fruit.Type.LIFE;
		} else if (type > 0.88) {
			item.type = Fruit.Type.EXTRA;
		} else if (type > 0.78) {
			item.type = Fruit.Type.ENEMY;
		}

		fruitArray.add(item);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		fontGenerator.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (lives <= 0 && currentTime - gameOverTime > 2f) {
			//menu mode
			gameOverTime = 0f;
			score = 0;
			lives = 3;
			genSpeed = startGenSpeed;
			fruitArray.clear();
		} else {
			//game mode

			Array<Fruit> toRemove = new Array<Fruit>();
			Vector2 pos = new Vector2(screenX, Gdx.graphics.getHeight() - screenY);
			int plusScore = 0;

			for (Fruit f : fruitArray) {
				if (f.clicked(pos)) {
					toRemove.add(f);

					switch (f.type) {
						case REGULAR:
							plusScore++;
							break;
						case EXTRA:
							plusScore += 2;
							break;
						case ENEMY:
							lives--;
							break;
						case LIFE:
							lives++;
							break;
					}
				}
			}

			score += plusScore * plusScore;

			for (Fruit f : toRemove) {
				fruitArray.removeValue(f, true);
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
