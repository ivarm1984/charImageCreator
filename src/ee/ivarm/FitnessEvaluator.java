package ee.ivarm;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class FitnessEvaluator implements Runnable {

	private List<ImageData> imageDatas;

	private int start;

	private int end;

	private boolean done = false;

	private BufferedImage original;
	private RGB[][] originalValues;

	@Override
	public void run() {
		if (imageDatas == null) {
			return;
		}

		for (int i = start; i < end; i++) {
			if (imageDatas.get(i).getFitness() == null) {
				evaluateFitness(imageDatas.get(i));
			}
		}
		done = true;
	}

	private void evaluateFitness(ImageData data) {
		BufferedImage image = new BufferedImage(original.getWidth(),
				original.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setPaint(new Color(255, 255, 255));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		for (CharPreferences preferences : data.getPreferences()) {
			graphics.setFont(new Font(null, Font.PLAIN, preferences.getSize()));
			graphics.setColor(preferences.getColor());
			graphics.drawString(Character.toString(preferences.getCharacter()),
					preferences.getxStart(), preferences.getyStart());
		}
		graphics.dispose();

		int red = 0;
		int green = 0;
		int blue = 0;
		long difference = 0L;
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				int pixel = image.getRGB(i, j);
				red = (pixel >> 16) & 0xff;
				green = (pixel >> 8) & 0xff;
				blue = (pixel) & 0xff;
				RGB rgb = new RGB(red, green, blue);
				difference += originalValues[i][j].difference(rgb);
			}
		}

		data.setFitness(difference);
	}

	public void setImageDatas(List<ImageData> imageDatas) {
		this.imageDatas = imageDatas;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public void setOriginal(BufferedImage original) {
		this.original = original;
	}

	public void setOriginalValues(RGB[][] originalValues) {
		this.originalValues = originalValues;
	}

}
