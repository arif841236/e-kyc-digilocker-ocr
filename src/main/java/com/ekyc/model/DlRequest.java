package com.ekyc.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DlRequest {

	@NotEmpty(message = "Please fill the consent.")
	private String consent;

	@NotEmpty(message = "DL number should not be empty or null.")
	private String dlNo;

	@NotEmpty(message = "Please enter dob.")
	@Pattern(regexp = "^([0-2][\\d]||3[0-1])-(0[\\d]||1[0-2])-([\\d][\\d])?[\\d][\\d]$",message = "Please enter dob in valid formate(dd-mm-yyyy)")
	private String dob;

	private boolean additionalDetails;
	
	private String source;
}
