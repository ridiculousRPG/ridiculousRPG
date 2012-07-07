/*
 * Copyright 2011 Alexander Baumgartner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ridiculousRPG.animation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.ridiculousRPG.util.Direction;
import com.ridiculousRPG.util.Speed;
import com.ridiculousRPG.util.TextureRegionLoader;
import com.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;

/**
 * Every Animation consists of one texture which is split up into tiles. This
 * tiles are switched periodically.<br>
 * For compatibility with old graphic cards, the texture should have a width and
 * a height by the power of two.<br>
 * Some good-sized animation textures for example: 256x256 pixel, 256x512 pixel
 * 1024x512 pixel. Don't use textures with more than 2048 pixel in width or
 * height.
 * 
 * @author Alexander Baumgartner
 */
public class TileAnimation implements Disposable, Serializable {
	private static final long serialVersionUID = 1L;

	private transient TextureRegionRef animationTexture;
	private transient TextureRegion[][] animationTiles; // [row][col]

	private int animationRow = 0, animationCol = 0;
	private float animationTimer = 1.001f;
	/**
	 * If the animationSpeed is set to null, it will automatically be computed
	 * from the move-distance, which is given by a relative x,y - position. (You
	 * can always use null if you don't want to worry about the characters walk
	 * and run - animations)
	 */
	public Speed animationSpeed = null;
	/**
	 * Indicates if one cycle of this animation has finished<br>
	 * This value must not be correct at all situations<br>
	 * It's only an estimation which should never be used for important
	 * computations!!!
	 */
	public boolean animationCycleFinished = false;

	// Needed for serialization
	private String path;
	private boolean isCompressed;

	/**
	 * Instantiate a new animation. Every animation consists of tiles which are
	 * switched periodically.<br>
	 * For compatibility with old graphic cards, the texture should have a width
	 * and a height by the power of two.<br>
	 * Good sizes for animation textures are for example: 256x256 pixel, 256x512
	 * pixel 1024x512 pixel. Don't use textures with more than 2048 pixel width
	 * or height.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param tileWidth
	 *            The width of one tile. Every animation tile must have the same
	 *            width.
	 * @param tileHeight
	 *            The height of one tile. Every animation tile must have the
	 *            same height.
	 * @param anzCols
	 *            The number of columns (how much tiles are in one row).
	 * @param anzRows
	 *            The number of rows.
	 */
	public TileAnimation(String path, int tileWidth, int tileHeight,
			int anzCols, int anzRows) {
		setAnimationTexture(path, tileWidth, tileHeight, anzCols, anzRows,
				false);
	}

	/**
	 * Instantiate a new animation. Every animation consists of tiles which are
	 * switched periodically.<br>
	 * For compatibility with old graphic cards, the texture should have a width
	 * and a height by the power of two.<br>
	 * Good sizes for animation textures are for example: 256x256 pixel, 256x512
	 * pixel 1024x512 pixel. Don't use textures with more than 2048 pixel width
	 * or height.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param tileWidth
	 *            The width of one tile. Every animation tile must have the same
	 *            width.
	 * @param tileHeight
	 *            The height of one tile. Every animation tile must have the
	 *            same height.
	 * @param anzCols
	 *            The number of columns (how much tiles are in one row).
	 * @param anzRows
	 *            The number of rows.
	 * @param isCompressed
	 *            true if the animation should be uncompressed. (default=false)
	 *            <ul>
	 *            <li>If there is only one row, a compressed animation is
	 *            uncompressed by flipping this row to a second one.<br>
	 *            <li>If there are three rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            <li>If there are fife rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            The fourth row represents the south east direction and will be
	 *            flipped to the south west.<br>
	 *            And the fifth row represents the north east direction and will
	 *            be flipped to the north west.
	 *            </ul>
	 */
	public TileAnimation(String path, int tileWidth, int tileHeight,
			int anzCols, int anzRows, boolean isCompressed) {
		setAnimationTexture(path, tileWidth, tileHeight, anzCols, anzRows,
				isCompressed);
	}

