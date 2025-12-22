package com.jumunhasyeotjo.order_to_shipping.shipping.infrastructure.external.gemini.dto;

import java.util.List;

public class GeminiResponse {

    private final List<Candidate> candidates;

    public GeminiResponse(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public static class Candidate {
        private final Content content;

        public Candidate(Content content) {
            this.content = content;
        }

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        private final String text;

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}