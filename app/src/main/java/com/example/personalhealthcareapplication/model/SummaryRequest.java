package com.example.personalhealthcareapplication.model;

public class SummaryRequest {
    private String model;
    private String prompt;

    public SummaryRequest(String model, String prompt) {
        this.model = model;
        this.prompt = "Generate a summary for the following medical report. Just write the summary of test and advise medical procedures that should be followed.";
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
