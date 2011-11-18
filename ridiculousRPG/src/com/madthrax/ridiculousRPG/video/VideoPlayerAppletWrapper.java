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

package com.madthrax.ridiculousRPG.video;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.fluendo.jst.Message;
import com.madthrax.ridiculousRPG.TextureRegionLoader;
import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;

/**
 * This class wraps the Cortado video player {@link Applet} inside an
 * {@link AppletStub} and creates a frame to display the video.<br>
 * The frame will cover the game screen. It's certainly not the best solution
 * but it works. Suggestions for better solutions welcome!<br>
 * The following formats are supported:<br>
 * <ul>
 * <li>Ogg Theora</li>
 * <li>Ogg Vorbis</li>
 * <li>Mulaw audio</li>
 * <li>MJPEG</li>
 * <li>Smoke codec</li>
 * </ul>
 * 
 * @see http://www.theora.org/cortado/
 * @author Alexander Baumgartner
 */
public class VideoPlayerAppletWrapper implements AppletStub, Disposable {
	private static VideoPlayerAppletWrapper instance;
	private VideoPlayerApplet player;
	private URL url;
	private boolean playing;
	private int width, height;
	private GraphicsWrapper gw;

	/**
	 * Instantiates a new video player. Use {@link #$(URL, Rectangle, boolean)}
	 * if you don't need to play more than one video at the same time!
	 * 
	 * @param url
	 *            url to ogg / ogv file
	 * @param screenBounds
	 *            the screen position, width and height
	 * @param withAudio
	 *            if false, the audio channel will be disabled.
	 * @param autoClose
	 *            if true, the player will close automatically after reaching
	 *            the end of the stream/file or if an error occurred. BUT: You
	 *            have to dispose the player yourself!
	 * @param fullscreen
	 */
	public VideoPlayerAppletWrapper(URL url, Rectangle screenBounds,
			boolean withAudio, final boolean autoClose) {
		this.url = url;
		this.width = screenBounds.width;
		this.height = screenBounds.height;

		TextureRegionRef tRef = TextureRegionLoader.obtainEmptyRegion(width,
				height, Format.RGBA8888);
		gw = new GraphicsWrapper(tRef, screenBounds.x, screenBounds.y);
		player = new VideoPlayerApplet() {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleMessage(Message msg) {
				if (autoClose
						&& (msg.getType() == Message.EOS || msg.getType() == Message.ERROR)) {
					VideoPlayerAppletWrapper.this.stop();
				}
				super.handleMessage(msg);
			}

			@Override
			public Graphics getGraphics() {
				return gw;
			}
		};
		initPlayer();
		player.setParam("url", url.toString());
		player.setParam("audio", String.valueOf(withAudio));
		player.setSize(width, height);
		player.setStub(this);
		player.init();
		player.start();
	}

	/**
	 * Initializes the player. Override if you want a status bar, an other
	 * buffer size,...
	 */
	protected void initPlayer() {
		player.setParam("bufferSize", "200");
		player.setParam("showStatus", "hide");
		player.setParam("showSpeaker", "false");
		player.setParam("showSubtitles", "false");
		player.setParam("autoPlay", "false");
		player.setParam("debug", "0");
	}

	/**
	 * Returns the default instance. If it's the first call, the default
	 * instance will be allocated.
	 * 
	 * @param url
	 *            url to ogg / ogv file
	 * @param screenBounds
	 *            the screen position, width and height
	 * @param withAudio
	 *            if false, the audio channel will be disabled.
	 * @param autoClose
	 *            if true, the player will close automatically after reaching
	 *            the end of the stream/file or if an error occurred.
	 * @param fullscreen
	 * @return
	 */
	public static VideoPlayerAppletWrapper $(URL url, Rectangle screenBounds,
			boolean withAudio, boolean autoClose, boolean fullscreen) {
		if (instance == null) {
			instance = new VideoPlayerAppletWrapper(url, screenBounds,
					withAudio, autoClose);
		} else {
			if (!url.sameFile(instance.url)) {
				instance.changeMedia(url, withAudio, fullscreen);
			}
			instance.resize(screenBounds);
		}
		return instance;
	}

	/**
	 * Changes the media file (ogg / ogv)<br>
	 * If you change the media and resize the screen, always first change the
	 * media and than resize the screen.
	 */
	public void changeMedia(URL url, boolean withAudio, boolean fullscreen) {
		player.setParam("audio", String.valueOf(withAudio));
		player.setParam("url", url.toString());
		player.restart();
		if (playing)
			play();
	}

	/**
	 * Starts the video (and audio) playback
	 */
	public void play() {
		player.doPlay();
		playing = true;
	}

	public boolean isPlaying() {
		return playing;
	}

