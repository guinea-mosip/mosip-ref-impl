package io.mosip.kernel.smsserviceprovider.mtn.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import io.mosip.kernel.core.exception.UnsupportedEncodingException;
import io.mosip.kernel.smsserviceprovider.mtn.constant.SmsPropertyConstant;

/**
 * 
 * @author condeis
 *
 */
public class MtnMessageRequest {
	public static void send(String url, String contact, String message)
			throws UnsupportedEncodingException, MalformedURLException, IOException {
		message = URLEncoder.encode(message, "ISO-8859-1");
		String https_url = url + SmsPropertyConstant.RECIPIENT_NUMBER.getProperty() + "=" + contact + "&"
				+ SmsPropertyConstant.SMS_MESSAGE.getProperty() + "=" + message;
		 
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(https_url).openConnection();
			urlConnection.setUseCaches(false);
			urlConnection.setDoOutput(true); // Triggers POST.
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("accept-charset", "ISO-8859-1");
			urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=ISO-8859-1");
			urlConnection.getInputStream();
		
	}

}
