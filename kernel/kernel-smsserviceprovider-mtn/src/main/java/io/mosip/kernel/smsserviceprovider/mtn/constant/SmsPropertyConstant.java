package io.mosip.kernel.smsserviceprovider.mtn.constant;

/**
 * This enum provides all the constant for sms notification.
 * 
 * @author Ritesh sinha
 * @since 1.0.0
 *
 */
public enum SmsPropertyConstant {

	PROVIDER_AFFILIATE("affiliate"),
	PROVIDER_PASSWORD("password"),
	PROVIDER_CLIENT("client"),
	AUTH_KEY("authkey"),
	SMS_MESSAGE("text"), 
	RECIPIENT_NUMBER("phone"), 
	COUNTRY_CODE("country"), 
	ROUTE("route"),
	SENDER_ID("from"), 
	VENDOR_RESPONSE_SUCCESS("success"), 
	SUCCESS_RESPONSE("Sms Request Sent"), UNICODE("unicode"),
	PROJECT_NAME("kernel-notification-sms"), 
	SUFFIX_MESSAGE(" digits"), 
	NUMBERS("numbers");

	/**
	 * The property for sms notification.
	 */
	private String property;

	/**
	 * The constructor to set sms property.
	 * 
	 * @param property the property to set.
	 */
	private SmsPropertyConstant(String property) {
		this.property = property;
	}

	/**
	 * Getter for sms property.
	 * 
	 * @return the sms property.
	 */
	public String getProperty() {
		return property;
	}

}