	/**
	 * Pauses or resumes the playback
	 */
	public void pause() {
		player.doPause();
	}

	/**
	 * Stops the video (and audio) playback
	 */
	public void stop() {
		playing = false;
		player.doStop();
	}

	/**
	 * Seeks the video file with the given amount of milliseconds
	 * 
	 * @param value
	 *            milliseconds to seek
	 */
	public void seek(double value) {
		player.doSeek(value);
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void resize(Rectangle screenBounds) {
		while (player.getWidth() != screenBounds.width
				|| player.getHeight() != screenBounds.height)
			Thread.yield();
	}

	/**
	 * Calls the applet's resize
	 * 
	 * @param width
	 * @param height
	 * @return void
	 */
	public void appletResize(int width, int height) {
		while (player.getWidth() != width || player.getHeight() != height)
			Thread.yield();
	}

	/**
	 * Returns the applet's context, which is null in this case. This is an area
	 * where more creative programming work can be done to try and provide a
	 * context
	 * 
	 * @return AppletContext Always null
	 */
	public AppletContext getAppletContext() {
		return null;
	}

	/**
	 * Returns the CodeBase. If a host parameter isn't provided in the command
	 * line arguments, the URL is based on InetAddress.getLocalHost(). The
	 * protocol is "file:"
	 * 
	 * @return URL
	 */
	public URL getCodeBase() {
		return url;
	}

	/**
	 * Returns getCodeBase
	 * 
	 * @return URL
	 */
	public URL getDocumentBase() {
		return getCodeBase();
	}

	/**
	 * Returns the corresponding command line value
	 * 
	 * @return String
	 */
	public String getParameter(String p) {
		return null;
	}

	/**
	 * Applet is always true
	 * 
	 * @return boolean True
	 */
	public boolean isActive() {
		return true;
	}

	/**
	 * Disposes the default instance. this method is automatically when the
	 * {@link MultimediaService} is disposed. Normally you don't need to call
	 * this.
	 */
	public static void dispose$() {
		if (instance != null) {
			instance.dispose();
			instance = null;
		}
	}

	@Override
	public void dispose() {
		stop();
		player.stop();
		player.destroy();
		if (player.isActive()) {
			// force exit
			final Thread current = Thread.currentThread();
			new Thread() {
				@Override
				public void run() {
					do
						try {
							sleep(2000);
						} catch (InterruptedException e) {
						}
					while (current.isAlive());
					System.exit(0);
				}
			}.start();
		}
	}

	public void draw(SpriteBatch spriteBatch, boolean debug) {
		gw.draw(spriteBatch);
	}

	public class GraphicsWrapper extends Graphics2D implements Runnable,
			Disposable {
		Pixmap data;
		TextureRegionRef tRef;
		BufferedImage rasterImage;
		Graphics rasterImageGraphics;
		IntBuffer rasterImageData;
		int x, y;
		int width, height;
		boolean running = true;

		public GraphicsWrapper(TextureRegionRef tRef, int x, int y) {
			resize(tRef, y, y);
			new Thread(this).start();
		}

		public void resize(TextureRegionRef tRef, int x, int y) {
			if (this.tRef != null)
				this.tRef.dispose();
			this.tRef = tRef;
			this.width = tRef.getRegionWidth();
			this.height = tRef.getRegionHeight();
			this.data = new Pixmap(width, height, Format.RGBA8888);
			WritableRaster raster = Raster.createInterleavedRaster(
					DataBuffer.TYPE_BYTE, width, height, 4, null);
			ComponentColorModel colorModel = new ComponentColorModel(ColorSpace
					.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 },
					true, false, ComponentColorModel.TRANSLUCENT,
					DataBuffer.TYPE_BYTE);
			rasterImage = new BufferedImage(colorModel, raster, false, null);
			rasterImageGraphics = rasterImage.createGraphics();
			DataBufferByte rawData = (DataBufferByte) raster.getDataBuffer();
			rasterImageData = ByteBuffer.wrap(rawData.getData()).asIntBuffer();
		}

		@Override
		public void run() {
			while (running) {
				Thread.yield();
				if (playing) {
					for (int j = 0; j < height; j++) {
						int rowIndex = j * width;
						int yOffset = y + j;
						for (int i = 0; i < width; i++) {
							data.drawPixel(x + i, yOffset, rasterImageData
									.get(rowIndex + i));
						}
					}
				} else {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
		}

		@Override
		public boolean drawImage(Image img, int x, int y, int width,
				int height, ImageObserver observer) {
			Thread.yield();
			return rasterImageGraphics.drawImage(img, x, y, width, height,
					observer);
		}

		public void draw(SpriteBatch spriteBatch) {
			tRef.draw(data);
			spriteBatch.draw(tRef, x, y);
		}

		@Override
		public void dispose() {
			tRef.dispose();
			rasterImageGraphics.dispose();
			running = false;
		}

		@Override
		public void addRenderingHints(Map<?, ?> hints) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void clip(Shape s) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("clip");
		}

		@Override
		public void draw(Shape s) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(Shape)");
		}

		@Override
		public void drawGlyphVector(GlyphVector g, float x, float y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(gv)");
		}

		@Override
		public boolean drawImage(Image img, AffineTransform xform,
				ImageObserver obs) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(img)");
			return false;
		}

