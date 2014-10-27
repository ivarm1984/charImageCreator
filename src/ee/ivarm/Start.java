package ee.ivarm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Start {

	private static final String ORIGINAL_IMAGE = "hello.png";
	private static final String IMAGE_SAVE_LOCATION = "/tmp/genetic/hello/";
	private static final int POOL_SIZE = 500;
	private static final String ALPHABET = "1234567890abcdefghijklmnopqrstuvõäöüxywz";
	private static final int CHARS_COUNT = 1500;
	private static final int ELITE_COUNT = 10;
	private static final int GENERATIONS = 999999;
	private static final int MUTATION_COUNT = 1;
	private static final int MAX_FONT_SIZE = 30;
	private static final int THREADS = 3;
	private static final boolean SHOW_UI = true;

	private static BufferedImage original;
	private static Random random = new Random();
	private static RGB[][] originalValues;

	private static List<FitnessEvaluator> evaluators;

	private static List<ImageData> pool;

	public static void main(String[] args) {
		// read in image
		readOriginalImage();
		// get image values
		readOriginalPixelValues();

		ImagePanel panel = null;
		if (SHOW_UI) {
			panel = new ImagePanel(original);
			JFrame frame = new JFrame();
			frame.setPreferredSize(new Dimension(original.getWidth() * 2 + 25,
					original.getHeight() + 40));
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}

		// init pool
		initPool();
		initThreads();
		for (int i = 0; i < GENERATIONS; i++) {
			// evaluate fitness
			evaluateFitness(pool);
			Collections.sort(pool);
			// save generation best
			BufferedImage best = getBufferedImage(pool.get(0));
			if (SHOW_UI) {
				panel.setImage(best);
			}
			double fitness = 100 - 100.0 / 765 * (1.0
					* pool.get(0).getFitness() / original.getWidth() / original
					.getHeight());
			System.out.printf(
					"Generation: %s best fitness: %s%% chars: %s %s\n", i + 1,
					fitness, CHARS_COUNT, ORIGINAL_IMAGE);
			if (SHOW_UI) {
				panel.repaint();
			}
			saveImage(best, i + 1, fitness);
			// throw away bad results and substitute with children
			pool = mateBest(pool);
		}

	}

	private static void saveImage(BufferedImage best, int generation,
			double fitness) {
		try {
			File file = new File(IMAGE_SAVE_LOCATION + generation + ".png");
			new File(IMAGE_SAVE_LOCATION).mkdir();
			ImageIO.write(best, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<ImageData> mateBest(List<ImageData> pool) {
		List<ImageData> newPool = new ArrayList<>();
		for (int i = 0; i < pool.size(); i++) {
			if (i < ELITE_COUNT) {
				newPool.add(pool.get(i));
			} else {
				newPool.add(mate(newPool));
			}
		}
		return newPool;
	}

	private static ImageData mate(List<ImageData> newPool) {
		// get random 2 from elite
		int first = random.nextInt(ELITE_COUNT);
		int second = random.nextInt(ELITE_COUNT);
		while (first == second) {
			second = random.nextInt(ELITE_COUNT);
		}
		// mate the two
		return mate(newPool.get(first), newPool.get(second));
	}

	private static ImageData mate(ImageData first, ImageData second) {
		List<CharPreferences> preferences = new ArrayList<>();
		for (int i = 0; i < CHARS_COUNT; i++) {
			switch (random.nextInt(2)) {
			case 0:
				preferences.add(new CharPreferences(first.getPreferences().get(
						i)));
				break;
			case 1:
				preferences.add(new CharPreferences(second.getPreferences()
						.get(i)));
				break;
			}
		}

		for (int i = 0; i < MUTATION_COUNT; i++) {
			mutate(preferences);
		}
		return new ImageData(preferences);
	}

	private static void mutate(List<CharPreferences> preferences) {
		int location = random.nextInt(preferences.size());
		CharPreferences prefs = preferences.get(location);

		switch (random.nextInt(4)) {
		case 0:
			// remove one and add new prefs to the end
			prefs = getRandomPreferences();
			preferences.remove(location);
			preferences.add(prefs);
			break;
		case 1:
			// swap two
			Collections.swap(preferences, random.nextInt(preferences.size()),
					location);
			break;
		case 2:
			// change ones color
			prefs.setColor(getRandomColor());
			preferences.set(location, prefs);
			break;
		case 3:
			// replace one with new prefs
			prefs = getRandomPreferences();
			preferences.set(location, prefs);
			break;
		}

	}

	private static void evaluateFitness(List<ImageData> pool) {
		// split between threads
		int forOneThread = pool.size() / THREADS;
		for (int i = 0; i < THREADS; i++) {
			FitnessEvaluator evaluator = evaluators.get(i);
			evaluator.setStart(i * forOneThread);
			evaluator.setEnd((i + 1) * forOneThread + 1);
			evaluator.setImageDatas(pool);
			if (i + 1 == THREADS) {
				evaluator.setEnd(pool.size());
			}
			new Thread(evaluator).start();
		}

		// see if all done
		while (!evaluatorsDone()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private static boolean evaluatorsDone() {
		for (FitnessEvaluator evaluator : evaluators) {
			if (!evaluator.isDone()) {
				return false;
			}
		}
		resetEvaluators();
		return true;
	}

	private static void resetEvaluators() {
		for (FitnessEvaluator evaluator : evaluators) {
			evaluator.setDone(false);
		}
	}

	private static void initThreads() {
		evaluators = new ArrayList<>();
		for (int i = 0; i < THREADS; i++) {
			FitnessEvaluator evaluator = new FitnessEvaluator();
			evaluator.setImageDatas(pool);
			evaluator.setOriginal(original);
			evaluator.setOriginalValues(originalValues);
			evaluators.add(evaluator);
		}
	}

	private static BufferedImage getBufferedImage(ImageData imageData) {
		BufferedImage image = new BufferedImage(original.getWidth(),
				original.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setPaint(new Color(255, 255, 255));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		for (CharPreferences preferences : imageData.getPreferences()) {
			graphics.setFont(new Font(null, Font.PLAIN, preferences.getSize()));
			graphics.setColor(preferences.getColor());
			graphics.drawString(Character.toString(preferences.getCharacter()),
					preferences.getxStart(), preferences.getyStart());
		}
		graphics.dispose();
		return image;
	}

	private static void initPool() {
		List<ImageData> imageDatas = new ArrayList<>();
		for (int i = 0; i < POOL_SIZE; i++) {
			List<CharPreferences> preferences = new ArrayList<>();
			for (int j = 0; j < CHARS_COUNT; j++) {
				preferences.add(getRandomPreferences());
			}
			imageDatas.add(new ImageData(preferences));
		}
		pool = imageDatas;
	}

	private static CharPreferences getRandomPreferences() {
		CharPreferences preferences = new CharPreferences();
		preferences.setCharacter(getRandomCharacter());
		preferences.setColor(getRandomColor());
		preferences.setSize(random.nextInt(MAX_FONT_SIZE));
		preferences.setxStart(random.nextInt(original.getWidth()));
		preferences.setyStart(random.nextInt(original.getHeight()));
		return preferences;
	}

	private static char getRandomCharacter() {
		return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
	}

	private static void readOriginalPixelValues() {
		originalValues = new RGB[original.getWidth()][original.getHeight()];

		// add values
		int red = 0;
		int green = 0;
		int blue = 0;
		for (int i = 0; i < original.getWidth(); i++) {
			for (int j = 0; j < original.getHeight(); j++) {
				int pixel = original.getRGB(i, j);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;
				originalValues[i][j] = new RGB(red, green, blue);
			}
		}
	}

	private static void readOriginalImage() {
		try {
			// Read from a file
			File file = new File(ORIGINAL_IMAGE);
			original = ImageIO.read(file);

		} catch (IOException e) {
			System.out.println("Unable to read original image");
		}

	}

	private static Color getRandomColor() {
		return new Color(random.nextInt(255), random.nextInt(255),
				random.nextInt(255));
	}
}