	/**
	 * Instantiate a new animation. Every animation consists of tiles which are
	 * switched periodically.<br>
	 * For compatibility with old graphic cards, the texture should have a width
	 * and a height by the power of two.<br>
	 * Good sizes for animation textures are for example: 256x256 pixel, 256x512
	 * pixel 1024x512 pixel. Don't use textures with more than 2048 pixel width
	 * or height.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param tileWidth
	 *            The width of one tile. Every animation tile must have the same
	 *            width.
	 * @param tileHeight
	 *            The height of one tile. Every animation tile must have the
	 *            same height.
	 * @param anzCols
	 *            The number of columns (how much tiles are in one row).
	 * @param anzRows
	 *            The number of rows.
	 * @param isCompressed
	 *            true if the animation should be uncompressed. (default=false)
	 *            <ul>
	 *            <li>If there is only one row, a compressed animation is
	 *            uncompressed by flipping this row to a second one.<br>
	 *            <li>If there are three rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            <li>If there are fife rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            The fourth row represents the south east direction and will be
	 *            flipped to the south west.<br>
	 *            And the fifth row represents the north east direction and will
	 *            be flipped to the north west.
	 *            </ul>
	 * @param animationSpeed
	 *            The fixed animation speed.
	 */
	public TileAnimation(String path, int tileWidth, int tileHeight,
			int anzCols, int anzRows, boolean isCompressed, Speed animationSpeed) {
		setAnimationTexture(path, tileWidth, tileHeight, anzCols, anzRows,
				isCompressed);
		this.animationSpeed = animationSpeed;
	}

	/**
	 * ATTENTION: The new animation texture must have exactly the same form as
	 * the old one!<br>
	 * Image size must be the same, tile (=character) size too,...
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @return The TextureRegion, which represents the new state of the
	 *         animation
	 */
	public TextureRegion setAnimationTexture(String path) {
		return setAnimationTexture(path, false);
	}

	/**
	 * ATTENTION: The new animation texture must have exactly the same form as
	 * the old one!<br>
	 * Image size must be the same, tile (=character) size too,...
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param isCompressed
	 *            true if the animation should be uncompressed. (default=false)
	 *            <ul>
	 *            <li>If there is only one row, a compressed animation is
	 *            uncompressed by flipping this row to a second one.<br>
	 *            <li>If there are three rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            <li>If there are fife rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            The fourth row represents the south east direction and will be
	 *            flipped to the south west.<br>
	 *            And the fifth row represents the north east direction and will
	 *            be flipped to the north west.
	 *            </ul>
	 * @return The TextureRegion, which represents the new state of the
	 *         animation
	 */
	public TextureRegion setAnimationTexture(String path, boolean isCompressed) {
		return setAnimationTexture(path, animationTiles[0][0].getRegionWidth(),
				animationTiles[0][0].getRegionHeight(),
				animationTiles[0].length, animationTiles.length, isCompressed);
	}

	/**
	 * Loads a new texture for this animation. Every animation consists of tiles
	 * which are switched periodically.<br>
	 * For compatibility with old graphic cards, the texture should have a width
	 * and a height by the power of two.<br>
	 * Good sizes for animation textures are for example: 256x256 pixel, 256x512
	 * pixel 1024x512 pixel. Don't use textures with more than 2048 pixel width
	 * or height.
	 * 
	 * @param path
	 *            The path to the texture file - which should be a power of 2
	 *            sized png image
	 * @param tileWidth
	 *            The width of one tile. Every animation tile must have the same
	 *            width.
	 * @param tileHeight
	 *            The height of one tile. Every animation tile must have the
	 *            same height.
	 * @param anzCols
	 *            The number of columns (how much tiles are in one row).
	 * @param anzRows
	 *            The number of rows.
	 * @param isCompressed
	 *            true if the animation should be uncompressed. (default=false)
	 *            <ul>
	 *            <li>If there is only one row, a compressed animation is
	 *            uncompressed by flipping this row to a second one.<br>
	 *            <li>If there are three rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            <li>If there are fife rows, the third row represents the east
	 *            direction and will be flipped to the west.<br>
	 *            The fourth row represents the south east direction and will be
	 *            flipped to the south west.<br>
	 *            And the fifth row represents the north east direction and will
	 *            be flipped to the north west.
	 *            </ul>
	 * @return The TextureRegion, which represents the new state of the
	 *         animation
	 */
	public TextureRegion setAnimationTexture(String path, int tileWidth,
			int tileHeight, int anzCols, int anzRows, boolean isCompressed) {
		this.path = path;
		this.isCompressed = isCompressed;
		if (animationTexture != null)
			animationTexture.dispose();
		animationTexture = TextureRegionLoader.load(path, 0, 0, tileWidth
				* anzCols, tileHeight * anzRows);
		animationTiles = animationTexture.split(tileWidth, tileHeight);
		if (isCompressed)
			animationTiles = uncompressAnimation(animationTiles);
		return animationTiles[animationRow % animationTiles.length][animationCol
				% animationTiles[0].length];
	}