		@Override
		public void drawImage(BufferedImage img, BufferedImageOp op, int x,
				int y) {
			System.out.println("draw(buffimg)");
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void drawRenderableImage(RenderableImage img,
				AffineTransform xform) {
			System.out.println("draw(rimg)");
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(rimg++)");
		}

		@Override
		public void drawString(String str, int x, int y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(stirn)");
		}

		@Override
		public void drawString(String str, float x, float y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(stirn)");
		}

		@Override
		public void drawString(AttributedCharacterIterator iterator, int x,
				int y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(stirn)");
		}

		@Override
		public void drawString(AttributedCharacterIterator iterator, float x,
				float y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("draw(stirn)");
		}

		@Override
		public void fill(Shape s) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("fillshape");
		}

		@Override
		public Color getBackground() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("getbackground");
			return null;
		}

		@Override
		public Composite getComposite() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("getCompo");
			return null;
		}

		@Override
		public GraphicsConfiguration getDeviceConfiguration() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("getDConf");
			return null;
		}

		@Override
		public FontRenderContext getFontRenderContext() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("getFrend");
			return null;
		}

		@Override
		public Paint getPaint() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("getPaint");
			return null;
		}

		@Override
		public Object getRenderingHint(Key hintKey) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("gethint");
			return null;
		}

		@Override
		public RenderingHints getRenderingHints() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("gethint");
			return null;
		}

		@Override
		public Stroke getStroke() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("getstrok");
			return null;
		}

		@Override
		public AffineTransform getTransform() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			System.out.println("gettransform");
			return null;
		}

		@Override
		public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return false;
		}

		@Override
		public void rotate(double theta) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void rotate(double theta, double x, double y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void scale(double sx, double sy) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setBackground(Color color) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setComposite(Composite comp) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setPaint(Paint paint) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setRenderingHint(Key hintKey, Object hintValue) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setRenderingHints(Map<?, ?> hints) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setStroke(Stroke s) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setTransform(AffineTransform Tx) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void shear(double shx, double shy) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void transform(AffineTransform Tx) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void translate(int x, int y) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void translate(double tx, double ty) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void clearRect(int x, int y, int width, int height) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void clipRect(int x, int y, int width, int height) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void copyArea(int x, int y, int width, int height, int dx, int dy) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public Graphics create() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return null;
		}

		@Override
		public void drawArc(int x, int y, int width, int height,
				int startAngle, int arcAngle) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return false;
		}

		@Override
		public boolean drawImage(Image img, int x, int y, Color bgcolor,
				ImageObserver observer) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return false;
		}

		@Override
		public boolean drawImage(Image img, int x, int y, int width,
				int height, Color bgcolor, ImageObserver observer) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return false;
		}

		@Override
		public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return false;
		}

		@Override
		public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
				int sx1, int sy1, int sx2, int sy2, Color bgcolor,
				ImageObserver observer) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return false;
		}

		@Override
		public void drawLine(int x1, int y1, int x2, int y2) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void drawOval(int x, int y, int width, int height) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void drawRoundRect(int x, int y, int width, int height,
				int arcWidth, int arcHeight) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void fillArc(int x, int y, int width, int height,
				int startAngle, int arcAngle) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void fillOval(int x, int y, int width, int height) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void fillRect(int x, int y, int width, int height) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void fillRoundRect(int x, int y, int width, int height,
				int arcWidth, int arcHeight) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public Shape getClip() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return null;
		}

		@Override
		public Rectangle getClipBounds() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return null;
		}

		@Override
		public Color getColor() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return null;
		}

		@Override
		public Font getFont() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return null;
		}

		@Override
		public FontMetrics getFontMetrics(Font f) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
			return null;
		}

		@Override
		public void setClip(Shape clip) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setClip(int x, int y, int width, int height) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setColor(Color c) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setFont(Font font) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setPaintMode() {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

		@Override
		public void setXORMode(Color c1) {
			System.out.println("##################################" + "\n"
					+ Thread.currentThread().getStackTrace()[1]
					+ "\nNOT IMPLEMENTED"
					+ "\n####################################");
		}

	}
}
