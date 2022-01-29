package org.te.toll;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TollFeeCalculatorApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void passageEarlyMorningIsNotCharged() {
		String params = "Car/ULJ985/2022-01-11 05:01:01";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params,
				String.class)).contains("0");
	}

	@Test
	public void passageAfterSixIsCharged() {
		String params = "Car/ULJ986/2022-01-11 07:59:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params,
				String.class)).contains("22");
	}

	@Test
	public void multiplePassagesInTheSameHour_OnlyTheHighestIsCounted() {
		String params = "Car/ULJ987/2022-01-11 07:59:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params,
				String.class)).contains("22");
		String params2 = "Car/ULJ987/2022-01-11 08:25:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params2,
				String.class)).contains("22");
		String params3 = "Car/ULJ987/2022-01-11 08:50:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params3,
				String.class)).contains("22");
	}

	@Test
	public void multiplePassagesRuleMultipleTimesInSameDay_OnlyTheHighestIsCountedForEachHourCluster() {
		String params = "Car/ULJ988/2022-01-11 07:59:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params,
				String.class)).contains("22");
		String params2 = "Car/ULJ988/2022-01-11 08:25:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params2,
				String.class)).contains("22");
		String params3 = "Car/ULJ988/2022-01-11 08:50:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params3,
				String.class)).contains("22");
		String params4 = "Car/ULJ988/2022-01-11 17:59:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params4,
				String.class)).contains("38");
		String params5 = "Car/ULJ988/2022-01-11 18:25:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params5,
				String.class)).contains("38");
		String params6 = "Car/ULJ988/2022-01-11 19:50:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params6,
				String.class)).contains("38");
	}

	@Test
	public void sameVehicleHasTwoDifferentTypesInOneDay_ExceptionIsThrown() {
		String params = "Car/ULJ989/2022-01-11 07:59:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params,
				String.class)).contains("22");
		String params2 = "Tractor/ULJ989/2022-01-11 08:25:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params2,
				String.class)).contains("Vehicle type doesn't match a previous passage");
	}

	@Test
	public void passageOnNationalHoliday_IsNotCharged() {
		String params = "Car/ULJ990/2022-06-06 07:59:00";
		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/toll/get-fee/" + params,
				String.class)).contains("0");
	}

}
