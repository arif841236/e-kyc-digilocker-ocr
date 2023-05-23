package com.ekyc.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PanRequest {

	@NotEmpty(message = "Please enter consent.")
	private String consent;

	@NotEmpty(message = "Please enter pan number.")
	@Pattern(regexp = "[A-Z]{5}[\\d+]{4}[A-Z]",message="Please enter valid pan number")
	private String pan;

	@NotEmpty(message = "Please enter name.")
	private String name;

	@NotEmpty(message = "Please enter dob.")
	@Pattern(regexp = "^([0-2][\\d]||3[0-1])/(0[\\d]||1[0-2])/([\\d][\\d])?[\\d][\\d]$",message = "Please enter dob in valid formate(dd/mm/yyyy)")
	private String dob;
	private String source;
}
