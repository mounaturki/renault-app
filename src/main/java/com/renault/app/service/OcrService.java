package com.renault.app.service;

import com.renault.app.model.PlateType;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    @Value("${tesseract.datapath}")
    private String tessDataPath;

    private ITesseract tesseract;

    private static final Pattern TUNISIAN_PATTERN = Pattern.compile(
            "(\\d{1,4})[\\s\\S]{1,15}?(\\d{1,4})"
    );
    private static final Pattern FOREIGN_PATTERN = Pattern.compile(
            "([A-Za-z]{2})[^A-Za-z0-9]*(\\d{2,3})[^A-Za-z0-9]*([A-Za-z]{2})",
            Pattern.CASE_INSENSITIVE
    );
    @PostConstruct
    public void init() {
        tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("fra+eng");
        tesseract.setPageSegMode(7);
        tesseract.setOcrEngineMode(1);
    }

    public PlateScanResult scanPlate(MultipartFile imageFile, PlateType expectedType) throws Exception {
        Path tempPath = Files.createTempFile("plate_", ".jpg");
        imageFile.transferTo(tempPath.toFile());

        try {
// Preprocessing avec OpenCV
            BufferedImage preprocessed = preprocessImage(tempPath.toString());
            javax.imageio.ImageIO.write(preprocessed, "jpg",
                    new java.io.File("C:/Users/MSI/debug_plate.jpg"));

// OCR sur image prétraitée
            if (expectedType == PlateType.TUNISIAN) {
                tesseract.setLanguage("ara+fra+eng");
            } else {
                tesseract.setLanguage("fra+eng");
            }
            String rawText = tesseract.doOCR(preprocessed);
            System.out.println("TEXTE BRUT OCR: [" + rawText + "]");

            PlateScanResult result = (expectedType == PlateType.TUNISIAN)
                    ? extractTunisianPlate(rawText)
                    : extractForeignPlate(rawText);

            if (!result.isValid()) {
                PlateScanResult fallback = (expectedType == PlateType.TUNISIAN)
                        ? extractForeignPlate(rawText)
                        : extractTunisianPlate(rawText);
                if (fallback.isValid()) result = fallback;
            }

            result.setConfidence(calculateConfidence(rawText, result));
            result.setRawText(rawText);
            return result;

        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    private BufferedImage preprocessImage(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) throw new RuntimeException("Impossible de charger l'image");

        if (image.width() < 800) {
            Mat resized = new Mat();
            double scale = 1200.0 / image.width();
            Imgproc.resize(image, resized, new Size(), scale, scale, Imgproc.INTER_CUBIC);
            image = resized;
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);


        Scalar meanVal = Core.mean(gray);
        System.out.println("Luminosité moyenne: " + meanVal.val[0]); // ← pour debug
        if (meanVal.val[0] < 127) {
            Core.bitwise_not(gray, gray);
        }


        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255,
                Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        return matToBufferedImage(binary);
    }

    private PlateScanResult extractTunisianPlate(String rawText) {

        String cleaned = rawText
                .replaceAll("[\\[\\]\\n\\r]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        System.out.println("TEXTE NETTOYÉ: [" + cleaned + "]");

        Matcher matcher = TUNISIAN_PATTERN.matcher(cleaned);

        if (matcher.find()) {
            String number1 = matcher.group(1);
            String number2 = matcher.group(2);
            // Format tunisien : 199 TN 199
            String formatted = String.format("%s TN %s", number1, number2);

            return PlateScanResult.builder()
                    .plateNumber(formatted)
                    .plateType(PlateType.TUNISIAN)
                    .regionCode("TN")
                    .governorateCode("TN")
                    .build();
        }
        return PlateScanResult.empty();
    }

    private PlateScanResult extractForeignPlate(String rawText) {
        String cleaned = rawText.toUpperCase()
                .replaceAll("[^A-Z0-9\\-\\s>|]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Matcher matcher = FOREIGN_PATTERN.matcher(cleaned);

        if (matcher.find()) {
            String part1 = matcher.group(1).toUpperCase();
            String numbers = matcher.group(2);
            String part2 = matcher.group(3).toUpperCase();
            String formatted = String.format("%s-%s-%s", part1, numbers, part2);

            return PlateScanResult.builder()
                    .plateNumber(formatted)
                    .plateType(PlateType.FOREIGN)
                    .build();
        }
        return PlateScanResult.empty();
    }

    private String cleanText(String text) {
        return text.toUpperCase()
                .replaceAll("[^A-Z0-9\\s-]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double calculateConfidence(String rawText, PlateScanResult result) {
        if (!result.isValid()) return 0.0;

        // Pour plaque tunisienne — chercher juste les chiffres
        if (result.getPlateType() == PlateType.TUNISIAN) {
            String number1 = result.getPlateNumber().split(" ")[0];
            String number2 = result.getPlateNumber().split(" ")[2];
            return rawText.contains(number1) && rawText.contains(number2)
                    ? 90.0 : 70.0;
        }

        String cleaned = cleanText(rawText).replaceAll("[\\s-]", "");
        String plate = result.getPlateNumber().replaceAll("[\\s-]", "");
        return cleaned.contains(plate) ? 85.0 + (plate.length() * 2) : 50.0;
    }
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width(), height = mat.height();
        int channels = mat.channels();
        byte[] pixels = new byte[width * height * channels];
        mat.get(0, 0, pixels);

        BufferedImage image;
        if (channels == 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            image.getRaster().setDataElements(0, 0, width, height, pixels);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            image.getRaster().setDataElements(0, 0, width, height, pixels);
        }
        return image;
    }

    // ========== CLASSE RÉSULTAT CORRIGÉE ==========
    public static class PlateScanResult {
        private String plateNumber;
        private PlateType plateType;
        private String regionCode;
        private String governorateCode;
        private double confidence;
        private String rawText;
        private boolean valid;

        public static PlateScanResult builder() { return new PlateScanResult(); }

        public static PlateScanResult empty() {
            PlateScanResult r = new PlateScanResult();
            r.valid = false;
            return r;
        }

        public PlateScanResult plateNumber(String p) { this.plateNumber = p; return this; }
        public PlateScanResult plateType(PlateType t) { this.plateType = t; this.valid = true; return this; }
        public PlateScanResult regionCode(String r) { this.regionCode = r; return this; }
        public PlateScanResult governorateCode(String g) { this.governorateCode = g; return this; }

        public PlateScanResult build() { return this; }

        public void setConfidence(double c) { this.confidence = c; }
        public void setRawText(String r) { this.rawText = r; }

        public String getPlateNumber() { return plateNumber; }
        public PlateType getPlateType() { return plateType; }
        public double getConfidence() { return confidence; }
        public String getRawText() { return rawText; }
        public boolean isValid() { return valid; }
        public String getRegionCode() { return regionCode; }
        public String getGovernorateCode() { return governorateCode; }
    }

}