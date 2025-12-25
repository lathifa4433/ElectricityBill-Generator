import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ElectricityBill {

    // ---------------- BILL CALCULATION ----------------
    public static double calculateBill(int units, boolean isCommercial, boolean peakHours) {

        double bill;

        if (units <= 100)
            bill = units * 10;
        else if (units <= 200)
            bill = (100 * 10) + (units - 100) * 15;
        else if (units <= 300)
            bill = (100 * 10) + (100 * 15) + (units - 200) * 20;
        else
            bill = (100 * 10) + (100 * 15) + (100 * 20) + (units - 300) * 25;

        if (peakHours)
            bill += bill * 0.15;

        if (isCommercial)
            bill += bill * 0.20;

        bill += bill * 0.05 + 50; // tax + service charge

        return Math.round(bill * 100.0) / 100.0;
    }

    // ---------------- NEXT MONTH ESTIMATION ----------------
    public static double expectedNextMonthBill(int currentUnits,
                                                boolean isCommercial,
                                                boolean peakHours) {

        int estimatedUnits =
                currentUnits + (int) (currentUnits * (Math.random() * 0.2 - 0.1));

        return calculateBill(estimatedUnits, isCommercial, peakHours);
    }

    // ---------------- PDF GENERATION ----------------
    public static void generatePdfBill(String houseNumber,
                                       String name,
                                       int units,
                                       boolean isCommercial,
                                       boolean peakHours,
                                       boolean isPaid,
                                       double bill,
                                       String dueDate,
                                       double nextMonthBill) {

        String fileName = "ElectricityBill_" + System.currentTimeMillis() + ".pdf";
        String billDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Electricity Bill", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Date: " + billDate, textFont));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.addCell(new PdfPCell(new Phrase("House Number", labelFont)));
            table.addCell(new PdfPCell(new Phrase(houseNumber, textFont)));

            table.addCell(new PdfPCell(new Phrase("Customer Name", labelFont)));
            table.addCell(new PdfPCell(new Phrase(name, textFont)));

            table.addCell(new PdfPCell(new Phrase("Connection Type", labelFont)));
            table.addCell(new PdfPCell(new Phrase(
                    isCommercial ? "Commercial" : "Residential", textFont)));

            table.addCell(new PdfPCell(new Phrase("Peak Usage", labelFont)));
            table.addCell(new PdfPCell(new Phrase(
                    peakHours ? "Yes" : "No", textFont)));

            table.addCell(new PdfPCell(new Phrase("Units Consumed", labelFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(units), textFont)));

            table.addCell(new PdfPCell(new Phrase("Payment Status", labelFont)));
            table.addCell(new PdfPCell(new Phrase(
                    isPaid ? "Paid" : "Unpaid", textFont)));

            table.addCell(new PdfPCell(new Phrase("Due Date", labelFont)));
            table.addCell(new PdfPCell(new Phrase(dueDate, textFont)));

            table.addCell(new PdfPCell(new Phrase("Bill Amount", labelFont)));
            table.addCell(new PdfPCell(
                    new Phrase(String.format("%.2f", bill), textFont)));

            table.addCell(new PdfPCell(
                    new Phrase("Expected Next Month Bill", labelFont)));
            table.addCell(new PdfPCell(
                    new Phrase(String.format("%.2f", nextMonthBill), textFont)));

            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Thank you for using our service.", textFont));

            document.close();

            System.out.println("PDF bill generated: " + fileName);

        } catch (DocumentException | IOException e) {
            System.out.println("PDF Error: " + e.getMessage());
        }
    }

    // ---------------- MAIN METHOD ----------------
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter House Number: ");
        String houseNumber = scanner.nextLine();

        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter units consumed: ");
        int units = scanner.nextInt();

        System.out.print("Connection type (R/C): ");
        char type = scanner.next().toUpperCase().charAt(0);
        boolean isCommercial = (type == 'C');

        System.out.print("Peak hours used (Y/N): ");
        char peak = scanner.next().toUpperCase().charAt(0);
        boolean peakHours = (peak == 'Y');

        System.out.print("Is bill paid (Y/N): ");
        char pay = scanner.next().toUpperCase().charAt(0);
        boolean isPaid = (pay == 'Y');

        double bill = calculateBill(units, isCommercial, peakHours);

        String dueDate;
        if (isPaid) {
            dueDate = "Paid";
        } else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 10);
            dueDate = new SimpleDateFormat("dd MMMM yyyy").format(cal.getTime());
        }

        double nextMonthBill =
                expectedNextMonthBill(units, isCommercial, peakHours);

        System.out.println("\n--- Electricity Bill ---");
        System.out.println("House No: " + houseNumber);
        System.out.println("Name: " + name);
        System.out.println("Units: " + units);
        System.out.println("Type: " +
                (isCommercial ? "Commercial" : "Residential"));
        System.out.println("Peak Usage: " +
                (peakHours ? "Yes" : "No"));
        System.out.println("Total Bill Amount: " + bill);
        System.out.println("Payment Status: " +
                (isPaid ? "Paid" : "Unpaid"));
        System.out.println("Due Date: " + dueDate);
        System.out.println("Expected Next Month Bill: " + nextMonthBill);

        generatePdfBill(
                houseNumber,
                name,
                units,
                isCommercial,
                peakHours,
                isPaid,
                bill,
                dueDate,
                nextMonthBill
        );

        scanner.close();
    }
}
