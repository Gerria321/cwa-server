package app.coronawarn.server.services.submission.checkins;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class EventCheckInProtectedReportsValidator {

  /**
   * Given the submission payload, it verifies whether user event checkInProtectedReports data is aligned with the
   * application constraints. For each checkInProtectedReports:
   * <li>locationIdHash must have 32 bytes</li>
   * <li>iv must have 32 bytes</li>
   * <li>encryptedCheckInRecord must have more than 0 bytes</li>
   */
  public boolean verify(SubmissionPayload submissionPayload, ConstraintValidatorContext validatorContext) {
    List<CheckInProtectedReport> checkInProtectedReportsList = submissionPayload.getCheckInProtectedReportsList();
    return checkInProtectedReportsList.stream()
        .map(checkInProtectedReport -> verifyLocationIdHashLength(checkInProtectedReport, validatorContext)
            && verifyIvLength(checkInProtectedReport, validatorContext)
            && verifyEncryptedCheckInRecordLength(checkInProtectedReport, validatorContext))
        .allMatch(checkInValidation -> Boolean.valueOf(checkInValidation).equals(Boolean.TRUE));
  }

  boolean verifyLocationIdHashLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getLocationIdHash())
        || checkInProtectedReport.getLocationIdHash().size() != 32) {
      addViolation(validatorContext, "CheckInProtectedReports locationIdHash must have 32 bytes not "
          + (checkInProtectedReport.getLocationIdHash() == null ? 0
          : checkInProtectedReport.getLocationIdHash().size()));
      return false;
    }
    return true;
  }

  boolean verifyIvLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getIv())
        || checkInProtectedReport.getIv().size() != 32) {
      addViolation(validatorContext, "CheckInProtectedReports iv must have 32 bytes not "
          + (checkInProtectedReport.getIv() == null ? 0 : checkInProtectedReport.getIv().size()));
      return false;
    }
    return true;
  }

  boolean verifyEncryptedCheckInRecordLength(CheckInProtectedReport checkInProtectedReport,
      ConstraintValidatorContext validatorContext) {
    if (ObjectUtils.isEmpty(checkInProtectedReport.getEncryptedCheckInRecord())
        || checkInProtectedReport.getEncryptedCheckInRecord().size() != 16) {
      addViolation(validatorContext, "CheckInProtectedReports encryptedCheckInRecord must have 16 bytes not "
          + (checkInProtectedReport.getEncryptedCheckInRecord() == null ? 0
          : checkInProtectedReport.getEncryptedCheckInRecord().size()));
      return false;
    }
    return true;
  }

  void addViolation(ConstraintValidatorContext validatorContext, String message) {
    validatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
  }
}