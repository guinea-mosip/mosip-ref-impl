/**
 * 
 */
package io.mosip.kernel.smsserviceprovider.mtn.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.exception.UnsupportedEncodingException;
import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.smsserviceprovider.mtn.constant.SmsExceptionConstant;
import io.mosip.kernel.smsserviceprovider.mtn.constant.SmsPropertyConstant;

/**
 * @author CONDEIS
 * @since 1.1.0
 */
@Component
public class SMSServiceProviderImpl implements SMSServiceProvider {

	Logger LOGGER = LoggerFactory.getLogger(SMSServiceProviderImpl.class);

	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.country.code}")
	private String countryCode;
	@Value("${mosip.kernel.sms.api}")
	private String apiUrl;
	@Value("${mosip.kernel.sms.client}")
	private String clienId;
	@Value("${mosip.kernel.sms.password}")
	private String password;
	@Value("${mosip.kernel.sms.sender}")
	private String senderId;
	@Value("${mosip.kernel.sms.affiliate}")
	private String affiliate;
	@Value("${mosip.kernel.sms.unicode:1}")
	private String unicode;
	@Value("${mosip.kernel.sms.number.length}")
	private int numberLength;

	@Override
	public SMSResponseDto sendSms(String contactNumber, String message) {
		SMSResponseDto smsResponseDTO = new SMSResponseDto();
		validateInput(contactNumber);
		LOGGER.info("SMS request: "+contactNumber);
		contactNumber = countryCode.concat(contactNumber);
		try {
			MtnMessageRequest.send(buildAPIURl(), contactNumber, message);
		} catch (HttpClientErrorException | HttpServerErrorException | UnsupportedEncodingException | IOException e) {
			// build the docker image
			e.printStackTrace();
			System.out.println(e.getMessage());

			throw new RuntimeException(e);
		}
		smsResponseDTO.setMessage(SmsPropertyConstant.SUCCESS_RESPONSE.getProperty());
		smsResponseDTO.setStatus("success");
		return smsResponseDTO;
	}

	private void validateInput(String contactNumber) {
		if (!StringUtils.isNumeric(contactNumber) || contactNumber.length() < numberLength
				|| contactNumber.length() > numberLength) {
			throw new InvalidNumberException(SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorCode(),
					SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorMessage() + numberLength
							+ SmsPropertyConstant.SUFFIX_MESSAGE.getProperty());
		}
	}

	private String buildAPIURl() {
		String mtnApiURl = apiUrl + "?" + SmsPropertyConstant.PROVIDER_AFFILIATE.getProperty() + "=" + affiliate + "&"
				+ SmsPropertyConstant.PROVIDER_CLIENT.getProperty() + "=" + clienId + "" + "&"
				+ SmsPropertyConstant.PROVIDER_PASSWORD.getProperty() + "=" + password + "&"
				+ SmsPropertyConstant.SENDER_ID.getProperty() + "=" + senderId + "&";

		return mtnApiURl;
	}

}
