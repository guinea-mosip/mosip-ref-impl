package io.mosip.authentication.demo.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.mosip.authentication.demo.helper.ApplicationMessageContext;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.authentication.demo.dto.AuthRequestDTO;
import io.mosip.authentication.demo.dto.AuthTypeDTO;
import io.mosip.authentication.demo.dto.CryptomanagerRequestDto;
import io.mosip.authentication.demo.dto.EncryptionRequestDto;
import io.mosip.authentication.demo.dto.EncryptionResponseDto;
import io.mosip.authentication.demo.dto.JWTSignatureRequestDto;
import io.mosip.authentication.demo.dto.JWTSignatureResponseDto;
import io.mosip.authentication.demo.dto.OtpRequestDTO;
import io.mosip.authentication.demo.dto.RequestDTO;
import io.mosip.authentication.demo.helper.CryptoUtility;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

/**
 * The Class IdaController.
 *
 * @author Sanjay Murali
 */
@Component
public class IdaController {

	private static final String DEFAULT_SUBID = "0";

	@Autowired
	private Environment env;

	/** The sign refid. */
	@Value("${mosip.sign.refid:SIGN}")
	private String signRefid;

	private static final String SSL = "SSL";

	ObjectMapper mapper = new ObjectMapper();

//	@FXML
//	ComboBox<String> irisCount;

	@FXML
	ComboBox<String> fingerCount;

	@FXML
	private TextField idValue;

	@FXML
	private TextField idValueVID;

	@FXML
	private CheckBox fingerAuthType;

//	@FXML
//	private CheckBox irisAuthType;

//	@FXML
//	private CheckBox faceAuthType;

	@FXML
	private CheckBox otpAuthType;

//	@FXML
//	private ComboBox<String> idTypebox;

	@FXML
	private TextField otpValue;

	@FXML
	private AnchorPane otpAnchorPane;

	@FXML
	private AnchorPane bioAnchorPane;

	@FXML
	private TextField responsetextField;

//	@FXML
//	private ImageView img;

//	@FXML
//	private Button requestOtp;

	@FXML
	private Button sendAuthRequest;

	private String capture;

	private String previousHash;

	private ObjectMapper objectMapper = new ObjectMapper();

	private String otpDefaultValue;

	private String uinDefaultValue;

	private String vidDefaultValue;

	private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(true);

	@FXML
	private Label tsLabel;

	@FXML
	private Button tsButton;

	@FXML
	private HBox tsHBox;

	private  String idType;

	Logger logger = LoggerFactory.getLogger(IdaController.class);

	ResourceBundle messageResource;

	@FXML
	private void initialize() {
		responsetextField.setText(null);
		messageResource = ApplicationMessageContext.getMessageResourceBundles();

		otpDefaultValue = messageResource.getString("enterOtpValue");
		uinDefaultValue = messageResource.getString("INU");
		vidDefaultValue = messageResource.getString("VID");

//		ObservableList<String> idTypeChoices = FXCollections.observableArrayList("UIN", "VID", "USERID");
//		ObservableList<String> fingerCountChoices = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7",
//				"8", "9", "10");
		ObservableList<String> fingerCountChoices = FXCollections.observableArrayList("1- Auriculaire gauche", "2- Annulaire gauche", "3- Majeur gauche",
				"4- Index gauche", "5- Pouce gauche", "6- Pouce droit", "7- Index droit",
				"8- Majeur droit", "9- Annulaire droit", "10- Auriculaire droit");
		fingerCount.setItems(fingerCountChoices);
		fingerCount.getSelectionModel().select(0);

//		ObservableList<String> irisCountChoices = FXCollections.observableArrayList("Left Iris", "Right Iris", "Both Iris");
//		irisCount.setItems(irisCountChoices);
//		irisCount.getSelectionModel().select(0);

//		idTypebox.setItems(idTypeChoices);
//		idTypebox.setValue("UIN");
		otpAnchorPane.setDisable(true);
		bioAnchorPane.setDisable(true);
		responsetextField.setDisable(true);
		sendAuthRequest.setDisable(true);

		idValue.textProperty().addListener((observable, oldValue, newValue) -> {
			updateSendButton();
		});

		otpValue.textProperty().addListener((observable, oldValue, newValue) -> {
			updateSendButton();
		});

		otpValue.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (otpValue.isEditable() && otpValue.getText().equals(otpDefaultValue)) {
				otpValue.setText("");
				otpValue.setStyle("-fx-text-fill: #020F59;");
			}
		});

		idValue.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (idValue.isEditable() && idValue.getText().equals(uinDefaultValue)) {
				idValue.setText("");
			}
		});

		idValueVID.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (idValueVID.isEditable()  && idValueVID.getText().equals(vidDefaultValue))
				idValueVID.setText("");
		});

		switchedOn.addListener((a, b, c) -> {
			if (c) {
				tsLabel.setText("");
				tsLabel.toFront();

				idValueVID.setEditable(false);
				idValueVID.setStyle("-fx-text-fill: grey;");
				idValueVID.setText(vidDefaultValue);

				idValue.setEditable(true);
				idValue.setStyle("-fx-text-fill: #020F59;");
				idValue.setText(uinDefaultValue);

				idType = uinDefaultValue;
			} else {
				tsLabel.setText("");
				tsHBox.setStyle("-fx-border-color: #020F59;-fx-background-color: white;");
				tsButton.toFront();
				idValueVID.setEditable(true);
				idValueVID.setStyle("-fx-text-fill: #020F59;");
				idValueVID.setText(vidDefaultValue);

				idValue.setEditable(false);
				idValue.setStyle("-fx-text-fill: grey;");
				idValue.setText(uinDefaultValue);

				idType = vidDefaultValue;
			}
		});
