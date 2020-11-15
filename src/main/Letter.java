package main;

public class Letter {
	public char letter;
	public int density;
	
	public Letter(char letter, int density) {
		this.letter = letter;
		this.density = density;
	}
	
	public String toString() {
		return letter + " " + density;
	}
}
