package com.ekyc.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Result {

	private String status;
	private boolean duplicate;
	private boolean nameMatch;
	private boolean dobMatch;
}
