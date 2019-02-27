package io.mosip.authentication.service.impl.indauth.validator;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthTypeDTO;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.dto.indauth.IdentityDTO;
import io.mosip.authentication.core.dto.indauth.InternalAuthType;
import io.mosip.authentication.core.dto.indauth.RequestDTO;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.util.DateUtils;

/**
 * Validator for internal authentication request
 * 
 * @author Prem Kumar
 *
 */
@Component
public class InternalAuthRequestValidator extends BaseAuthRequestValidator {
	/** The Final Constant For allowed Internal auth  type*/
	private static final String INTERNAL_ALLOWED_AUTH_TYPE = "internal.allowed.auth.type";

	/** The Constant REQ_TIME. */
	private static final String REQ_TIME = "reqTime";

	/** The Constant REQUEST. */
	private static final String REQUEST = "request";
	
	/** The Constant AUTH_TYPE. */
	private static final String AUTH_TYPE = "authType";
	
	/** The Constant REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS. */
	private static final String REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS = "authrequest.received-time-allowed.in-hours";


	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.impl.indauth.validator.BaseAuthRequestValidator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(AuthRequestDTO.class);
	}

	/* (non-Javadoc)
	 * @see io.mosip.authentication.service.impl.indauth.validator.BaseAuthRequestValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object authRequestDTO, Errors errors) {
		if (authRequestDTO instanceof AuthRequestDTO) {
			AuthRequestDTO requestDTO = (AuthRequestDTO) authRequestDTO;
			validateId(requestDTO.getId(), errors);
			Optional<String> uinOpt = Optional.ofNullable(requestDTO.getRequest())
					.map(RequestDTO::getIdentity)
					.map(IdentityDTO::getUin);
			Optional<String> vidOpt = Optional.ofNullable(requestDTO.getRequest())
					.map(RequestDTO::getIdentity)
					.map(IdentityDTO::getVid);
			if(uinOpt.isPresent()) {
				validateIdvId(uinOpt.get(), IdType.UIN.getType(), errors);
			} else if(vidOpt.isPresent()) {
				validateIdvId(vidOpt.get(), IdType.VID.getType(), errors);
			} else {
				// TODO Missing UIN/VID
			}
			//validateVer(requestDTO.getVer(), errors);
			validateTxnId(requestDTO.getTransactionID(), errors);
			validateDate(requestDTO, errors);
			validateAuthType(requestDTO.getAuthType(), errors);
			validateRequest(requestDTO, errors);
		}
	}

	/**
	 * Method to validate auth type
	 * 
	 * @param requestDTO
	 * @param errors
	 */
	private void validateRequest(AuthRequestDTO requestDTO, Errors errors) {
		AuthTypeDTO authTypeDTO = requestDTO.getRequestedAuth();
		if (authTypeDTO != null) {
			Set<String> allowedAuthType = extractAuthInfo();			
			validateAuthType(requestDTO, errors, authTypeDTO, allowedAuthType);
		}else {
			errors.rejectValue(REQUEST, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
					String.format(IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage(), REQUEST));
		}
		
		
	}

	/**
	 * Validate auth type.
	 *
	 * @param requestDTO the request DTO
	 * @param errors the errors
	 * @param authTypeDTO the auth type DTO
	 * @param allowedAuthType the allowed auth type
	 */
	private void validateAuthType(AuthRequestDTO requestDTO, Errors errors, AuthTypeDTO authTypeDTO,
			Set<String> allowedAuthType) {
		checkAllowedAuthType(requestDTO, errors, authTypeDTO, allowedAuthType);
		
		if(authTypeDTO.isBio()) {
			if(allowedAuthType.contains(InternalAuthType.BIO.getType())) {
				validateBioMetadataDetails(requestDTO, errors);
			} else {
				errors.rejectValue(AUTH_TYPE, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
						new Object[]{AUTH_TYPE} , IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
			}
			
		}
	}

	/**
	 * Check allowed auth type.
	 *
	 * @param requestDTO the request DTO
	 * @param errors the errors
	 * @param authTypeDTO the auth type DTO
	 * @param allowedAuthType the allowed auth type
	 */
	private void checkAllowedAuthType(AuthRequestDTO requestDTO, Errors errors, AuthTypeDTO authTypeDTO,
			Set<String> allowedAuthType) {
		if(authTypeDTO.isDemo()) {
			if(allowedAuthType.contains(InternalAuthType.DEMO.getType())) {
				checkDemoAuth(requestDTO, errors);
			} else {
				errors.rejectValue(AUTH_TYPE, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
						new Object[]{AUTH_TYPE} , IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
			}
		} 
		
		if(authTypeDTO.isOtp()) {
			if(allowedAuthType.contains(InternalAuthType.OTP.getType())) {
				checkOTPAuth(requestDTO, errors);
			} else {
				errors.rejectValue(AUTH_TYPE, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
						new Object[]{AUTH_TYPE} , IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
			}
		}
		
		if(authTypeDTO.isPin()) {
			if(allowedAuthType.contains(InternalAuthType.SPIN.getType())) {
				validateAdditionalFactorsDetails(requestDTO, errors);
			} else {
				errors.rejectValue(AUTH_TYPE, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
						new Object[]{AUTH_TYPE} , IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
			}
		}
	}

	/**
	 * Extract auth info.
	 *
	 * @return the sets the
	 */
	private Set<String> extractAuthInfo() {
		Set<String> allowedAuthType = new HashSet<>();
		String intAllowedAuthType = env.getProperty(INTERNAL_ALLOWED_AUTH_TYPE);
		if (null!=intAllowedAuthType && intAllowedAuthType.contains(",")) {
			String value[] = intAllowedAuthType.split(",");
			for (int i = 0; i < value.length; i++) {
				allowedAuthType.add(value[i]);				
			}
		}else {
			allowedAuthType.add(intAllowedAuthType);
		}
		return allowedAuthType;
	}

	/**
	 *  Validation for DateTime.
	 *
	 * @param authRequestDTO the auth request DTO
	 * @param errors the errors
	 */
	public void validateDate(AuthRequestDTO authRequestDTO, Errors errors) {
		if (null != authRequestDTO.getRequestTime()&& !authRequestDTO.getRequestTime().isEmpty()) {
			try {
				Date reqDate = DateUtils.parseToDate(authRequestDTO.getRequestTime(),env.getProperty("datetime.pattern"));
				Instant reqTimeInstance =reqDate.toInstant();
				Instant now = Instant.now();
				Integer reqDateMaxTimeInt = env.getProperty(REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS, Integer.class);
				if (reqDate.after(new Date())|| Duration.between(reqTimeInstance, now).toHours() > reqDateMaxTimeInt) {
					errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP.getErrorCode(),
							new Object[] {env.getProperty(REQUESTDATE_RECEIVED_IN_MAX_TIME_MINS, Integer.class)},IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP.getErrorMessage());
				}

			} catch (ParseException | java.text.ParseException e) {
				errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorCode(),
						new Object[] {REQ_TIME},IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST.getErrorMessage());
			}

		}else {
			errors.rejectValue(REQ_TIME, IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					new Object[] {REQ_TIME},IdAuthenticationErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage());
		
			
		}
	}

}