	private TextureRegion[][] uncompressAnimation(TextureRegion[][] animation) {
		TextureRegion[][] tmp;
		switch (animation.length) {
		case 1:
			// 2 directions compressed
			tmp = new TextureRegion[2][animation[0].length];
			tmp[0] = animation[0];
			for (int i = 0; i < tmp[1].length; i++) {
				tmp[1][i] = new TextureRegion(tmp[0][i]);
				tmp[1][i].flip(true, false);
			}
			return tmp;
		case 3:
			// 4 directions compressed
			tmp = new TextureRegion[4][animation[0].length];
			tmp[0] = animation[0];
			tmp[3] = animation[1];
			tmp[2] = animation[2];
			for (int i = 0; i < tmp[1].length; i++) {
				tmp[1][i] = new TextureRegion(tmp[2][i]);
				tmp[1][i].flip(true, false);
			}
			return tmp;
		case 5:
			// 8 directions compressed
			tmp = new TextureRegion[8][animation[0].length];
			tmp[0] = animation[0];
			tmp[3] = animation[1];
			tmp[2] = animation[2];
			tmp[6] = animation[3];
			tmp[7] = animation[4];
			for (int i = 0; i < tmp[1].length; i++) {
				tmp[1][i] = new TextureRegion(tmp[2][i]);
				tmp[1][i].flip(true, false);
				tmp[4][i] = new TextureRegion(tmp[6][i]);
				tmp[4][i].flip(true, false);
				tmp[5][i] = new TextureRegion(tmp[7][i]);
				tmp[5][i].flip(true, false);
			}
			return tmp;
		default:
			return animation;
		}
	}

	/**
	 * This method returns the actual TextureRegion for the animation
	 * 
	 * @return The TextureRegion, which represents the actual state of the
	 *         animation
	 */
	public TextureRegion getActualTextureRegion() {
		return animationTiles[animationRow % animationTiles.length][animationCol
				% animationTiles[0].length];
	}

	/**
	 * This method returns a TextureRegion specified by row and column.
	 * 
	 * @return The TextureRegion at the given row and column
	 * @param row
	 * @param col
	 */
	public TextureRegion getTextureRegion(int row, int col) {
		return animationTiles[row % animationTiles.length][col
				% animationTiles[0].length];
	}

	/**
	 * Computes the animation and returns the actual tile (as TextureRegion) to
	 * display.<br>
	 * The animation speed is computed by the distance given as relative x,y -
	 * position.
	 * 
	 * @param x
	 * @param y
	 * @param deltaTime
	 */
	public TextureRegion animate(float x, float y, float deltaTime) {
		return animate(x, y, Direction
				.fromMovement(x, y, animationTiles.length), deltaTime);
	}

	/**
	 * Computes the animation and returns the actual tile (as TextureRegion) to
	 * display.<br>
	 * The animation speed is computed by the distance given as relative x,y -
	 * position.
	 * 
	 * @param x
	 * @param y
	 * @param dir
	 * @param deltaTime
	 */
	public TextureRegion animate(float x, float y, Direction dir,
			float deltaTime) {
		float pixelPerSecond;
		if (deltaTime == 0) {
			pixelPerSecond = 0;
		} else if (animationSpeed == null) {
			pixelPerSecond = (x < 0 ? -x : x) + (y < 0 ? -y : y);
			pixelPerSecond = pixelPerSecond / deltaTime;
			if (x != 0 && y != 0)
				pixelPerSecond = (float)Math.sqrt(x*x+y*y)/deltaTime;
		} else {
			pixelPerSecond = animationSpeed.getPixelPerSecond();
		}
		return animate(pixelPerSecond, dir.getDirectionIndex(), deltaTime);
	}

