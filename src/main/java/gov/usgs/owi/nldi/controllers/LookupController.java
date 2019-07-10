package gov.usgs.owi.nldi.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.owi.nldi.NavigationMode;
import gov.usgs.owi.nldi.dao.BaseDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.NavigationDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.swagger.model.DataSource;
import gov.usgs.owi.nldi.transform.FeatureTransformer;
import io.swagger.annotations.ApiOperation;

@RestController
public class LookupController extends BaseController {

	private static final String DOWNSTREAM_DIVERSIONS = "downstreamDiversions";
	private static final String DOWNSTREAM_MAIN = "downstreamMain";
	private static final String UPSTREAM_MAIN = "upstreamMain";
	private static final String UPSTREAM_TRIBUTARIES = "upstreamTributaries";

	protected ConfigurationService configurationService;

	@Autowired
	public LookupController(LookupDao inLookupDao, StreamingDao inStreamingDao,
			Navigation inNavigation, Parameters inParameters, ConfigurationService configurationService,
			LogService inLogService) {
		super(inLookupDao, inStreamingDao, inNavigation, inParameters, configurationService.getRootUrl(), inLogService);
	}

	@ApiOperation(value="getDataSources", response=DataSource.class, responseContainer="List")
	@GetMapping(value="", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, Object>> getDataSources(HttpServletRequest request, HttpServletResponse response) {
		BigInteger logId = logService.logRequest(request);
		List<Map<String, Object>> rtn = new ArrayList<>();
		try {
			Map<String, Object> featureSource = new LinkedHashMap<>();

			//Manually add comid as a feature source.
			featureSource.put(LookupDao.SOURCE, Parameters.COMID);
			featureSource.put(LookupDao.SOURCE_NAME, "NHDPlus comid");
			featureSource.put(BaseDao.FEATURES, String.join("/", rootUrl, Parameters.COMID));
			rtn.add(featureSource);

			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put(LookupDao.ROOT_URL, rootUrl);
			rtn.addAll(lookupDao.getList(BaseDao.DATA_SOURCES, parameterMap));

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}

	@ApiOperation(value="getFeatures", hidden=true)
	@GetMapping(value="{featureSource}", produces=MediaType.APPLICATION_JSON_VALUE)
	public Object getFeatures(HttpServletRequest request, HttpServletResponse response, @PathVariable(LookupDao.FEATURE_SOURCE) String featureSource) throws IOException {
		BigInteger logId = logService.logRequest(request);
		try {
			response.sendError(HttpStatus.BAD_REQUEST.value(), "This functionality is not implemented.");
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return null;
	}

	@GetMapping(value="{featureSource}/{featureID}", produces=MediaType.APPLICATION_JSON_VALUE)
	public void getRegisteredFeature(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID) throws Exception {
		BigInteger logId = logService.logRequest(request);
		try (FeatureTransformer transformer = new FeatureTransformer(response, rootUrl)) {
			Map<String, Object> parameterMap = new HashMap<> ();
			parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
			parameterMap.put(Parameters.FEATURE_ID, featureID);
			addContentHeader(response);
			streamResults(transformer, BaseDao.FEATURE, parameterMap);
		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
	}

	@GetMapping(value="{featureSource}/{featureID}/navigate", produces=MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getNavigationTypes(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(LookupDao.FEATURE_SOURCE) String featureSource,
			@PathVariable(Parameters.FEATURE_ID) String featureID) throws UnsupportedEncodingException {
		BigInteger logId = logService.logRequest(request);
		Map<String, Object> rtn = new LinkedHashMap<>();
		try {
			//Verify that the feature source and identifier are valid
			Map<String, Object> parameterMap = new HashMap<>();
			parameterMap.put(LookupDao.FEATURE_SOURCE, featureSource);
			parameterMap.put(Parameters.FEATURE_ID, featureID);
			List<Map<String, Object>> results = lookupDao.getList(BaseDao.FEATURE, parameterMap);

			if (null == results || results.isEmpty()) {
				response.setStatus(HttpStatus.NOT_FOUND.value());
			} else {
				rtn.put(UPSTREAM_MAIN, 
						String.join("/", rootUrl, featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.UM.toString()));
				rtn.put(UPSTREAM_TRIBUTARIES, 
						String.join("/", rootUrl, featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.UT.toString()));
				rtn.put(DOWNSTREAM_MAIN, 
						String.join("/", rootUrl, featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.DM.toString()));
				rtn.put(DOWNSTREAM_DIVERSIONS, 
						String.join("/", rootUrl, featureSource.toLowerCase(), URLEncoder.encode(featureID, FeatureTransformer.DEFAULT_ENCODING), NavigationDao.NAVIGATE, NavigationMode.DD.toString()));
			}

		} catch (Exception e) {
			GlobalDefaultExceptionHandler.handleError(e, response);
		} finally {
			logService.logRequestComplete(logId, response.getStatus());
		}
		return rtn;
	}

}
