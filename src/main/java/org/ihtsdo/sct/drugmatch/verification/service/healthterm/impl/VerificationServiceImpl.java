package org.ihtsdo.sct.drugmatch.verification.service.healthterm.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ihtsdo.sct.drugmatch.enumeration.DescriptionType;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author dev-team@carecom.dk
 *
 */
public class VerificationServiceImpl implements VerificationService {

	private static Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

	private final CredentialsProvider credentialsProvider;

	private SSLConnectionSocketFactory customSslSocketFactory = null;

	private final DrugMatchProperties drugMatchProperties = new DrugMatchProperties();

	private final Set<String> englishLocaleCodes = new TreeSet<>(Arrays.asList(new String[] { "en", "en-GB", "en-US" }));

	private final TypeReference<List<ConceptSearchResultDescriptor>> conceptSearchResultDescriptorTypeReference = new TypeReference<List<ConceptSearchResultDescriptor>>(){};

	private final TypeReference<List<LogEntry>> logEntryTypeReference = new TypeReference<List<LogEntry>>(){};

	public VerificationServiceImpl() throws DrugMatchConfigurationException, KeyManagementException, NoSuchAlgorithmException, IOException {
		String serviceUrl = this.drugMatchProperties.getVerificationService();
		if (serviceUrl == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_SERVICE + "' isn't set!");
		}
		UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });
		if (!urlValidator.isValid(serviceUrl.toLowerCase())) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_SERVICE + "' isn't a valid HTTP/HTTPS URL!");
		}
		String login = this.drugMatchProperties.getVerificationLogin();
		if (login == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_LOGIN + "' isn't set!");
		}
		String password = this.drugMatchProperties.getVerificationPassword();
		if (password == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_PASSWORD + "' isn't set!");
		}
		
		URL url = new URL(serviceUrl);
		int port;
		if (url.getPort() > 0) {
			port = url.getPort();
		} else {
			port = ("https".equals(url.getProtocol().toLowerCase())) ? 443 : 80;
		}
		this.credentialsProvider = new BasicCredentialsProvider();
		this.credentialsProvider.setCredentials(
				new AuthScope(url.getHost(), port),
				new UsernamePasswordCredentials(login,password));
		
		// test if CA root is present in JRE, otherwise invoke SSL workaround
		try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
			HttpGet httpget = new HttpGet(url.getProtocol() + "://" + url.getHost() + ":" + port);
			log.debug("Executing request: {}", httpget.getRequestLine());
			try (CloseableHttpResponse response = httpclient.execute(httpget);) {
				log.debug("Executed request: {} status: {}", httpget.getRequestLine(), response.getStatusLine());
				EntityUtils.consume(response.getEntity());
			} catch (SSLHandshakeException e) {
				if ("sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target".equals(e.getMessage())) {
					log.debug("Unable to establish certificate trust chain, most likely cause: root certificate unavailable from JRE keystore. Proceeding with disabled TrustManager.");
					this.customSslSocketFactory = new SSLConnectionSocketFactory(initializeCerts());
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 *  Workaround for ex. StartCom CA root certificate is not included in the JRE's default keystore.
	 * 
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	private static SSLContext initializeCerts() throws KeyManagementException, NoSuchAlgorithmException {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
				// empty on purpose
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
				// empty on purpose
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		return sc;
	}

	private CloseableHttpClient getHttpClient() {
		if (this.customSslSocketFactory == null) {
			return HttpClients.custom().setDefaultCredentialsProvider(this.credentialsProvider).build();
		} // else
		return HttpClients.custom().setDefaultCredentialsProvider(this.credentialsProvider).setSSLSocketFactory(this.customSslSocketFactory).build();
	}

	private HttpGet getHttpGetJSON(String path) {
		StringBuilder url = new StringBuilder(this.drugMatchProperties.getVerificationService());
		if (!path.startsWith("/")) {
			url.append('/');
		}
		url.append(path);
		HttpGet httpget = new HttpGet(url.toString());
		httpget.setHeader("Accept", "application/json");
		return httpget;
	}

	public List<ConceptSearchResultDescriptor> getDoseFormExactEnglishPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException {
		Long constraintId = this.drugMatchProperties.getConstraintIdDoseForm();
		if (constraintId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.CONSTRAINT_ID_DOSE_FORM + "' isn't set!");
		} // else
		return getExactPreferredTermMatch(Collections.<String>emptySet(), // unable to filter on namespace as the SNOMED CT international release now contains 1+ namespace.
				Collections.singleton(constraintId),
				this.englishLocaleCodes,
				query);
	}

	public List<ConceptSearchResultDescriptor> getExactPreferredTermMatch(
			Set<String> namespaceIds,
			Set<Long> constraintIds,
			Set<String> localeCodes,
			String query) throws IOException {
		return getExactTermMatch(
				constraintIds,
				namespaceIds,
				Collections.singleton(DescriptionType.PREFERRED_TERM),
				localeCodes,
				query);
	}

	public List<ConceptSearchResultDescriptor> getExactTermMatch(
			Set<Long> constraintIds,
			Set<String> namespaceIds,
			Set<DescriptionType> descriptionTypes,
			Set<String> localeCodes,
			String query) throws IOException {
		// construct path
		StringBuilder path = new StringBuilder("/webservice/restricted/v1.0/search/concept/exactSearch?query=");
		path.append(URLEncoder.encode(query, CharEncoding.UTF_8));
		// top point constraints
		for (Long constraintId : constraintIds) {
			path.append("&constraintId=")
				.append(constraintId);
		}
		// namespaces
		for (String namespaceId : namespaceIds) {
			path.append("&namespaceId=")
				.append(URLEncoder.encode(namespaceId, CharEncoding.UTF_8));
		}
		// description types
		for (DescriptionType descriptionType : descriptionTypes) {
			path.append("&descriptionTypeId=")
				.append(descriptionType.getId());
		}
		// locale codes
		for (String localeCode : localeCodes) {
			path.append("&contentLocaleCode=")
				.append(URLEncoder.encode(localeCode, CharEncoding.UTF_8));
		}
		// search
		try (CloseableHttpClient httpclient = getHttpClient();) {
			HttpGet httpget = getHttpGetJSON(path.toString());
			try (CloseableHttpResponse response = httpclient.execute(httpget);) {
				log.debug("Executed request: {} status: {}", httpget.getRequestLine(), response.getStatusLine());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode results = mapper.readTree(response.getEntity().getContent()).get("result");
					List<ConceptSearchResultDescriptor> result = mapper.readValue(results.traverse(), this.conceptSearchResultDescriptorTypeReference);
					EntityUtils.consume(response.getEntity());
					return result;
				} // else
				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					ObjectMapper mapper = new ObjectMapper();
					JsonNode logEntries = mapper.readTree(sb.toString()).get("log");
					List<LogEntry> result = mapper.readValue(logEntries.traverse(), this.logEntryTypeReference);
					EntityUtils.consume(response.getEntity());
					if (result.isEmpty()) {
						throw new IOException("Unable to search for exact match, cause: HTTP status code " + response.getStatusLine().getStatusCode());
					} // else
					throw new IOException(result.iterator().next().toString());
				} catch (JsonParseException e) {
					throw new IOException("Unable to search for exact match, cause: HTTP status code " + response.getStatusLine().getStatusCode() +
							" response content: " + sb.toString());
				}
			}
		}
	}

	public List<ConceptSearchResultDescriptor> getSubstanceExactEnglishPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException {
		return getSubstanceExactPreferredTermMatch(
				Collections.<String>emptySet(), // unable to filter on namespace as the SNOMED CT international release now contains 1+ namespace.
				query,
				this.englishLocaleCodes);
	}

	public List<ConceptSearchResultDescriptor> getSubstanceExactNationalPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException {
		String nationalNamespaceId = this.drugMatchProperties.getNationalNamespaceId();
		if (nationalNamespaceId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.NATIONAL_NAMESPACE_ID + "' isn't set!");
		} // else
		return getSubstanceExactPreferredTermMatch(
				Collections.singleton(nationalNamespaceId),
				query,
				Collections.<String>emptySet());
	}

	public List<ConceptSearchResultDescriptor> getSubstanceExactPreferredTermMatch(
			Set<String> namespaceIds,
			String query,
			Set<String> localeCodes) throws IOException, DrugMatchConfigurationException {
		Long constraintId = this.drugMatchProperties.getConstraintIdSubstance();
		if (constraintId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.CONSTRAINT_ID_SUBSTANCE + "' isn't set!");
		} // else
		return getExactPreferredTermMatch(
				namespaceIds,
				Collections.singleton(constraintId),
				localeCodes,
				query);
	}

	public List<ConceptSearchResultDescriptor> getUnitExactEnglishPreferredTermMatch(String query) throws IOException, DrugMatchConfigurationException {
		Long constraintId = this.drugMatchProperties.getConstraintIdUnit();
		if (constraintId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.CONSTRAINT_ID_UNIT + "' isn't set!");
		} // else
		return getExactPreferredTermMatch(Collections.<String>emptySet(), // unable to filter on namespace as the SNOMED CT international release now contains 1+ namespace.
				Collections.singleton(constraintId),
				this.englishLocaleCodes,
				query);
	}
}