//		otpValue.setEditable(false);
		init();
	}

	private void init() {
		otpAnchorPane.setStyle("-fx-border-color: lightgrey;");
		bioAnchorPane.setStyle("-fx-border-color: lightgrey;");

		idValueVID.setEditable(false);
		idValueVID.setStyle("-fx-text-fill: grey;");
		idValueVID.setText(vidDefaultValue);

		idValue.setEditable(true);
		idValue.setStyle("-fx-text-fill: #020F59;");
		idValue.setText(uinDefaultValue);
		idType = uinDefaultValue;

		tsLabel.setText("");
		tsLabel.toFront();
		tsLabel.setVisible(false);
		tsLabel.setAlignment(Pos.CENTER);
		tsLabel.prefWidthProperty().bind(tsHBox.widthProperty().divide(2));
		tsLabel.prefHeightProperty().bind(tsHBox.heightProperty());
		tsLabel.setOnMouseClicked((e) -> {
			switchedOn.set(!switchedOn.get());
		});

		tsButton.prefWidthProperty().bind(tsHBox.widthProperty().divide(2));
		tsButton.prefHeightProperty().bind(tsHBox.heightProperty());
		tsButton.setOnAction((e) -> {
			switchedOn.set(!switchedOn.get());
		});
	}

	@FXML
	private void onFingerPrintAuth() {
		updateBioCapture();
	}

	private void updateBioCapture() {
		capture = null;
		previousHash = null;
		updateBioPane();
		updateSendButton();
	}

//	@FXML
//	private void onIrisAuth() {
//		updateBioCapture();
//	}

//	@FXML
//	private void onFaceAuth() {
//		updateBioCapture();
//	}

	private void updateSendButton() {
		if(idValue.getText() == null || idValue.getText().trim().isEmpty() || idValueVID.getText() == null
				|| idValueVID.getText().trim().isEmpty()) {
			sendAuthRequest.setDisable(true);
			return;
		}

		if(otpAuthType.isSelected()) {
			if(otpValue.getText().trim().isEmpty() || otpValue.getText().equals(otpDefaultValue)) {
				sendAuthRequest.setDisable(true);
				return;
			}
		}

		if(fingerAuthType.isSelected()) {
			if(capture == null) {
				sendAuthRequest.setDisable(true);
				return;
			}
		}

		sendAuthRequest.setDisable(!(fingerAuthType.isSelected() || otpAuthType.isSelected()));
	}

	private void updateBioPane() {
		if (fingerAuthType.isSelected()) {
			bioAnchorPane.setDisable(false);
			bioAnchorPane.setStyle("-fx-border-color: #020F59;");
			fingerCount.getSelectionModel().select(0);
		} else {
			bioAnchorPane.setDisable(true);
			bioAnchorPane.setStyle("-fx-border-color: lightgrey;");
			fingerCount.getSelectionModel().select(0);
		}
//		irisCount.setDisable(!irisAuthType.isSelected());
//		fingerCount.setDisable(!fingerAuthType.isSelected());
	}


	@FXML
	private void onOTPAuth() {
		responsetextField.setText(null);
		otpAnchorPane.setDisable(!otpAuthType.isSelected());
		updateSendButton();

		if (otpAuthType.isSelected()) {
			otpValue.setText(otpDefaultValue);
			otpValue.setStyle("-fx-text-fill: grey;");
			otpAnchorPane.setStyle("-fx-border-color: #020F59;");
		} else {
			otpValue.setText("");
			otpAnchorPane.setStyle("-fx-border-color: lightgrey;");
		}
	}

