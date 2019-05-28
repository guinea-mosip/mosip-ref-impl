package io.mosip.registration.processor.quality.checker.stage;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.registration.processor.packet.storage.utils.Utilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.mosip.kernel.bioapi.impl.BioApiImpl;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.spi.IBioApi;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.code.RegistrationExceptionTypeCode;
import io.mosip.registration.processor.core.code.RegistrationTransactionStatusCode;
import io.mosip.registration.processor.core.code.RegistrationTransactionTypeCode;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.PacketFiles;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.util.JsonUtil;
import io.mosip.registration.processor.core.util.RegistrationExceptionMapperUtil;
import io.mosip.registration.processor.quality.checker.exception.FieldNotPresentException;
import io.mosip.registration.processor.quality.checker.exception.FileNameMissingException;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

public class QualityCheckerStage extends MosipVerticleManager {

	/** The cluster manager url. */
	@Value("${vertx.cluster.configuration}")
	private String clusterManagerUrl;

	//@Value("${mosip.registration.iris_threshold}")
	private Integer irisThreshold = 70;

	//@Value("${mosip.registration.leftslap_fingerprint_threshold}")
	private Integer leftFingerThreshold = 80;

	//@Value("${mosip.registration.rightslap_fingerprint_threshold}")
	private Integer rightFingerThreshold = 80;

	//@Value("${mosip.registration.thumbs_fingerprint_threshold}")
	private Integer thumbFingerThreshold = 80;

	//@Value("${mosip.registration.facequalitythreshold}")
	private Integer faceThreshold = 25;

	/** The adapter. */
	@Autowired
	private FileSystemAdapter adapter;

	/** The core audit request builder. */
	@Autowired
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The registration status service. */
	@Autowired
	private RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	@Autowired
	private Utilities utilities;

	private IBioApi bioAPi = new BioApiImpl();

	private CbeffUtil cbeffUtil = new CbeffImpl();

	private RegistrationExceptionMapperUtil registrationStatusMapperUtil = new RegistrationExceptionMapperUtil();

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(QualityCheckerStage.class);

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = File.separator;

