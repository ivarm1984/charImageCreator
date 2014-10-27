package ee.ivarm;

import java.awt.Color;

public class CharPreferences implements Comparable<CharPreferences> {

	private char character;

	private int size;

	private Color color;

	private int xStart;

	private int yStart;

	private Long fitness;

	public CharPreferences(CharPreferences charPreferences) {
		this.character = charPreferences.getCharacter();
		this.size = charPreferences.getSize();
		this.color = new Color(charPreferences.getColor().getRed(),
				charPreferences.getColor().getGreen(), charPreferences
						.getColor().getBlue());
		this.xStart = charPreferences.getxStart();
		this.yStart = charPreferences.getyStart();
	}

	public CharPreferences() {
	}

	public Long getFitness() {
		return fitness;
	}

	public void setFitness(Long fitness) {
		this.fitness = fitness;
	}

	public char getCharacter() {
		return character;
	}

	public void setCharacter(char character) {
		this.character = character;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getxStart() {
		return xStart;
	}

	public void setxStart(int xStart) {
		this.xStart = xStart;
	}

	public int getyStart() {
		return yStart;
	}

	public void setyStart(int yStart) {
		this.yStart = yStart;
	}

	@Override
	public int compareTo(CharPreferences o) {
		return fitness.compareTo(o.getFitness());
	}

}
