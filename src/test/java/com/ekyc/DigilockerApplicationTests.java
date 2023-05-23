package com.ekyc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.ekyc.service.IDigilockerService;

@SpringBootTest
class DigilockerApplicationTests {

	@Autowired
	IDigilockerService digilockerService;

	@Test
	void contextLoads() {
		Object userDetails = new Object();
		boolean flag = false;
		if(userDetails!= null) {
			flag = true;
		}
		assertEquals(true, flag);
	}
}