//	@FXML
//	private void onIdTypeChange() {
//		responsetextField.setText(null);
//	}

	@FXML
	private void onSubTypeSelection() {
		responsetextField.setText(null);
	}

	@FXML
	private void onCapture() throws Exception {
		responsetextField.setFont(Font.font("Times New Roman", javafx.scene.text.FontWeight.EXTRA_BOLD, 20));
		previousHash = null;
		List<String> bioCaptures = new ArrayList<>();

		String fingerCapture;
		if (fingerAuthType.isSelected()) {
			fingerCapture = captureFingerprint();
			if(!fingerCapture.contains("\"biometrics\"")) {
				updateSendButton();
				return;
			}
			bioCaptures.add(fingerCapture);
			capture = fingerCapture;
		}
	//	capture = combineCaptures(bioCaptures);

		updateSendButton();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
//	private String combineCaptures(List<String> bioCaptures) {
//		List<String> captures = bioCaptures.stream()
//				.filter(obj -> obj != null)
//				.filter(str -> str.contains("\"biometrics\""))
//				.collect(Collectors.toList());
//
//		if(captures.isEmpty()) {
//			return null;
//		}
//
///		if(captures.size() == 1) {
//			return captures.get(0);
//		}
//
//		LinkedHashMap<String, Object> identity = new LinkedHashMap<String, Object>();
//		List<Map<String, Object>> biometricsList = captures.stream()
//				.map(obj -> {
//					try {
//						return objectMapper.readValue(obj, Map.class);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					return null;
//				})
//				.map(map -> map.get("biometrics"))
//				.filter(obj -> obj instanceof List)
//				.map(obj -> (List<Map>) obj )
//				.flatMap(list -> list.stream())
//				.filter(obj -> obj instanceof Map)
//				.map(obj -> (Map<String, Object>)obj)
//				.collect(Collectors.toList());
//
//		identity.put("biometrics", biometricsList);
//
//		try {
//			return objectMapper.writeValueAsString(identity);
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	/**
	 *
	 * @return
	 */
	private String getCaptureRequestTemplate() {
		String requestBody = new String(cryptoUtil.decodeBase64(env.getProperty("ida.request.captureRequest.template")));
		System.out.println(requestBody);
		return requestBody;
	}

	private String captureFingerprint() throws Exception {
		responsetextField.setText(messageResource.getString("captureFingerPrint"));
		responsetextField.setStyle("-fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold");

		String requestBody = getCaptureRequestTemplate();

		requestBody = requestBody.replace("$timeout", env.getProperty("ida.request.captureFinger.timeout"))
				.replace("$count", getFingerCount()).
						replace("$deviceId", env.getProperty("ida.request.captureFinger.deviceId")).
						replace("$domainUri", env.getProperty("ida.request.captureFinger.domainUri")).
						replace("$deviceSubId", getFingerDeviceSubId()).
						replace("$captureTime", getCaptureTime()).
						replace("$previousHash", getPreviousHash()).
						replace("$requestedScore",env.getProperty("ida.request.captureFinger.requestedScore")).
						replace("$type",env.getProperty("ida.request.captureFinger.type")).
						replace("$bioSubType",getBioSubType(getFingerCount(), env.getProperty("ida.request.captureFinger.bioSubType"))).
						replace("$name",env.getProperty("ida.request.captureFinger.name")).
						replace("$value",env.getProperty("ida.request.captureFinger.value")).
						replace("$env",env.getProperty("ida.request.captureFinger.env"));

		return capturebiometrics(requestBody);
	}


	private String getFingerDeviceSubId() {
		return env.getProperty("finger.device.subid",DEFAULT_SUBID);
	}

//	private String getIrisDeviceSubId() {
//		String irisSubId = env.getProperty("iris.device.subid");
//		if(irisSubId == null) {
//			if(irisCount.getSelectionModel().getSelectedIndex() == 0) {
//				irisSubId = String.valueOf(1);
//			} else if(irisCount.getSelectionModel().getSelectedIndex() == 1) {
//				irisSubId = String.valueOf(2);
//			} else {
//				irisSubId = String.valueOf(3);
//			}
//		}
//		return irisSubId;
//	}

//	private String getFaceDeviceSubId() {
//		return env.getProperty("face.device.subid",DEFAULT_SUBID);
//	}

//	private String captureIris() throws Exception {
//		responsetextField.setText("Capturing Iris...");
//		responsetextField.setStyle("-fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold");
//
//		String requestBody = getCaptureRequestTemplate();
//
//		requestBody = requestBody.replace("$timeout", env.getProperty("ida.request.captureIris.timeout"))
//				.replace("$count", getIrisCount()).
//						replace("$deviceId", env.getProperty("ida.request.captureIris.deviceId")).
//						replace("$domainUri", env.getProperty("ida.request.captureIris.domainUri")).
//						replace("$deviceSubId", getIrisDeviceSubId()).
//						replace("$captureTime", getCaptureTime()).
//						replace("$previousHash", getPreviousHash()).
//						replace("$requestedScore",env.getProperty("ida.request.captureIris.requestedScore")).
//						replace("$type",env.getProperty("ida.request.captureIris.type")).
//						replace("$bioSubType",getBioSubType(getIrisCount(), env.getProperty("ida.request.captureIris.bioSubType"))).
//						replace("$name",env.getProperty("ida.request.captureIris.name")).
//						replace("$value",env.getProperty("ida.request.captureIris.value")).
//						replace("$env",env.getProperty("ida.request.captureIris.env"));
//
//		return capturebiometrics(requestBody);
//	}

//	private String captureFace() throws Exception {
//		responsetextField.setText("Capturing Face...");
//		responsetextField.setStyle("-fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold");
//
//		String requestBody = getCaptureRequestTemplate();
//
//		requestBody = requestBody.replace("$timeout", env.getProperty("ida.request.captureFace.timeout"))
//				.replace("$count", getFaceCount()).
//						replace("$deviceId", env.getProperty("ida.request.captureFace.deviceId")).
//						replace("$domainUri", env.getProperty("ida.request.captureFace.domainUri")).
//						replace("$deviceSubId", getFaceDeviceSubId()).
//						replace("$captureTime", getCaptureTime()).
//						replace("$previousHash", getPreviousHash()).
//						replace("$requestedScore",env.getProperty("ida.request.captureFace.requestedScore")).
//						replace("$type",env.getProperty("ida.request.captureFace.type")).
//						replace("$bioSubType",getBioSubType(getFaceCount(), env.getProperty("ida.request.captureFace.bioSubType"))).
//						replace("$name",env.getProperty("ida.request.captureFace.name")).
//						replace("$value",env.getProperty("ida.request.captureFace.value")).
//						replace("$env",env.getProperty("ida.request.captureFace.env"));
//
//		return capturebiometrics(requestBody);
//	}

	private String getPreviousHash() {
		return previousHash == null ? "" : previousHash;
	}

	private String getFingerCount() {
		return fingerCount.getValue() == null ? String.valueOf(1) : ""+fingerCount.getValue().charAt(0);
	}

	private String getBioSubType(String count, String bioValue) {
		if(count.equalsIgnoreCase("1")) {
			return  "\"" + bioValue + "\"";
		}
		String finalStr = "\""+ bioValue + "\"";
		for(int i = 2; i<=Integer.parseInt(count); i++) {
			finalStr = finalStr + "," + "\""+ bioValue + "\"";
		}

		return finalStr;
	}

//	private String getIrisCount() {
//		return String.valueOf(irisCount.getSelectionModel().getSelectedIndex() + 1);
//	}

//	private String getFaceCount() {
//		return String.valueOf(1);
//	}

	private String getCaptureTime() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
		df.setTimeZone(tz);
		String nowAsISO = df.format(new Date());
		return nowAsISO;
	}

	@SuppressWarnings("rawtypes")
	private String capturebiometrics(String requestBody) throws Exception {
		System.out.println("Capture request:\n" + requestBody);
		logger.info("Capture request:\n" + requestBody);
		CloseableHttpClient client = HttpClients.createDefault();
		StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
		HttpUriRequest request = RequestBuilder.create("CAPTURE").setUri(env.getProperty("ida.captureRequest.uri"))
				.setEntity(requestEntity).build();
		CloseableHttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			response = client.execute(request);

			InputStream inputStram = response.getEntity().getContent();
			BufferedReader bR = new BufferedReader(new InputStreamReader(inputStram));
			String line = null;
			while ((line = bR.readLine()) != null) {
				stringBuilder.append(line);
			}
			bR.close();
		} catch (IOException e) {
			responsetextField.setText("Device connectivity failed....");
			responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
			e.printStackTrace();
			logger.error("==========Error: " + e.getMessage());
		}
		String result = stringBuilder.toString();
		String error = null;

		List data = (List) objectMapper.readValue(result.getBytes(), Map.class).get("biometrics");
		if(data == null) {
			responsetextField.setText(result);
			responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
		} else {
			for (int j = 0; j < data.size(); j++) {
				Map e = (Map) data.get(j);
				Map errorMap = (Map) e.get("error");
				error = errorMap.get("errorCode").toString();
				if (error.equals(DEFAULT_SUBID) || error.equals("100")) {
					responsetextField.setText(messageResource.getString("captureSuccess"));
					responsetextField.setStyle("-fx-text-fill: green; -fx-font-size: 20px; -fx-font-weight: bold");
					ObjectMapper objectMapper = new ObjectMapper();
					List dataList = (List) objectMapper.readValue(result.getBytes(), Map.class).get("biometrics");
					for (int i = 0; i < dataList.size(); i++) {
						Map b = (Map) dataList.get(i);
						String dataJws = (String) b.get("data");
						Map dataMap = objectMapper.readValue(CryptoUtil.decodeBase64(dataJws.split("\\.")[1]), Map.class);
						System.out.println((i+1) + " Bio-type: " + dataMap.get("bioType") + " Bio-sub-type: " +  dataMap.get("bioSubType"));
						previousHash = (String) b.get("hash");
						System.out.println(" 548 Hash: "+previousHash);
					}
				} else {
					responsetextField.setText(messageResource.getString("captureFailed"));
					responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
					break;
				}
			}
		}

		System.out.println(result);
		return result;
	}

	@SuppressWarnings("rawtypes")
	@FXML
	private void onRequestOtp() {
		responsetextField.setText(null);
		OtpRequestDTO otpRequestDTO = new OtpRequestDTO();
		otpRequestDTO.setId("mosip.identity.otp");
		otpRequestDTO.setIndividualId(idValue.getText());
		otpRequestDTO.setIndividualIdType(idType);
		otpRequestDTO.setOtpChannel(Collections.singletonList("email"));
		otpRequestDTO.setRequestTime(getUTCCurrentDateTimeISOString());
		otpRequestDTO.setTransactionID(getTransactionID());
		otpRequestDTO.setVersion("1.0");

		try {
			RestTemplate restTemplate = createTemplate();
			String reqJson = mapper.writeValueAsString(otpRequestDTO);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("signature", getSignature(reqJson));
			httpHeaders.add("Content-type", MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<String> httpEntity = new HttpEntity<>(reqJson,httpHeaders);
			ResponseEntity<Map> response = restTemplate.exchange(
					env.getProperty("ida.otp.url"),
					HttpMethod.POST, httpEntity, Map.class);
			System.err.println(response);

			if (response.getStatusCode().is2xxSuccessful()) {
				List errors = ((List) response.getBody().get("errors"));
				boolean status = errors == null || errors.isEmpty();
				String responseText = status ? messageResource.getString("otpSuccess") : messageResource.getString("otpFailure");
				if (status) {
					responsetextField.setStyle("-fx-text-fill: green; -fx-font-size: 20px; -fx-font-weight: bold");
					otpValue.setEditable(true);
					otpValue.setText(otpDefaultValue);
				} else {
					responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
				}
				responsetextField.setText(responseText);
			} else {
				responsetextField.setText(messageResource.getString("otpFailuresError"));
				responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
			}
		} catch (KeyManagementException | NoSuchAlgorithmException | JsonProcessingException | UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("Error: " + e);
		}
	}

	private String getSignature(String reqJson) throws KeyManagementException, NoSuchAlgorithmException, UnsupportedEncodingException {
		return sign(reqJson, false);
	}

	public String sign(String data, boolean isPayloadRequired)
			throws KeyManagementException, NoSuchAlgorithmException, UnsupportedEncodingException {
		turnOffSslChecking();
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				String authToken = generateAuthToken();
				if (authToken != null && !authToken.isEmpty()) {
					request.getHeaders().set("Cookie", "Authorization=" + authToken);
				}
				return execution.execute(request, body);
			}
		};

		restTemplate.setInterceptors(Collections.singletonList(interceptor));

		JWTSignatureRequestDto request = new JWTSignatureRequestDto();
		request.setApplicationId("IDA");
		request.setDataToSign(CryptoUtil.encodeBase64(data.getBytes("UTF-8")));
		request.setIncludeCertHash(true);
		request.setIncludeCertificate(true);
		request.setIncludePayload(isPayloadRequired);
		request.setReferenceId(signRefid);
		RequestWrapper<JWTSignatureRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(request);
		HttpEntity<RequestWrapper<JWTSignatureRequestDto>> requestEntity = new HttpEntity<>(requestWrapper);
		ResponseEntity<ResponseWrapper<JWTSignatureResponseDto>> exchange = restTemplate.exchange(
				env.getProperty("ida.internal.jwtSign.url"), HttpMethod.POST, requestEntity,
				new ParameterizedTypeReference<ResponseWrapper<JWTSignatureResponseDto>>() {
				});
		return exchange.getBody().getResponse().getJwtSignedData();
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@FXML
	private void onSendAuthRequest() throws Exception {
		responsetextField.setText(null);
		responsetextField.setStyle("-fx-text-fill: black; -fx-font-size: 20px; -fx-font-weight: bold");
		responsetextField.setText(messageResource.getString("prepareAuthReq"));
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		// Set Auth Type
		AuthTypeDTO authTypeDTO = new AuthTypeDTO();
		authTypeDTO.setBio(fingerAuthType.isSelected());
		authTypeDTO.setOtp(otpAuthType.isSelected());
		authRequestDTO.setRequestedAuth(authTypeDTO);
		// set Individual Id
		authRequestDTO.setIndividualId(idValue.getText());
		// Set Individual Id type
// Old Code		authRequestDTO.setIndividualIdType("UIN");
		authRequestDTO.setIndividualIdType(idType);
		authRequestDTO.setEnv(env.getProperty("ida.request.captureFinger.env"));
		authRequestDTO.setDomainUri(env.getProperty("ida.request.captureFinger.domainUri"));
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setTimestamp(getUTCCurrentDateTimeISOString());

		if (otpAuthType.isSelected()) {
			requestDTO.setOtp(otpValue.getText());
		}

		Map<String, Object> identityBlock = mapper.convertValue(requestDTO, Map.class);
		if (fingerAuthType.isSelected()) {
			identityBlock.put("biometrics", mapper.readValue(capture, Map.class).get("biometrics"));
		}
		responsetextField.setText("Encrypting Auth Request...");
		System.out.println("******* Request before encryption ************ \n\n");
		System.out.println(mapper.writeValueAsString(identityBlock));
		EncryptionRequestDto encryptionRequestDto = new EncryptionRequestDto();
		encryptionRequestDto.setIdentityRequest(identityBlock);
		EncryptionResponseDto kernelEncrypt = null;
		try {
			kernelEncrypt = kernelEncrypt(encryptionRequestDto, false);
		} catch (Exception e) {
			e.printStackTrace();
			responsetextField.setText(messageResource.getString("authEncryptFail"));
			return;
		}

		responsetextField.setText(messageResource.getString("authenticating"));
		// Set request block
		authRequestDTO.setRequest(requestDTO);

		authRequestDTO.setTransactionID(getTransactionID());
		authRequestDTO.setRequestTime(getUTCCurrentDateTimeISOString());
		authRequestDTO.setConsentObtained(true);
		authRequestDTO.setId(getAuthRequestId());
		authRequestDTO.setVersion("1.0");
		authRequestDTO.setThumbprint(kernelEncrypt.getThumbprint());

		Map<String, Object> authRequestMap = mapper.convertValue(authRequestDTO, Map.class);
		authRequestMap.replace("request", kernelEncrypt.getEncryptedIdentity());
		authRequestMap.replace("requestSessionKey", kernelEncrypt.getEncryptedSessionKey());
		authRequestMap.replace("requestHMAC", kernelEncrypt.getRequestHMAC());
		RestTemplate restTemplate = createTemplate();

		String reqJson = mapper.writeValueAsString(authRequestMap);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("signature", getSignature(reqJson));
		httpHeaders.add("Content-type", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> httpEntity = new HttpEntity<>(reqJson,httpHeaders);

		String url = getUrl();
		System.out.println("Auth URL: " + url);
		System.out.println("Auth Request : \n" + new ObjectMapper().writeValueAsString(authRequestMap));
		try {
			ResponseEntity<Map> authResponse = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, Map.class);
			if (authResponse.getStatusCode().is2xxSuccessful()) {
				boolean status = (boolean) ((Map<String, Object>) authResponse.getBody().get("response")).get("authStatus");
				String response = status ? messageResource.getString("authSuccess") : messageResource.getString("authFailure");
				if (status) {
					responsetextField.setStyle("-fx-text-fill: green; -fx-font-size: 20px; -fx-font-weight: bold");
				} else {
					responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
				}
				responsetextField.setText(response);
			} else {
				responsetextField.setText(messageResource.getString("authFailuresError"));
				responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
			}

			System.out.println("Auth Response : \n" + new ObjectMapper().writeValueAsString(authResponse));
			System.out.println(authResponse.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			responsetextField.setText(messageResource.getString("authFailuresError"));
			responsetextField.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-font-weight: bold");
		}
	}

	private String getAuthRequestId() {
		return env.getProperty("authRequestId", "mosip.identity.auth");
	}

	private String getUrl() {
		return env.getProperty("ida.auth.url");
	}

	private EncryptionResponseDto kernelEncrypt(EncryptionRequestDto encryptionRequestDto, boolean isInternal)
			throws Exception {
		EncryptionResponseDto encryptionResponseDto = new EncryptionResponseDto();
		String identityBlock = mapper.writeValueAsString(encryptionRequestDto.getIdentityRequest());

		SecretKey secretKey = cryptoUtil.genSecKey();

		byte[] encryptedIdentityBlock = cryptoUtil.symmetricEncrypt(identityBlock.getBytes(), secretKey);
		encryptionResponseDto.setEncryptedIdentity(Base64.encodeBase64URLSafeString(encryptedIdentityBlock));

		X509Certificate certificate = getCertificate(identityBlock, isInternal);
		PublicKey publicKey = certificate.getPublicKey();
		byte[] encryptedSessionKeyByte = cryptoUtil.asymmetricEncrypt((secretKey.getEncoded()), publicKey);
		encryptionResponseDto.setEncryptedSessionKey(Base64.encodeBase64URLSafeString(encryptedSessionKeyByte));
		byte[] byteArr = cryptoUtil.symmetricEncrypt(
				HMACUtils2.digestAsPlainText(identityBlock.getBytes()).getBytes(), secretKey);
		encryptionResponseDto.setRequestHMAC(Base64.encodeBase64URLSafeString(byteArr));

		String thumbprint = CryptoUtil.encodeBase64(getCertificateThumbprint(certificate));
		encryptionResponseDto.setThumbprint(thumbprint);
		return encryptionResponseDto;
	}

	private byte[] getCertificateThumbprint(Certificate cert) throws CertificateEncodingException {
		return DigestUtils.sha256(cert.getEncoded());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public X509Certificate getCertificate(String data, boolean isInternal)
			throws KeyManagementException, RestClientException, NoSuchAlgorithmException, CertificateException {
		RestTemplate restTemplate = createTemplate();

		CryptomanagerRequestDto request = new CryptomanagerRequestDto();
		request.setApplicationId("IDA");
		request.setData(Base64.encodeBase64URLSafeString(data.getBytes(StandardCharsets.UTF_8)));
		String publicKeyId = env.getProperty("ida.reference.id");
		request.setReferenceId(publicKeyId);
		String utcTime = getUTCCurrentDateTimeISOString();
		request.setTimeStamp(utcTime);
		Map<String, String> uriParams = new HashMap<>();
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(
						env.getProperty("ida.certificate.url"))
				.queryParam("applicationId", "IDA")
				.queryParam("referenceId", publicKeyId);
		ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
		String certificate =  (String) ((Map<String, Object>) response.getBody().get("response")).get("certificate");

		String certificateTrimmed = trimBeginEnd(certificate);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(java.util.Base64.getDecoder().decode(certificateTrimmed)));
		return x509cert;
	}

	public static String trimBeginEnd(String pKey) {
		pKey = pKey.replaceAll("-*BEGIN([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("-*END([^-]*)-*(\r?\n)?", "");
		pKey = pKey.replaceAll("\\s", "");
		return pKey;
	}

	private RestTemplate createTemplate() throws KeyManagementException, NoSuchAlgorithmException {
		turnOffSslChecking();
		RestTemplate restTemplate = new RestTemplate();
		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				String authToken = generateAuthToken();
				if (authToken != null && !authToken.isEmpty()) {
					request.getHeaders().set("Cookie", "Authorization=" + authToken);
					request.getHeaders().set("Authorization", "Authorization=" + authToken);
				}
				return execution.execute(request, body);
			}
		};

		restTemplate.setInterceptors(Collections.singletonList(interceptor));
		return restTemplate;
	}

	private String generateAuthToken() {
		ObjectNode requestBody = mapper.createObjectNode();
		requestBody.put("clientId", env.getProperty("clientId"));
		requestBody.put("secretKey", env.getProperty("secretKey"));
		requestBody.put("appId", env.getProperty("appId"));
		RequestWrapper<ObjectNode> request = new RequestWrapper<>();
		request.setRequesttime(DateUtils.getUTCCurrentDateTime());
		request.setRequest(requestBody);
		ClientResponse response = WebClient
				.create(env.getProperty("ida.authmanager.url"))
				.post().syncBody(request).exchange().block();
		List<ResponseCookie> list = response.cookies().get("Authorization");
		if (list != null && !list.isEmpty()) {
			ResponseCookie responseCookie = list.get(0);
			return responseCookie.getValue();
		}
		return "";
	}

	@SuppressWarnings("unused")
	private HttpEntity<CryptomanagerRequestDto> getHeaders(CryptomanagerRequestDto req) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<CryptomanagerRequestDto>(req, headers);
	}


	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String arg1)
				throws CertificateException {
		}
	} };

	@Autowired
	private CryptoUtility cryptoUtil;

	public static void turnOffSslChecking() throws KeyManagementException, java.security.NoSuchAlgorithmException {
		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance(SSL);
		sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	public static String getUTCCurrentDateTimeISOString() {
		return DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime());
	}

	/**
	 * Gets the transaction ID.
	 *
	 * @return the transaction ID
	 */
	public static String getTransactionID() {
		return "1234567890";
	}

	@FXML
	private void onReset() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Confirm Reset");
		alert.setContentText(messageResource.getString("resetAlertMessages"));
		ButtonType okButton = new ButtonType(messageResource.getString("Yes"), ButtonBar.ButtonData.YES);
		ButtonType noButton = new ButtonType(messageResource.getString("No"), ButtonBar.ButtonData.NO);
		alert.getButtonTypes().setAll(okButton, noButton);
		alert.initStyle(StageStyle.UNDECORATED);
		alert.initModality(Modality.WINDOW_MODAL);
		alert.getDialogPane().getStylesheets().add("alert.css");
		alert.showAndWait().ifPresent(type -> {
			if (type.getButtonData().equals(ButtonType.YES.getButtonData())) {
				reset();
			}
		});
	}

	private void reset() {
		fingerCount.getSelectionModel().select(0);
//		irisCount.getSelectionModel().select(0);
		idValue.setText("");
		fingerAuthType.setSelected(false);
//		irisAuthType.setSelected(false);
//		faceAuthType.setSelected(false);
		otpAuthType.setSelected(false);
//		idTypebox.setValue("UIN");
		otpValue.setText("");
		otpAnchorPane.setDisable(true);
		bioAnchorPane.setDisable(true);
		responsetextField.setText("");
		sendAuthRequest.setDisable(false);
		capture = null;
		previousHash = null;
		updateBioPane();
		updateSendButton();
		init();
	}

//	@FXML
//	private void onOtpValueUpdate() {
//		updateSendButton();
//	}
}
