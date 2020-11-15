package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class AsciiPic {
	static File densities;
	static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" +
			"`~!@#$%^&*()_+-=<>?:\"{}|,./;'[]\\``™£¢∞§¶•ªº–––≠œ∑´´†¥¨ˆˆπ“‘«åß∂ƒ©˙∆˚¬…æΩ≈ç√∫˜˜≤≥ç`" +
			"⁄€‹›ﬁ°·‚—±Œ„´Á¨Ø∏”’¨ÅÍÎÓÔÒÚÆ¸ıÂ¯";
	static ArrayList<Letter> letterData;

	public static void main(String[] args) {
		calcDensities();
		sortDensities();
		try {
			PrintStream printer = new PrintStream(args[1]);
			int width = 100;
			int height = 100;

			if (args.length > 2) {
				width = Integer.parseInt(args[2]);
				height = Integer.parseInt(args[3]);
			}
			printer.println(imageToASCII(args[0], width, height));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("INVALID ARGUMENTS\nUSAGE: java -jar <jar_file>.jar <image_to_convert> <output_file> (<width> <height>)");
		}
	}

	public static void calcDensities() {
		letterData = new ArrayList<>();

		densities = new File("densities.txt");
		
		try (Scanner read = new Scanner(densities)) {
			int num = 0;
			if (read.hasNextLine()) {
				String s = read.nextLine();
				try {
					num = Integer.parseInt(s);
				} catch (Exception exc) {
					num = 0;
				}
			}

			if (!read.hasNextLine() || num != letters.length()) {
				updateFile(densities);
				return;
			}

			String str = "";
			while (read.hasNextLine()) {
				str = read.nextLine();
				while (str.equals("") && read.hasNextLine())
					str = read.nextLine();

				letterData.add(new Letter(str.charAt(0), Integer.parseInt(str.substring(2))));
			}
		} catch (FileNotFoundException exc) {
			System.out.println("File not found");
		}

	}

	public static void updateFile(File f) {
		try (BufferedWriter write = new BufferedWriter(new FileWriter(f))) {

			BufferedImage im = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY);
			im.createGraphics();
			Graphics2D g = im.createGraphics();
			String ch;
			int gray;
			int rgb;

			g.setColor(Color.BLACK);
			g.setFont(new Font("monospace", Font.PLAIN, 16));

			write.write(letters.length() + "");
			for (int i = 0; i < letters.length(); i++) {
				ch = letters.substring(i, i + 1);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, 16, 16);
				g.setColor(Color.WHITE);
				g.drawString(ch, 0, 16);

				gray = 0;
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						rgb = im.getRGB(x, y);
						rgb = ((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + ((rgb) & 0xff);
						rgb /= 3;
						gray += rgb;
					}
				}

				gray = gray >> 6;
				gray /= 3;

				write.newLine();
				write.write(ch + " " + gray);

				if (i < letterData.size())
					letterData.set(i, new Letter(ch.charAt(0), gray));
				else
					letterData.add(i, new Letter(ch.charAt(0), gray));
			}

			write.newLine();
		} catch (IOException e1) {
			System.out.println("IO Exception");
		}
	}

	public static void sortDensities() {
		Letter l;
		for (int i = 0; i < letterData.size(); i++) {
			l = letterData.get(i);

			int j = 0;
			while (letterData.get(j).density <= l.density && j < i)
				j++;

			letterData.remove(i);
			letterData.add(j, l);
		}
	}

	public static Letter findLetter(int density) {
		int index = letterData.size() / 2;
		int start = 0;
		int end = letterData.size();

		while (start < end - 1) {
			if (density == letterData.get(index).density)
				return letterData.get(index);
			else if (density > letterData.get(index).density) {
				start = index + 1;
				index = (start + end) / 2;
			} else {
				end = index - 1;
				index = (start + end) / 2;
			}
		}

		start = Math.max(0, index - 1);
		end = Math.min(letterData.size() - 1, index + 1);

		index = Math.abs(letterData.get(end).density - density) < Math.abs(letterData.get(index).density - density)
				? end
				: index;
		index = Math.abs(letterData.get(start).density - density) < Math.abs(letterData.get(index).density - density)
				? start
				: index;

		return letterData.get(index);

	}

	public static void printLetterData() {
		for (Letter s : letterData)
			System.out.println(s);
	}

	public static String imageToASCII(String imageName, int w, int h) {
		StringBuilder ascii = new StringBuilder();

		try {
			BufferedImage image = ImageIO.read(new File(imageName));
			int imageW = image.getWidth();
			int imageH = image.getHeight();

			int x;
			int y = 0;
			w = imageW / w;
			h = imageH / h;
			int area = w * h;
			int gray;
			int rgb;

			while (y + h < imageH) {
				x = 0;
				while (x + w < imageW) {
					gray = 0;
					for (int i = x; i < x + w; i++) {
						for (int j = y; j < y + h; j++) {
							rgb = image.getRGB(i, j);
							rgb = ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + ((rgb) & 0xFF);
							rgb /= 3;
							gray += rgb;
						}
					}
					gray /= area;

					gray = 250 - (int) (gray * 1.1);

					ascii.append(findLetter(gray).letter);

					x += w;
				}

				ascii.append("\n");

				y += h;
			}

		} catch (IOException e) {
			System.out.println("Image not found");
		}

		return ascii.toString();
	}
}
