package com.fitexa.services.impl;

import com.fitexa.dtos.RoutineAssignmentExerciseResponseDto;
import com.fitexa.dtos.RoutineAssignmentResponseDto;
import com.fitexa.entities.PrescriptionMode;
import com.fitexa.services.RoutineAssignmentPdfService;
import com.fitexa.services.RoutineAssignmentService;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineAssignmentPdfServiceImpl implements RoutineAssignmentPdfService {
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 36f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final float BOTTOM_LIMIT = 55f;

    private static final PDFont FONT_REGULAR = PDType1Font.HELVETICA;
    private static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;

    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_PRIMARY = new Color(255, 95, 31);
    private static final Color COLOR_PRIMARY_DARK = new Color(222, 78, 21);
    private static final Color COLOR_TEXT = new Color(30, 35, 45);
    private static final Color COLOR_MUTED = new Color(95, 105, 120);
    private static final Color COLOR_CARD = new Color(255, 255, 255);
    private static final Color COLOR_TABLE_HEADER = new Color(33, 37, 43);
    private static final Color COLOR_ROW_ODD = new Color(255, 255, 255);
    private static final Color COLOR_ROW_EVEN = new Color(243, 246, 250);
    private static final Color COLOR_BORDER = new Color(225, 229, 235);

    private final RoutineAssignmentService routineAssignmentService;

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long assignmentId) {
        RoutineAssignmentResponseDto assignment = routineAssignmentService.findById(assignmentId);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfContext context = newPage(document);
            float y = drawHeader(context, assignment);
            y = drawInfoCards(context, assignment, y - 14f);
            y = drawExercisesTable(document, context, assignment.getExercisePlans(), y - 20f);
            drawFooter(context, y - 18f);
            context.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de rutina", ex);
        }
    }

    private float drawHeader(PdfContext context, RoutineAssignmentResponseDto assignment) throws IOException {
        float headerHeight = 112f;
        float topY = PAGE_HEIGHT - MARGIN;
        drawRoundedRect(context.stream, MARGIN, topY - headerHeight, CONTENT_WIDTH, headerHeight, COLOR_PRIMARY, COLOR_PRIMARY_DARK);

        drawText(context.stream, FONT_BOLD, 23, Color.WHITE, "F I T E X A", MARGIN + 20, topY - 34);
        drawText(context.stream, FONT_BOLD, 14, Color.WHITE, "Plan de Entrenamiento Personalizado", MARGIN + 20, topY - 57);
        drawText(
            context.stream,
            FONT_REGULAR,
            11,
            new Color(255, 235, 220),
            "Asignacion #" + assignment.getId() + "  |  Generado: " + LocalDate.now(),
            MARGIN + 20,
            topY - 79
        );
        return topY - headerHeight;
    }

    private float drawInfoCards(PdfContext context, RoutineAssignmentResponseDto assignment, float startY) throws IOException {
        float gap = 12f;
        float cardHeight = 96f;
        float cardWidth = (CONTENT_WIDTH - gap) / 2f;

        drawCard(context.stream, MARGIN, startY - cardHeight, cardWidth, cardHeight);
        drawCard(context.stream, MARGIN + cardWidth + gap, startY - cardHeight, cardWidth, cardHeight);

        float leftX = MARGIN + 14f;
        float rightX = MARGIN + cardWidth + gap + 14f;
        drawText(context.stream, FONT_BOLD, 11, COLOR_PRIMARY_DARK, "ALUMNO", leftX, startY - 20);
        drawText(context.stream, FONT_REGULAR, 10.5f, COLOR_TEXT, safeText(assignment.getUserName()), leftX, startY - 38);
        drawText(context.stream, FONT_BOLD, 11, COLOR_PRIMARY_DARK, "RUTINA", leftX, startY - 56);
        drawText(context.stream, FONT_REGULAR, 10.5f, COLOR_TEXT, safeText(assignment.getRoutineTemplateName()), leftX, startY - 74);

        drawText(context.stream, FONT_BOLD, 11, COLOR_PRIMARY_DARK, "VIGENCIA", rightX, startY - 20);
        drawText(
            context.stream,
            FONT_REGULAR,
            10.5f,
            COLOR_TEXT,
            assignment.getStartDate() + "  a  " + assignment.getEndDate(),
            rightX,
            startY - 38
        );
        drawText(context.stream, FONT_BOLD, 11, COLOR_PRIMARY_DARK, "DIAS", rightX, startY - 56);
        drawText(context.stream, FONT_REGULAR, 10.5f, COLOR_TEXT, formatDays(assignment), rightX, startY - 74);

        return startY - cardHeight;
    }

    private float drawExercisesTable(
        PDDocument document,
        PdfContext context,
        List<RoutineAssignmentExerciseResponseDto> exercises,
        float startY
    ) throws IOException {
        float y = startY;
        drawText(context.stream, FONT_BOLD, 13, COLOR_TEXT, "Detalle de la rutina", MARGIN, y);
        y -= 18f;

        TableConfig table = new TableConfig();
        y = drawTableHeader(context.stream, table, y);

        int rowIndex = 0;
        for (RoutineAssignmentExerciseResponseDto exercise : exercises) {
            List<String> wrappedName = wrapByWords(safeText(exercise.getExerciseName()), 29);
            float rowHeight = Math.max(26f, (wrappedName.size() * 11f) + 10f);

            if (y - rowHeight < BOTTOM_LIMIT) {
                context.close();
                context = newPage(document);
                y = PAGE_HEIGHT - MARGIN;
                drawText(context.stream, FONT_BOLD, 12, COLOR_MUTED, "Detalle de la rutina (continuacion)", MARGIN, y);
                y -= 18f;
                y = drawTableHeader(context.stream, table, y);
            }

            Color rowColor = rowIndex % 2 == 0 ? COLOR_ROW_ODD : COLOR_ROW_EVEN;
            fillRect(context.stream, MARGIN, y - rowHeight, CONTENT_WIDTH, rowHeight, rowColor);
            strokeRect(context.stream, MARGIN, y - rowHeight, CONTENT_WIDTH, rowHeight, COLOR_BORDER);

            drawCellText(context.stream, FONT_BOLD, 10, COLOR_TEXT, String.valueOf(rowIndex + 1), table.colX(0), y - 16);
            drawWrappedText(context.stream, FONT_REGULAR, 10, COLOR_TEXT, wrappedName, table.colX(1), y - 16, 11f);
            drawCellText(context.stream, FONT_REGULAR, 10, COLOR_TEXT, String.valueOf(safeInt(exercise.getSets())), table.colX(2), y - 16);
            drawCellText(context.stream, FONT_REGULAR, 10, COLOR_TEXT, modeLabel(exercise.getPrescriptionMode()), table.colX(3), y - 16);
            drawCellText(context.stream, FONT_REGULAR, 10, COLOR_TEXT, repsLabel(exercise), table.colX(4), y - 16);
            drawCellText(context.stream, FONT_REGULAR, 10, COLOR_TEXT, weightLabel(exercise), table.colX(5), y - 16);

            y -= rowHeight;
            rowIndex += 1;
        }

        return y;
    }

    private float drawTableHeader(PDPageContentStream stream, TableConfig table, float y) throws IOException {
        fillRect(stream, MARGIN, y - 24f, CONTENT_WIDTH, 24f, COLOR_TABLE_HEADER);
        drawCellText(stream, FONT_BOLD, 10, Color.WHITE, "#", table.colX(0), y - 16);
        drawCellText(stream, FONT_BOLD, 10, Color.WHITE, "Ejercicio", table.colX(1), y - 16);
        drawCellText(stream, FONT_BOLD, 10, Color.WHITE, "Series", table.colX(2), y - 16);
        drawCellText(stream, FONT_BOLD, 10, Color.WHITE, "Modo", table.colX(3), y - 16);
        drawCellText(stream, FONT_BOLD, 10, Color.WHITE, "Repeticiones", table.colX(4), y - 16);
        drawCellText(stream, FONT_BOLD, 10, Color.WHITE, "Peso (kg)", table.colX(5), y - 16);
        strokeRect(stream, MARGIN, y - 24f, CONTENT_WIDTH, 24f, COLOR_TABLE_HEADER);
        return y - 24f;
    }

    private void drawFooter(PdfContext context, float y) throws IOException {
        float footerY = Math.max(y, BOTTOM_LIMIT - 4f);
        drawText(context.stream, FONT_BOLD, 11, COLOR_PRIMARY_DARK, "Hoy mejor que ayer.", MARGIN, footerY);
        drawText(
            context.stream,
            FONT_REGULAR,
            9.5f,
            COLOR_MUTED,
            "Segui esta rutina con constancia y registra tus progresos en cada sesion.",
            MARGIN,
            footerY - 14
        );
    }

    private String formatDays(RoutineAssignmentResponseDto assignment) {
        if (assignment.getDays() == null || assignment.getDays().isEmpty()) {
            return "-";
        }
        return assignment.getDays().stream()
            .map(day -> day.getValue())
            .toList()
            .toString()
            .replace("[", "")
            .replace("]", "");
    }

    private String repsLabel(RoutineAssignmentExerciseResponseDto exercise) {
        if (exercise.getPrescriptionMode() == PrescriptionMode.FIJO) {
            return String.valueOf(safeInt(exercise.getReps()));
        }
        return safeInt(exercise.getMinReps()) + " - " + safeInt(exercise.getMaxReps());
    }

    private String weightLabel(RoutineAssignmentExerciseResponseDto exercise) {
        if (exercise.getPrescriptionMode() == PrescriptionMode.FIJO) {
            return String.valueOf(safeDouble(exercise.getWeight()));
        }
        return safeDouble(exercise.getMinWeight()) + " - " + safeDouble(exercise.getMaxWeight());
    }

    private String modeLabel(PrescriptionMode mode) {
        if (mode == null) {
            return "N/A";
        }
        return "fijo".equalsIgnoreCase(mode.getValue()) ? "Fijo" : "Dinamico";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double safeDouble(Double value) {
        return value == null ? 0d : value;
    }

    private String safeText(String value) {
        return value == null ? "-" : value;
    }

    private void drawCard(PDPageContentStream stream, float x, float y, float width, float height) throws IOException {
        fillRect(stream, x, y, width, height, COLOR_CARD);
        strokeRect(stream, x, y, width, height, COLOR_BORDER);
    }

    private void drawRoundedRect(
        PDPageContentStream stream,
        float x,
        float y,
        float width,
        float height,
        Color topColor,
        Color bottomColor
    ) throws IOException {
        fillRect(stream, x, y, width, height, topColor);
        fillRect(stream, x, y, width, height / 2f, bottomColor);
    }

    private void drawWrappedText(
        PDPageContentStream stream,
        PDFont font,
        float fontSize,
        Color color,
        List<String> lines,
        float x,
        float y,
        float lineHeight
    ) throws IOException {
        float currentY = y;
        for (String line : lines) {
            drawText(stream, font, fontSize, color, line, x, currentY);
            currentY -= lineHeight;
        }
    }

    private void drawCellText(PDPageContentStream stream, PDFont font, float fontSize, Color color, String text, float x, float y)
        throws IOException {
        drawText(stream, font, fontSize, color, text, x, y);
    }

    private void drawText(PDPageContentStream stream, PDFont font, float fontSize, Color color, String text, float x, float y)
        throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(cleanText(text));
        stream.endText();
    }

    private void fillRect(PDPageContentStream stream, float x, float y, float width, float height, Color color) throws IOException {
        stream.setNonStrokingColor(color);
        stream.addRect(x, y, width, height);
        stream.fill();
    }

    private void strokeRect(PDPageContentStream stream, float x, float y, float width, float height, Color color) throws IOException {
        stream.setStrokingColor(color);
        stream.addRect(x, y, width, height);
        stream.stroke();
    }

    private String cleanText(String value) {
        return value
            .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
            .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
            .replace("ñ", "n").replace("Ñ", "N")
            .replace("–", "-");
    }

    private List<String> wrapByWords(String value, int maxChars) {
        if (value.length() <= maxChars) {
            return List.of(value);
        }
        String[] words = value.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (candidate.length() <= maxChars) {
                current.setLength(0);
                current.append(candidate);
            } else {
                if (current.length() > 0) {
                    lines.add(current.toString());
                }
                current.setLength(0);
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private PdfContext newPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        fillRect(stream, 0, 0, PAGE_WIDTH, PAGE_HEIGHT, COLOR_BG);
        return new PdfContext(page, stream);
    }

    private static final class TableConfig {
        private final float[] widths = {24f, 206f, 52f, 62f, 95f, 76f};

        float colX(int index) {
            float value = MARGIN + 8f;
            for (int current = 0; current < index; current += 1) {
                value += widths[current];
            }
            return value;
        }
    }

    private static final class PdfContext {
        private final PDPage page;
        private final PDPageContentStream stream;

        private PdfContext(PDPage page, PDPageContentStream stream) {
            this.page = page;
            this.stream = stream;
        }

        private void close() throws IOException {
            stream.close();
        }
    }
}
