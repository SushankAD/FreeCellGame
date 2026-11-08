import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

final class CardArt {
	private static final String[] SUIT_CODES = {"C", "D", "H", "S"};
	private static final BufferedImage[][] IMAGES = new BufferedImage[13][4];

	private CardArt() {
	}

	static synchronized BufferedImage get(int rank, int suit) {
		if (rank < 0 || rank >= IMAGES.length || suit < 0 || suit >= SUIT_CODES.length) {
			throw new IllegalArgumentException("Invalid card rank or suit.");
		}
		if (IMAGES[rank][suit] == null) {
			IMAGES[rank][suit] = loadImage(Card.getRankName(rank) + SUIT_CODES[suit] + ".png");
		}
		return IMAGES[rank][suit];
	}

	private static BufferedImage loadImage(String filename) {
		String resourceName = "/assets/cards/" + filename;
		InputStream stream = CardArt.class.getResourceAsStream(resourceName);
		try {
			if (stream == null) {
				Path file = Paths.get("assets", "cards", filename);
				if (!Files.isRegularFile(file)) {
					throw new IllegalStateException("Missing card image: " + file.toAbsolutePath());
				}
				stream = Files.newInputStream(file);
			}
			try (InputStream imageStream = stream) {
				BufferedImage image = ImageIO.read(imageStream);
				if (image == null) {
					throw new IllegalStateException("Unreadable card image: " + filename);
				}
				return image;
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not load card image: " + filename, e);
		}
	}
}
