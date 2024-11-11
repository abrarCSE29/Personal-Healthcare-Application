package com.example.personalhealthcareapplication.model;

public class SummaryRequest {
    private String model;
    private String prompt;

    public SummaryRequest(String model, String prompt) {
        this.model = model;
        this.prompt = "Generate a summary for the following medical report. It should contain a section called summary of report and another section called Advisements based on the report. Report should be within 30 lines";
        this.prompt = this.prompt+prompt;

    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
