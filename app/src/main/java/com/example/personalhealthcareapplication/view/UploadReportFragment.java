package com.example.personalhealthcareapplication.view;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.personalhealthcareapplication.R;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;

public class UploadReportFragment extends Fragment {


    private static final int PDF_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;
    private TextView tvSelectedFile, tvSummaryResult;
    private Uri pdfUri;
    private Button btnGenerateSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_report, container, false);

        PDFBoxResourceLoader.init(getContext()); // Initialize PDFBox

        tvSelectedFile = view.findViewById(R.id.tvSelectedFile);
        tvSummaryResult = view.findViewById(R.id.tvSummaryResult);
        btnGenerateSummary = view.findViewById(R.id.btnGenerateSummary);
        Button btnSelectPdf = view.findViewById(R.id.btnSelectPdf);

        btnSelectPdf.setOnClickListener(v -> selectPdf());
        btnGenerateSummary.setOnClickListener(v -> analyzePdf());

        return view;
    }

    private void selectPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            pdfUri = data.getData();
            String fileName = getFileName(pdfUri);
            tvSelectedFile.setText(fileName);
            btnGenerateSummary.setEnabled(true);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = ((android.database.Cursor) cursor).getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void analyzePdf() {
        if (pdfUri == null) {
            Toast.makeText(getContext(), "No PDF selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream inputStream = getContext().getContentResolver().openInputStream(pdfUri);
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);

            // Call the summarization function
            String summary = summarizeText(extractedText);
            tvSummaryResult.setText(summary);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to analyze PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private String summarizeText(String extractedText) {
        // Basic summarization: First 3 sentences
        String[] sentences = extractedText.split("\\.");
        int numSentences = Math.min(3, sentences.length);

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < numSentences; i++) {
            summary.append(sentences[i].trim()).append(". ");
        }
        return summary.toString();
    }
}
