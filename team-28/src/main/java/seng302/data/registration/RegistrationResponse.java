package seng302.data.registration;

/**
 * Created by mjt169 on 10/08/17.
 */
public class RegistrationResponse {

    private Integer sourceId;
    private RegistrationResponseStatus status;

    public RegistrationResponse(Integer sourceId, RegistrationResponseStatus status) {
        this.sourceId = sourceId;
        this.status = status;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public RegistrationResponseStatus getStatus() {
        return status;
    }
}
