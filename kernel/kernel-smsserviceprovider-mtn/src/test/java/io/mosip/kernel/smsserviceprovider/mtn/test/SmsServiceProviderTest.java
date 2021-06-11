package io.mosip.kernel.smsserviceprovider.mtn.test;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.smsserviceprovider.mtn.SMSServiceProviderBootApplication;
import io.mosip.kernel.smsserviceprovider.mtn.constant.SmsPropertyConstant;
import io.mosip.kernel.smsserviceprovider.mtn.dto.SmsServerResponseDto;
import io.mosip.kernel.smsserviceprovider.mtn.impl.SMSServiceProviderImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SMSServiceProviderBootApplication.class })
public class SmsServiceProviderTest {

	@Autowired
	SMSServiceProviderImpl service;

	@MockBean
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
	String unicode;
	@Value("${mosip.kernel.sms.number.length}")
	int numberLength;

	//@Test
	public void sendSmsTest() {

		UriComponentsBuilder sms = UriComponentsBuilder.fromHttpUrl(apiUrl)
				.queryParam(SmsPropertyConstant.SENDER_ID.getProperty(), senderId)
				.queryParam(SmsPropertyConstant.PROVIDER_CLIENT.getProperty(), clienId)
				.queryParam(SmsPropertyConstant.PROVIDER_PASSWORD.getProperty(), password)
				.queryParam(SmsPropertyConstant.PROVIDER_AFFILIATE.getProperty(), affiliate)
				.queryParam(SmsPropertyConstant.RECIPIENT_NUMBER.getProperty(), "224625739085")
				.queryParam(SmsPropertyConstant.COUNTRY_CODE.getProperty(), countryCode);

		SmsServerResponseDto serverResponse = new SmsServerResponseDto();
		serverResponse.setType("success");
		SMSResponseDto dto = new SMSResponseDto();
		dto.setStatus(serverResponse.getType());
		dto.setMessage("Sms Request Sent");

		when(restTemplate.getForEntity(sms.toUriString(), String.class))
				.thenReturn(new ResponseEntity<>(serverResponse.toString(), HttpStatus.OK));

		when(restTemplate.postForEntity(Mockito.anyString(), Mockito.eq(Mockito.any()), Object.class))
				.thenReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK));

		// assertThat(service.sendSms("8987876473", "your otp is 4646"),
		// is(dto));

	}

	@Test(expected = InvalidNumberException.class)
	public void invalidContactNumberTest() {
		service.sendSms("jsbchb", "hello your otp is 45373");
	}

	@Test(expected = InvalidNumberException.class)
	public void contactNumberMinimumThresholdTest() {
		service.sendSms("78978976", "hello your otp is 45373");
	}

	@Test(expected = InvalidNumberException.class)
	public void contactNumberMaximumThresholdTest() {
		service.sendSms("7897897458673484376", "hello your otp is 45373");
	}

	@Test
	public void validGateWayTest() {
		service.sendSms("625739085", "Test implementation dans le kernel");
	}

}