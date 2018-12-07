package io.mosip.preregistration.booking.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RequestDto<T> {
	
	private String id;
	private String ver;
	private Timestamp reqTime;
	private T request;

}
