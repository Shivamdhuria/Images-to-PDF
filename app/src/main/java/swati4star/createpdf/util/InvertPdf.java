package swati4star.createpdf.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import swati4star.createpdf.interfaces.OnPDFCreatedInterface;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class InvertPdf extends AsyncTask<Void, Void, Void> {
    private String mPath;
    private OnPDFCreatedInterface mOnPDFCreatedInterface;
    private ArrayList<Bitmap> mBitmaps;
    private StringBuilder mSequence;
    private Boolean mIsNewPDFCreated;

    public InvertPdf(String mPath, OnPDFCreatedInterface onPDFCreatedInterface) {
        this.mPath = mPath;
        mSequence = new StringBuilder();
        mBitmaps = new ArrayList<>();
        this.mOnPDFCreatedInterface = onPDFCreatedInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mOnPDFCreatedInterface.onPDFCreationStarted();
        mIsNewPDFCreated = false;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (mPath != null)
                // resolve pdf file path based on relative path
                fileDescriptor = ParcelFileDescriptor.open(new File(mPath), MODE_READ_ONLY);


            if (fileDescriptor != null) {
                String outputPath = mPath.replace(".pdf", "_inverted" + ".pdf");
                if (createPDF(mPath, outputPath)) {
                    mPath = outputPath;
                    mIsNewPDFCreated = true;
                }

            }
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
            mIsNewPDFCreated = false;
        }

        return null;
    }

    void invert(PdfStamper stamper) {
        for (int i = stamper.getReader().getNumberOfPages(); i > 0; i--) {
            invertPage(stamper, i);
        }
    }

    void invertPage(PdfStamper stamper, int page) {
        Rectangle rect = stamper.getReader().getPageSize(page);

        PdfContentByte cb = stamper.getOverContent(page);
        PdfGState gs = new PdfGState();
        gs.setBlendMode(PdfGState.BM_DIFFERENCE);
        cb.setGState(gs);
        cb.setColorFill(new GrayColor(1.0f));
        cb.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        cb.fill();

        cb = stamper.getUnderContent(page);
        cb.setColorFill(new GrayColor(1.0f));
        cb.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        cb.fill();
    }


    @Override
    protected void onPostExecute(Void avoid) {
        // Execution of result of Long time consuming operation
        super.onPostExecute(avoid);
        mOnPDFCreatedInterface.onPDFCreated(mIsNewPDFCreated, mPath);
    }

    private boolean createPDF(String mPath, String outputPath) {

        try {
            PdfReader reader = new PdfReader(mPath);
            OutputStream os = new FileOutputStream(outputPath);
            PdfStamper stamper = new PdfStamper(reader, os);
            invert(stamper);
            stamper.close();
            os.close();
            return true;
        } catch (Exception er) {
            er.printStackTrace();
            return false;
        }

    }


}