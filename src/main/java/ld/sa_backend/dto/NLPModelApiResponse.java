package ld.sa_backend.dto;

import java.util.List;

public class NLPModelApiResponse {
    private List<Rating> predictions;

    public List<Rating> getRatings() {
        return this.predictions;
    }

    public void setRatings(List<Rating> predictions) {
        this.predictions = predictions;
    }
}
