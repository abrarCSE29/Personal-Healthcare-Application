package com.example.personalhealthcareapplication.view;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.api.ApiClient;
import com.example.personalhealthcareapplication.api.OllamaService;
import com.example.personalhealthcareapplication.model.SummaryRequest;
import com.example.personalhealthcareapplication.model.SummaryResponse;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.json.JSONObject;

import java.io.InputStream;

import io.noties.markwon.Markwon;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadReportFragment extends Fragment {

    private static final int PDF_REQUEST_CODE = 1;
    private TextView tvSelectedFile, tvSummaryResult;
    private Uri pdfUri;
    private Button btnGenerateSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_report, container, false);

        PDFBoxResourceLoader.init(getContext());  // Initialize PDFBox

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
            tvSelectedFile.setText(getFileName(pdfUri));
            btnGenerateSummary.setEnabled(true);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
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

            ollamaGenerateSummary(extractedText);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to analyze PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void ollamaGenerateSummary(String extractedText) {
        OllamaService service = ApiClient.getClient().create(OllamaService.class);
        SummaryRequest request = new SummaryRequest("llama3.2:1b", extractedText);

        service.generateSummary(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            // Initialize Markwon for rendering markdown
                            Markwon markwon = Markwon.create(getContext());
                            BufferedSource source = Okio.buffer(response.body().source());
                            StringBuilder summaryBuilder = new StringBuilder();

                            while (!source.exhausted()) {
                                String line = source.readUtf8Line();
                                if (line != null && !line.isEmpty()) {
                                    JSONObject jsonObject = new JSONObject(line);
                                    String lineResponse = jsonObject.getString("response");

                                    summaryBuilder.append(lineResponse);

                                    // Update the UI in real-time
                                    getActivity().runOnUiThread(() -> {
                                        // Render the markdown formatted text
                                        markwon.setMarkdown(tvSummaryResult, summaryBuilder.toString());
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(() -> {
                                tvSummaryResult.setText("Failed to parse response.");
                            });
                        }
                    }).start();

                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                    tvSummaryResult.setText("Failed to generate summary: " + errorBody);
                    Log.e("SummaryError", "Response code: " + response.code() + ", error: " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                tvSummaryResult.setText("Error: " + t.getMessage());
            }
        });
    }
}
