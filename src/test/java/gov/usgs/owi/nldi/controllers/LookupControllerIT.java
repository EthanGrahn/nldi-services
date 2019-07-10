package gov.usgs.owi.nldi.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONArrayAs;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;

@EnableWebMvc
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class LookupControllerIT extends BaseIT {

	@Value("${serverContextPath}")
	private String context;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	private static final String RESULT_FOLDER  = "lookup/";

	@Before
	public void setup() {
		urlRoot = "http://localhost:" + port + context;
	}

	//DataSources Testing
	@Test
	public void getDataSourcesTest() throws Exception {
		String actualbody = assertEntity(restTemplate,
				"/api",
				HttpStatus.OK.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				null,
				false,
				false);
		assertThat(new JSONArray(actualbody),
				sameJSONArrayAs(new JSONArray(getCompareFile(RESULT_FOLDER, "dataSources.json"))).allowingAnyArrayOrdering());
	}

	//Features Testing
	@Test
	public void getFeaturesTest() throws Exception {
		assertEntity(restTemplate,
				"/api/comid",
				HttpStatus.BAD_REQUEST.value(),
				null,
				null,
				null,
				"{\"status\":400,\"error\":\"Bad Request\",\"message\":\"This functionality is not implemented.\",\"path\":\"/nldi/api/comid\"}",
				true,
				true);
	}

	//Object Testing Catchment
	@Test
	public void getComidTest() throws Exception {
		assertEntity(restTemplate,
				"/api/comid/13297246",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER, "comid_13297246.json"),
				true,
				false);
	}

	//Linked Object Testing WQP
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpTest() throws Exception {
		assertEntity(restTemplate,
				"/api/wqp/USGS-05427880",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880.json"),
				true,
				false);
	}

	//Linked Object Testing huc12pp
	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void gethuc12ppTest() throws Exception {
		assertEntity(restTemplate,
				"/api/huc12pp/070900020604",
				HttpStatus.OK.value(),
				null,
				null,
				BaseController.MIME_TYPE_GEOJSON,
				getCompareFile(RESULT_FOLDER_HUC, "huc12pp_070900020604.json"),
				true,
				false);
	}

	//Navigation Types Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTest() throws Exception {
		assertEntity(restTemplate,
				"/api/wqp/USGS-05427880/navigate",
				HttpStatus.OK.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				getCompareFile(RESULT_FOLDER, "wqp_USGS-05427880.json"),
				true,
				false);
	}

	@Test
	public void getNavigationTypesNotFoundTest() throws Exception {
		assertEntity(restTemplate,
				"/api/wqx/USGS-05427880/navigate",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				null,
				true,
				false);

		assertEntity(restTemplate,
				"/api/wqp/USGX-05427880/navigate",
				HttpStatus.NOT_FOUND.value(),
				null,
				null,
				MediaType.APPLICATION_JSON_UTF8_VALUE,
				null,
				true,
				false);
	}

}