	/**
	 * Computes the animation and returns the actual tile (as TextureRegion) to
	 * display.<br>
	 * The defined speed is used for animating this tile. If the speed is null,
	 * the first tile of the given row is returned.
	 * 
	 * @param animationTextureRow
	 *            The row of images which are used for this animation<br>
	 *            If animationTextureRow is -1, then the animation runs over all
	 *            tiles in all rows. The actual row will be computed like the
	 *            column.
	 */
	public TextureRegion animate(int animationTextureRow, float deltaTime) {
		return animationSpeed == null ? setAnimationPosition(
				animationTextureRow, 0) : animate(animationSpeed
				.getPixelPerSecond(), animationTextureRow, deltaTime);
	}

	/**
	 * Computes the animation and returns the actual tile (as TextureRegion) to
	 * display.<br>
	 * The given speed is used for animating this tile. If the given animation
	 * speed is null, the defined speed for this animation object is used. If
	 * this speed is also null, the first tile of the given row is returned.
	 * 
	 * @param speed
	 *            Speed of the animation (null is allowed)
	 * @param animationTextureRow
	 *            The row of images which are used for this animation<br>
	 *            If animationTextureRow is -1, then the animation runs over all
	 *            tiles in all rows. The actual row will be computed like the
	 *            column.
	 */
	public TextureRegion animate(int animationTextureRow, Speed speed,
			float deltaTime) {
		return speed == null ? animate(animationTextureRow, deltaTime)
				: animate(speed.getPixelPerSecond(), animationTextureRow,
						deltaTime);
	}

	private TextureRegion animate(float pixelPerSecond,
			int animationTextureRow, float deltaTime) {
		if (deltaTime > 0) {
			animationCycleFinished = false;
			animationTimer += Math.sqrt(pixelPerSecond * .25f) * deltaTime;
			if (Float.isNaN(animationTimer)) {
				animationTimer = 1.01f;
			}
			if (animationTextureRow > -1) {
				while (animationTimer >= animationTiles[animationTextureRow].length) {
					animationTimer -= animationTiles[animationTextureRow].length;
					animationCycleFinished = true;
				}
				animationRow = animationTextureRow;
			} else {
				while (animationTimer >= animationTiles[animationRow].length) {
					animationTimer -= animationTiles[animationRow].length;
					animationRow++;
					animationRow %= animationTiles.length;
					if (animationRow == 0)
						animationCycleFinished = true;
				}
			}
			animationCol = (int) animationTimer;
		} else if (animationTextureRow > -1) {
			animationRow = animationTextureRow;
		}
		return animationTiles[animationRow][animationCol];
	}

	/**
	 * Different directions are represented by the rows of the picture. this
	 * method sets the row by the direction-index<br>
	 * <code>direction.getIndex(animationTiles.length)</code>
	 * 
	 * @param direction
	 * @return The TextureRegion, which represents the new direction
	 */
	public TextureRegion setAnimationPosition(Direction direction) {
		return setAnimationPosition(direction.getIndex(animationTiles.length),
				animationCol);
	}

	/**
	 * Sets an exact tile for this animation
	 * 
	 * @param row
	 * @param col
	 * @return The TextureRegion, which represents the new state of the
	 *         animation
	 */
	public TextureRegion setAnimationPosition(int row, int col) {
		if (row > -1)
			animationRow = row;
		if (col > -1)
			animationCol = col;
		return animationTiles[animationRow][animationCol];
	}

	/**
	 * This method resets the animation timer and sets the animation to the
	 * first tile of the actual row (the row represents the direction)
	 * 
	 * @return The first tile of the actual direction (=row)
	 */
	public TextureRegion stop() {
		animationTimer = 1.001f;
		animationCol = 0;
		return animationTiles[animationRow][animationCol];
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		// write tile width
		out.writeInt(animationTiles[0][0].getRegionWidth());
		// write tile height
		out.writeInt(animationTiles[0][0].getRegionHeight());
		// write amount of columns
		out.writeInt(animationTiles[0].length);
		// write amount of rows
		out.writeInt(animationTiles.length);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		setAnimationTexture(path, in.readInt(), in.readInt(), in.readInt(), in
				.readInt(), isCompressed);
	}

	public void dispose() {
		if (animationTexture != null)
			animationTexture.dispose();
	}
}
