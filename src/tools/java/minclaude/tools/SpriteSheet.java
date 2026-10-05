package minclaude.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Planche de tous les sprites du mod, agrandis x2 sur fond gris (comme en jeu), pour la relecture et la doc.
 * Les animations de convoyeur ne montrent que la forme 0. Usage : {@code ./gradlew spriteSheet}.
 */
public final class SpriteSheet{
    private static final int CELL = 140, SCALE = 2, COLUMNS = 8;

    public static void main(String[] args) throws IOException{
        Path root = Path.of(args[0]);
        List<Path> files;
        try(Stream<Path> s = Files.walk(root)){
            files = s.filter(p -> p.toString().endsWith(".png"))
                .filter(p -> !p.getFileName().toString().matches(".*-[1-6]-\\d\\.png") && !p.getFileName().toString().matches(".*-0-[1-3]\\.png"))
                .sorted().toList();
        }
        int rows = (files.size() + COLUMNS - 1) / COLUMNS;
        BufferedImage sheet = new BufferedImage(COLUMNS * CELL, rows * CELL, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x4a4b53));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        for(int i = 0; i < files.size(); i++){
            BufferedImage img = ImageIO.read(files.get(i).toFile());
            int x = (i % COLUMNS) * CELL, y = (i / COLUMNS) * CELL;
            int w = img.getWidth() * SCALE, h = img.getHeight() * SCALE;
            g.drawImage(img, x + (CELL - w) / 2, y + (CELL - 18 - h) / 2, w, h, null);
            g.setColor(Color.WHITE);
            String name = files.get(i).getFileName().toString().replace(".png", "");
            g.drawString(name, x + 4, y + CELL - 6);
        }
        g.dispose();
        File out = new File(args[1]);
        out.getParentFile().mkdirs();
        ImageIO.write(sheet, "png", out);
        System.out.println("planche " + out + " (" + files.size() + " sprites)");
    }
}
