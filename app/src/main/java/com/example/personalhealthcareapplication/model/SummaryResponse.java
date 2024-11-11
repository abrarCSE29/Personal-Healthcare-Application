
package com.example.personalhealthcareapplication.model;
import java.util.List;

public class SummaryResponse {
    private List<ResponsePart> responses;

    public List<ResponsePart> getResponses() {
        return responses;
    }

    public void setResponses(List<ResponsePart> responses) {
        this.responses = responses;
    }

    // Inner class to hold each response part
    public static class ResponsePart {
        private String response;

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }
}
