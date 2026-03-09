package com.fitexa.services.impl;

import com.fitexa.entities.UserEntity;
import com.fitexa.entities.UserPaymentMovementEntity;
import com.fitexa.services.PaymentReceiptPdfService;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentReceiptPdfServiceImpl implements PaymentReceiptPdfService {
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 36f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);

    private static final PDFont FONT_REGULAR = PDType1Font.HELVETICA;
    private static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;

    private static final Color COLOR_BG = new Color(243, 246, 251);
    private static final Color COLOR_HEADER_TOP = new Color(27, 35, 57);
    private static final Color COLOR_HEADER_BOTTOM = new Color(17, 22, 38);
    private static final Color COLOR_PRIMARY = new Color(255, 115, 43);
    private static final Color COLOR_PRIMARY_SOFT = new Color(255, 238, 227);
    private static final Color COLOR_CARD = new Color(255, 255, 255);
    private static final Color COLOR_BORDER = new Color(223, 229, 238);
    private static final Color COLOR_TEXT = new Color(32, 37, 46);
    private static final Color COLOR_MUTED = new Color(97, 108, 126);
    private static final Color COLOR_SUCCESS_BG = new Color(231, 247, 237);
    private static final Color COLOR_SUCCESS_TEXT = new Color(33, 112, 67);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generateReceipt(UserPaymentMovementEntity movement) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                fillRect(stream, 0, 0, PAGE_WIDTH, PAGE_HEIGHT, COLOR_BG);

                float y = drawHeader(stream, movement);
                y = drawSummary(stream, movement, y - 18f);
                y = drawParties(stream, movement, y - 18f);
                y = drawMovementDetail(stream, movement, y - 18f);
                drawFooter(stream, movement, y - 24f);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el comprobante de pago", ex);
        }
    }

    private float drawHeader(PDPageContentStream stream, UserPaymentMovementEntity movement) throws IOException {
        float headerHeight = 118f;
        float topY = PAGE_HEIGHT - MARGIN;
        float brandX = MARGIN + 20f;
        float brandY = topY - 86f;
        float brandWidth = 92f;
        float brandHeight = 28f;
        float statusWidth = 96f;
        float statusHeight = 28f;
        float statusX = MARGIN + CONTENT_WIDTH - 132f;
        float statusY = topY - 82f;

        fillRect(stream, MARGIN, topY - headerHeight, CONTENT_WIDTH, headerHeight, COLOR_HEADER_TOP);
        fillRect(stream, MARGIN, topY - headerHeight, CONTENT_WIDTH, headerHeight / 2f, COLOR_HEADER_BOTTOM);
        fillRect(stream, brandX, brandY, brandWidth, brandHeight, COLOR_PRIMARY);

        drawCenteredText(stream, FONT_BOLD, 12f, Color.WHITE, "F I T E X A", brandX, brandY, brandWidth, brandHeight);
        drawText(stream, FONT_BOLD, 22f, Color.WHITE, "Resumen de pago", MARGIN + 20f, topY - 34f);
        drawText(
            stream,
            FONT_REGULAR,
            10.5f,
            new Color(221, 228, 240),
            "Comprobante " + movement.getId() + "  |  Emitido " + LocalDate.now().format(DATE_FORMAT),
            MARGIN + 20f,
            topY - 52f
        );

        drawCenteredText(stream, FONT_BOLD, 11f, new Color(255, 214, 191), "Estado", statusX, topY - 43f, statusWidth, 16f);
        fillRect(stream, statusX, statusY, statusWidth, statusHeight, COLOR_SUCCESS_BG);
        drawCenteredText(stream, FONT_BOLD, 11f, COLOR_SUCCESS_TEXT, "Pago acreditado", statusX, statusY, statusWidth, statusHeight);
        return topY - headerHeight;
    }

    private float drawSummary(PDPageContentStream stream, UserPaymentMovementEntity movement, float startY) throws IOException {
        float cardHeight = 102f;
        float cardWidth = (CONTENT_WIDTH - 14f) / 2f;

        drawCard(stream, MARGIN, startY - cardHeight, cardWidth, cardHeight);
        drawCard(stream, MARGIN + cardWidth + 14f, startY - cardHeight, cardWidth, cardHeight);

        drawText(stream, FONT_BOLD, 10.5f, COLOR_PRIMARY, "IMPORTE", MARGIN + 16f, startY - 20f);
        drawText(stream, FONT_BOLD, 24f, COLOR_TEXT, formatCurrency(movement.getAmount()), MARGIN + 16f, startY - 52f);
        drawText(stream, FONT_REGULAR, 10f, COLOR_MUTED, "Movimiento registrado y asociado al alumno.", MARGIN + 16f, startY - 74f);

        float rightX = MARGIN + cardWidth + 30f;
        drawText(stream, FONT_BOLD, 10.5f, COLOR_PRIMARY, "PLAN Y CONCEPTO", rightX, startY - 20f);
        drawText(stream, FONT_BOLD, 13f, COLOR_TEXT, safeText(resolvePlanName(movement)), rightX, startY - 43f);
        drawText(stream, FONT_REGULAR, 10f, COLOR_MUTED, safeText(movement.getDetail()), rightX, startY - 63f);
        drawText(
            stream,
            FONT_REGULAR,
            10f,
            COLOR_MUTED,
            "Fecha imputada: " + movement.getPaidAt().format(DATE_TIME_FORMAT),
            rightX,
            startY - 82f
        );
        return startY - cardHeight;
    }

    private float drawParties(PDPageContentStream stream, UserPaymentMovementEntity movement, float startY) throws IOException {
        float blockHeight = 118f;
        drawCard(stream, MARGIN, startY - blockHeight, CONTENT_WIDTH, blockHeight);

        float leftX = MARGIN + 18f;
        float rightX = MARGIN + (CONTENT_WIDTH / 2f) + 8f;
        UserEntity user = movement.getUser();

        drawText(stream, FONT_BOLD, 10.5f, COLOR_PRIMARY, "TITULAR", leftX, startY - 20f);
        drawText(stream, FONT_BOLD, 13f, COLOR_TEXT, safeText(user.getFirstName() + " " + user.getLastName()), leftX, startY - 42f);
        drawText(stream, FONT_REGULAR, 10.5f, COLOR_MUTED, safeText(user.getEmail()), leftX, startY - 62f);
        drawText(stream, FONT_REGULAR, 10.5f, COLOR_MUTED, "Cliente ID: " + user.getId(), leftX, startY - 82f);

        drawText(stream, FONT_BOLD, 10.5f, COLOR_PRIMARY, "EMISOR", rightX, startY - 20f);
        drawText(stream, FONT_BOLD, 13f, COLOR_TEXT, "Fitexa Training Club", rightX, startY - 42f);
        drawText(stream, FONT_REGULAR, 10.5f, COLOR_MUTED, "Gestion interna de cuotas y renovaciones.", rightX, startY - 62f);
        drawText(stream, FONT_REGULAR, 10.5f, COLOR_MUTED, "Fecha de emision: " + LocalDate.now().format(DATE_FORMAT), rightX, startY - 82f);

        return startY - blockHeight;
    }

    private float drawMovementDetail(PDPageContentStream stream, UserPaymentMovementEntity movement, float startY) throws IOException {
        float blockHeight = 214f;
        drawCard(stream, MARGIN, startY - blockHeight, CONTENT_WIDTH, blockHeight);

        float labelX = MARGIN + 18f;
        float valueX = MARGIN + 176f;
        float rowY = startY - 24f;

        drawText(stream, FONT_BOLD, 11f, COLOR_TEXT, "Detalle del movimiento", labelX, rowY);
        rowY -= 26f;

        rowY = drawDetailRow(stream, "Numero de comprobante", String.valueOf(movement.getId()), labelX, valueX, rowY);
        rowY = drawDetailRow(stream, "Fecha y hora de pago", movement.getPaidAt().format(DATE_TIME_FORMAT), labelX, valueX, rowY);
        rowY = drawDetailRow(stream, "Concepto", safeText(movement.getDetail()), labelX, valueX, rowY);
        rowY = drawDetailRow(stream, "Plan asociado", safeText(resolvePlanName(movement)), labelX, valueX, rowY);

        float noteX = MARGIN + 18f;
        float noteWidth = CONTENT_WIDTH - 36f;
        float noteHeight = 34f;
        float noteY = rowY - noteHeight - 8f;
        fillRect(stream, noteX, noteY, noteWidth, noteHeight, COLOR_PRIMARY_SOFT);
        drawCenteredText(
            stream,
            FONT_BOLD,
            10.5f,
            COLOR_PRIMARY,
            "Este comprobante no es valido como factura. Uso interno y constancia de pago.",
            noteX,
            noteY,
            noteWidth,
            noteHeight
        );
        return startY - blockHeight;
    }

    private void drawFooter(PDPageContentStream stream, UserPaymentMovementEntity movement, float y) throws IOException {
        float footerY = Math.max(y, 84f);
        drawText(stream, FONT_BOLD, 11f, COLOR_TEXT, "Canales de consulta", MARGIN, footerY);
        drawText(
            stream,
            FONT_REGULAR,
            9.5f,
            COLOR_MUTED,
            "Ante cualquier diferencia, comunicate con administracion indicando el comprobante " + movement.getId() + ".",
            MARGIN,
            footerY - 16f
        );
        drawText(
            stream,
            FONT_REGULAR,
            9.5f,
            COLOR_MUTED,
            "Documento generado automaticamente por Fitexa el " + LocalDate.now().format(DATE_FORMAT) + ".",
            MARGIN,
            footerY - 30f
        );
    }

    private float drawDetailRow(
        PDPageContentStream stream,
        String label,
        String value,
        float labelX,
        float valueX,
        float y
    ) throws IOException {
        drawText(stream, FONT_BOLD, 10f, COLOR_MUTED, label, labelX, y);
        drawText(stream, FONT_REGULAR, 10.5f, COLOR_TEXT, safeText(value), valueX, y);
        strokeLine(stream, labelX, y - 8f, MARGIN + CONTENT_WIDTH - 18f, y - 8f, COLOR_BORDER);
        return y - 24f;
    }

    private String resolvePlanName(UserPaymentMovementEntity movement) {
        return movement.getPlan() == null ? "Sin plan asignado" : movement.getPlan().getName();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return "$" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return cleanText(value);
    }

    private void drawCard(PDPageContentStream stream, float x, float y, float width, float height) throws IOException {
        fillRect(stream, x, y, width, height, COLOR_CARD);
        strokeRect(stream, x, y, width, height, COLOR_BORDER);
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

    private void drawCenteredText(
        PDPageContentStream stream,
        PDFont font,
        float fontSize,
        Color color,
        String text,
        float x,
        float y,
        float width,
        float height
    ) throws IOException {
        String cleaned = cleanText(text);
        float textWidth = font.getStringWidth(cleaned) / 1000f * fontSize;
        float textX = x + Math.max((width - textWidth) / 2f, 0f);
        float textY = y + ((height - fontSize) / 2f) + (fontSize * 0.30f);
        drawText(stream, font, fontSize, color, cleaned, textX, textY);
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

    private void strokeLine(PDPageContentStream stream, float startX, float startY, float endX, float endY, Color color)
        throws IOException {
        stream.setStrokingColor(color);
        stream.moveTo(startX, startY);
        stream.lineTo(endX, endY);
        stream.stroke();
    }

    private String cleanText(String value) {
        return value == null
            ? "-"
            : value
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N")
                .replace("ü", "u").replace("Ü", "U")
                .replace("’", "'")
                .replace("–", "-");
    }
}