	/**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		MosipEventBus mosipEventBus = this.getEventBus(this, clusterManagerUrl, 50);
		this.consumeAndSend(mosipEventBus, MessageBusAddress.QUALITY_CHECKER_BUS_IN,
				MessageBusAddress.QUALITY_CHECKER_BUS_OUT);
	}

	@Override
	public MessageDTO process(MessageDTO object) {
		object.setMessageBusAddress(MessageBusAddress.QUALITY_CHECKER_BUS_IN);
		String regId = object.getRid();
		String description = "";
		Boolean isTransactionSuccessful = null;
		InternalRegistrationStatusDto registrationStatusDto = null;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), regId,
				"QualityCheckerStage::process()::entry");
		try {
			registrationStatusDto = registrationStatusService.getRegistrationStatus(regId);
			registrationStatusDto.setRegistrationStageName(this.getClass().getSimpleName());
			InputStream idJsonStream = adapter.getFile(regId,
					PacketFiles.DEMOGRAPHIC.name() + FILE_SEPARATOR + PacketFiles.ID.name());
			String idJsonString = IOUtils.toString(idJsonStream, "UTF-8");
			JSONObject idJsonObject = JsonUtil.objectMapperReadValue(idJsonString, JSONObject.class);
			JSONObject identity = JsonUtil.getJSONObject(idJsonObject,
					utilities.getGetRegProcessorDemographicIdentity());
			JSONObject individualBiometricsObject = JsonUtil.getJSONObject(identity, "individualBiometrics");
			if (individualBiometricsObject == null) {
				description = "Individual Biometric parameter is not present in ID Json";
				throw new FieldNotPresentException(PlatformErrorMessages.RPR_QCR_FIELD_NOT_PRESENT.getCode(),
						PlatformErrorMessages.RPR_QCR_FIELD_NOT_PRESENT.getMessage());
			}
			String biometricFileName = JsonUtil.getJSONValue(individualBiometricsObject, "value");
			if (biometricFileName == null || biometricFileName.isEmpty()) {
				description = "File Name of individual biometric is not present";
				throw new FileNameMissingException(PlatformErrorMessages.RPR_QCR_FILENAME_MISSING.getCode(),
						PlatformErrorMessages.RPR_QCR_FILENAME_MISSING.getMessage());
			}
			InputStream cbeffStream = adapter.getFile(regId,
					PacketFiles.BIOMETRIC.name() + FILE_SEPARATOR + biometricFileName);
			List<BIRType> birTypeList = cbeffUtil.getBIRDataFromXML(IOUtils.toByteArray(cbeffStream));
			int scoreCounter = 0;
			for (BIRType birType : birTypeList) {
				SingleType singleType = birType.getBDBInfo().getType().get(0);
				List<String> subtype = birType.getBDBInfo().getSubtype();
				Integer threshold = getThresholdBasedOnType(singleType, subtype);
				QualityScore qualityScore = bioAPi.checkQuality(birType, null);
				if (qualityScore.getInternalScore() < threshold) {
					object.setIsValid(Boolean.FALSE);
					isTransactionSuccessful = Boolean.FALSE;
					registrationStatusDto
							.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.FAILED.toString());
					registrationStatusDto.setStatusCode(RegistrationStatusCode.REJECTED.toString());
					description = "The " + birType.getBDBInfo().getType().get(0)
							+ " information captured is below the configured threshold";
					break;
				} else {
					scoreCounter++;
				}
			}
			if (scoreCounter == birTypeList.size()) {
				object.setIsValid(Boolean.TRUE);
				description = "All Quality Scores are more than threshold";
				isTransactionSuccessful = Boolean.TRUE;
				registrationStatusDto
						.setLatestTransactionStatusCode(RegistrationTransactionStatusCode.SUCCESS.toString());
				registrationStatusDto.setStatusCode(RegistrationStatusCode.PROCESSING.toString());
			}

			registrationStatusDto
					.setLatestTransactionTypeCode(RegistrationTransactionTypeCode.QUALITY_CHECK.toString());
		} catch (FSAdapterException e) {
			registrationStatusDto.setStatusCode(RegistrationStatusCode.FAILED.name());
			registrationStatusDto
					.setStatusComment(PlatformErrorMessages.RPR_UGS_PACKET_STORE_NOT_ACCESSIBLE.getMessage());
			registrationStatusDto.setLatestTransactionStatusCode(
					registrationStatusMapperUtil.getStatusCode(RegistrationExceptionTypeCode.FSADAPTER_EXCEPTION));
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					regId, PlatformErrorMessages.RPR_UGS_PACKET_STORE_NOT_ACCESSIBLE.getMessage() + e.getMessage());
			object.setInternalError(Boolean.TRUE);
			isTransactionSuccessful = false;
			description = "FileSytem is not accessible for packet " + regId + "::" + e.getMessage();
			object.setRid(regId);
		} catch (BiometricException e) {
			registrationStatusDto.setStatusCode(RegistrationStatusCode.FAILED.name());
			registrationStatusDto.setStatusComment(PlatformErrorMessages.RPR_QCR_BIOMETRIC_EXCEPTION.getMessage());
			registrationStatusDto.setLatestTransactionStatusCode(
					registrationStatusMapperUtil.getStatusCode(RegistrationExceptionTypeCode.BIOMETRIC_EXCEPTION));
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					regId, PlatformErrorMessages.RPR_QCR_BIOMETRIC_EXCEPTION.getMessage() + e.getMessage());
			object.setInternalError(Boolean.TRUE);
			isTransactionSuccessful = false;
			description = "Biometric exception from IDA for " + regId + "::" + e.getMessage();
			object.setRid(regId);
		} catch (Exception ex) {
			registrationStatusDto.setStatusCode(RegistrationStatusCode.FAILED.name());
			registrationStatusDto.setStatusComment(ExceptionUtils.getMessage(ex));
			registrationStatusDto.setLatestTransactionStatusCode(
					registrationStatusMapperUtil.getStatusCode(RegistrationExceptionTypeCode.EXCEPTION));
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					regId,
					RegistrationStatusCode.FAILED.toString() + ex.getMessage() + ExceptionUtils.getStackTrace(ex));
			object.setInternalError(Boolean.TRUE);
			isTransactionSuccessful = Boolean.FALSE;
			description = "Internal error occurred in QualityChecker stage while processing registrationId " + regId
					+ ex.getMessage();
		} finally {
			registrationStatusDto.setStatusComment(description);
			registrationStatusService.updateRegistrationStatus(registrationStatusDto);
			String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			String eventName = isTransactionSuccessful ? EventName.UPDATE.toString() : EventName.EXCEPTION.toString();
			String eventType = isTransactionSuccessful ? EventType.BUSINESS.toString() : EventType.SYSTEM.toString();

			String moduleId = isTransactionSuccessful ? "Quality-Check Success" : "";
			String moduleName = "Quality-Checker";

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType, moduleId,
					moduleName, regId);
			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), regId,
					"QualityCheckerStage::process()::exit");
		}

		return object;
	}

	private Integer getThresholdBasedOnType(SingleType singleType, List<String> subtype) {
		if(singleType.value().equalsIgnoreCase("FINGER")){
			if(subtype.contains("Thumb")){
				return thumbFingerThreshold;
			} else if(subtype.contains("Right")){
				return rightFingerThreshold;
			} else if(subtype.contains("Left")) {
				return leftFingerThreshold;
			}
		} else if(singleType.value().equalsIgnoreCase("IRIS")){
			return irisThreshold;
		} else if(singleType.value().equalsIgnoreCase("FACE")){
			return faceThreshold;
		}
		return 0;
	}
}
