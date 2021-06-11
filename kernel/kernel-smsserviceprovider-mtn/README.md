## kernel-smsserviceprovider-mtn

 [Background & Design]()
 

 [API Documentation ]
 
 ```
 mvn javadoc:javadoc

 ```
 
**Properties to be added in Spring application environment using this component**

[application-dev.properties](../../config/application-dev.properties)

 ```
#-----------------------------VID Properties--------------------------------------
mosip.kernel.sms.enabled=true
mosip.kernel.sms.country.code=224
mosip.kernel.sms.number.length=9

#----------mtn gateway---------------
mosip.kernel.sms.api=https://mtnguineevas.com/httpsms/Send
mosip.kernel.sms.sender=WURI-GUINEE
mosip.kernel.sms.affiliate=999
auth.server.admin.validate.url=<auth server validate url>

 ```
 
 **Maven Dependency**
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-smsserviceprovider-mtn</artifactId>
			<version>${version}</version>
		</dependency>

 ```
 



**Usage Sample:**

Autowired interface 

```
	@Autowired
	private SMSServiceProvider smsServiceProvider;
```
Call the method 

Example:
 
 ```
	 smsServiceProvider.sendSms(contactNumber, contentMessage);

```
	

 
