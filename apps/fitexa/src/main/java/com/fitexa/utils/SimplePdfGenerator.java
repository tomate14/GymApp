package com.fitexa.utils;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SimplePdfGenerator {
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN_LEFT = 40;
    private static final int TOP_Y = 800;
    private static final int BOTTOM_Y = 40;
    private static final int LINE_HEIGHT = 14;
    private static final int MAX_CHARS_PER_LINE = 90;

    private SimplePdfGenerator() {
    }

    public static byte[] fromLines(List<String> lines) {
        List<String> normalizedLines = normalizeAndWrap(lines);
        List<List<String>> pages = paginate(normalizedLines);
        List<String> objects = buildObjects(pages);
        return buildPdf(objects);
    }

    private static List<String> buildObjects(List<List<String>> pages) {
        List<String> objects = new ArrayList<>();
        objects.add(null);
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add(null);
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        List<Integer> pageObjectNumbers = new ArrayList<>();
        int nextObjectNumber = 4;
        for (List<String> pageLines : pages) {
            int pageObjectNumber = nextObjectNumber;
            int contentObjectNumber = nextObjectNumber + 1;
            nextObjectNumber += 2;

            pageObjectNumbers.add(pageObjectNumber);
            ensureSize(objects, contentObjectNumber);
            objects.set(contentObjectNumber, createContentStream(pageLines));
        }

        StringBuilder kids = new StringBuilder();
        for (int index = 0; index < pageObjectNumbers.size(); index += 1) {
            if (index > 0) {
                kids.append(' ');
            }
            kids.append(pageObjectNumbers.get(index)).append(" 0 R");
        }
        objects.set(2, "<< /Type /Pages /Kids [" + kids + "] /Count " + pageObjectNumbers.size() + " >>");

        for (Integer pageObjectNumber : pageObjectNumbers) {
            int contentObjectNumber = pageObjectNumber + 1;
            ensureSize(objects, pageObjectNumber);
            objects.set(
                pageObjectNumber,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] " +
                    "/Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObjectNumber + " 0 R >>"
            );
        }

        return objects;
    }

    private static byte[] buildPdf(List<String> objects) {
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        int maxObjectNumber = objects.size() - 1;
        List<Integer> offsets = new ArrayList<>(Collections.nCopies(maxObjectNumber + 1, 0));
        int byteCount = "%PDF-1.4\n".length();

        for (int objectNumber = 1; objectNumber <= maxObjectNumber; objectNumber += 1) {
            offsets.set(objectNumber, byteCount);
            String objectPayload = objectNumber + " 0 obj\n" + objects.get(objectNumber) + "\nendobj\n";
            pdf.append(objectPayload);
            byteCount += objectPayload.length();
        }

        int xrefOffset = byteCount;
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(maxObjectNumber + 1).append('\n');
        xref.append("0000000000 65535 f \n");
        for (int objectNumber = 1; objectNumber <= maxObjectNumber; objectNumber += 1) {
            xref.append(String.format("%010d 00000 n %n", offsets.get(objectNumber)));
        }
        xref.append("trailer\n<< /Size ").append(maxObjectNumber + 1).append(" /Root 1 0 R >>\n");
        xref.append("startxref\n").append(xrefOffset).append("\n%%EOF");
        pdf.append(xref);

        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static String createContentStream(List<String> lines) {
        StringBuilder commands = new StringBuilder();
        commands.append("BT\n");
        commands.append("/F1 11 Tf\n");
        commands.append(MARGIN_LEFT).append(' ').append(TOP_Y).append(" Td\n");

        for (int index = 0; index < lines.size(); index += 1) {
            commands.append('(').append(escapePdfText(lines.get(index))).append(") Tj\n");
            if (index < lines.size() - 1) {
                commands.append("0 -").append(LINE_HEIGHT).append(" Td\n");
            }
        }

        commands.append("ET");
        String content = commands.toString();
        return "<< /Length " + content.length() + " >>\nstream\n" + content + "\nendstream";
    }

    private static List<List<String>> paginate(List<String> lines) {
        List<List<String>> pages = new ArrayList<>();
        pages.add(new ArrayList<>());

        int currentY = TOP_Y;
        for (String line : lines) {
            if (currentY < BOTTOM_Y) {
                pages.add(new ArrayList<>());
                currentY = TOP_Y;
            }
            pages.get(pages.size() - 1).add(line);
            currentY -= LINE_HEIGHT;
        }

        if (pages.get(0).isEmpty()) {
            pages.get(0).add(" ");
        }

        return pages;
    }

    private static List<String> normalizeAndWrap(List<String> lines) {
        List<String> wrapped = new ArrayList<>();
        for (String line : lines) {
            String normalized = normalizeAscii(line);
            if (normalized.trim().isEmpty()) {
                wrapped.add("");
            } else {
                wrapped.addAll(wrapLine(normalized, MAX_CHARS_PER_LINE));
            }
        }
        return wrapped;
    }

    private static List<String> wrapLine(String value, int maxChars) {
        if (value.length() <= maxChars) {
            return List.of(value);
        }

        String[] words = value.split(" ");
        List<String> lines = new ArrayList<>();
        String current = "";
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (candidate.length() <= maxChars) {
                current = candidate;
                continue;
            }

            if (!current.isEmpty()) {
                lines.add(current);
            }

            if (word.length() <= maxChars) {
                current = word;
                continue;
            }

            List<String> chunks = splitLongWord(word, maxChars);
            lines.addAll(chunks.subList(0, chunks.size() - 1));
            current = chunks.get(chunks.size() - 1);
        }

        if (!current.isEmpty()) {
            lines.add(current);
        }

        return lines.isEmpty() ? List.of("") : lines;
    }

    private static List<String> splitLongWord(String value, int size) {
        List<String> chunks = new ArrayList<>();
        for (int index = 0; index < value.length(); index += size) {
            chunks.add(value.substring(index, Math.min(index + size, value.length())));
        }
        return chunks;
    }

    private static String normalizeAscii(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replaceAll("[^\\x20-\\x7E]", " ");
    }

    private static String escapePdfText(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static void ensureSize(List<String> values, int targetIndex) {
        while (values.size() <= targetIndex) {
            values.add(null);
        }
    }
}
